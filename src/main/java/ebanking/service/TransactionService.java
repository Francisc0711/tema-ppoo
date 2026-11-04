package ebanking.service;

import ebanking.exception.InsufficientFundsException;
import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.DepositTransaction;
import ebanking.model.Transaction;
import ebanking.model.TransferTransaction;
import ebanking.model.WithdrawalTransaction;
import ebanking.repository.BankRepository;
import ebanking.storage.CsvBankStorage;
import ebanking.storage.StorageException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adds, updates and deletes transactions, keeping balances and histories consistent.
 * Every operation checks all resulting balances before changing anything, then saves.
 */
public class TransactionService {

    private final BankRepository repository;
    private final CsvBankStorage storage;

    public TransactionService(BankRepository repository, CsvBankStorage storage) {
        if (repository == null) {
            throw new IllegalArgumentException("repository is required");
        }
        if (storage == null) {
            throw new IllegalArgumentException("storage is required");
        }
        this.repository = repository;
        this.storage = storage;
    }

    /** Creates an account, rejecting invalid data and duplicate numbers, and saves. */
    public Account createAccount(String number, String owner, BigDecimal balance, AccountType type)
            throws ValidationException, StorageException {
        Account account = new Account(number, owner, balance, type);
        repository.addAccount(account);
        storage.save(repository);
        return account;
    }

    public Transaction addDeposit(String accountId, BigDecimal amount)
            throws ValidationException, StorageException {
        return add(new DepositTransaction(accountId, amount));
    }

    public Transaction addWithdrawal(String accountId, BigDecimal amount)
            throws ValidationException, StorageException {
        return add(new WithdrawalTransaction(accountId, amount));
    }

    public Transaction addTransfer(String sourceId, String targetId, BigDecimal amount)
            throws ValidationException, StorageException {
        return add(new TransferTransaction(sourceId, targetId, amount));
    }

    /** Deletes a transaction and reverses its effect on the balances. */
    public void deleteTransaction(String id) throws ValidationException, StorageException {
        Transaction old = find(id);
        Map<String, BigDecimal> delta = new LinkedHashMap<>();
        effect(old).forEach((k, v) -> delta.put(k, v.negate()));
        apply(delta);
        repository.removeTransaction(id);
        rebuildHistories(old);
        storage.save(repository);
    }

    /** Changes the amount of a transaction; id, creation time, type and accounts stay the same. */
    public Transaction updateTransactionAmount(String id, BigDecimal newAmount)
            throws ValidationException, StorageException {
        Transaction old = find(id);
        Transaction updated = old.withAmount(newAmount);
        Map<String, BigDecimal> delta = new LinkedHashMap<>(effect(updated));
        effect(old).forEach((k, v) -> delta.merge(k, v.negate(), BigDecimal::add));
        apply(delta);
        repository.replaceTransaction(updated);
        rebuildHistories(updated);
        storage.save(repository);
        return updated;
    }

    private Transaction add(Transaction tx) throws ValidationException, StorageException {
        Map<String, BigDecimal> delta = effect(tx);
        for (String accountId : delta.keySet()) {
            account(accountId); // unknown accounts fail before anything changes
        }
        apply(delta);
        repository.addTransaction(tx);
        for (String accountId : delta.keySet()) {
            account(accountId).addToHistory(tx);
        }
        storage.save(repository);
        return tx;
    }

    private Transaction find(String id) throws ValidationException {
        return repository.findTransactionById(id)
                .orElseThrow(() -> new ValidationException("unknown transaction: " + id));
    }

    private Account account(String id) throws ValidationException {
        return repository.findAccountById(id)
                .orElseThrow(() -> new ValidationException("unknown account: " + id));
    }

    /** Signed balance change per account id caused by this transaction. */
    private static Map<String, BigDecimal> effect(Transaction t) {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        switch (t.getType()) {
            case DEPOSIT -> m.put(t.getAccountId(), t.getAmount());
            case WITHDRAWAL -> m.put(t.getAccountId(), t.getAmount().negate());
            default -> {
                m.put(t.getAccountId(), t.getAmount().negate());
                m.put(((TransferTransaction) t).getTargetAccountId(), t.getAmount());
            }
        }
        return m;
    }

    /** Checks every resulting balance first, then changes the accounts. */
    private void apply(Map<String, BigDecimal> delta) throws ValidationException {
        for (Map.Entry<String, BigDecimal> e : delta.entrySet()) {
            Account a = account(e.getKey());
            if (a.getBalance().add(e.getValue()).signum() < 0) {
                throw new InsufficientFundsException("insufficient funds on " + a.getAccountNumber());
            }
        }
        for (Map.Entry<String, BigDecimal> e : delta.entrySet()) {
            if (e.getValue().signum() > 0) {
                account(e.getKey()).deposit(e.getValue());
            } else if (e.getValue().signum() < 0) {
                account(e.getKey()).withdraw(e.getValue().negate());
            }
        }
    }

    private void rebuildHistories(Transaction tx) throws ValidationException {
        for (String accountId : effect(tx).keySet()) {
            account(accountId).rebuildHistory(repository.getTransactions());
        }
    }
}
