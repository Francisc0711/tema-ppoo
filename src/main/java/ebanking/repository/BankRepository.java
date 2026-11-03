package ebanking.repository;

import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.Transaction;
import ebanking.model.TransactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** In-memory store of accounts and transactions. */
public class BankRepository {

    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public void addAccount(Account account) throws ValidationException {
        if (account == null) {
            throw new ValidationException("account is required");
        }
        for (Account existing : accounts) {
            if (existing.getAccountNumber().equals(account.getAccountNumber())) {
                throw new ValidationException("account number already exists: " + account.getAccountNumber());
            }
        }
        accounts.add(account);
    }

    public Optional<Account> findAccountById(String id) {
        for (Account account : accounts) {
            if (account.getId().equals(id)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }

    public void addTransaction(Transaction transaction) throws ValidationException {
        if (transaction == null) {
            throw new ValidationException("transaction is required");
        }
        transactions.add(transaction);
    }

    public Optional<Transaction> findTransactionById(String id) {
        for (Transaction transaction : transactions) {
            if (transaction.getId().equals(id)) {
                return Optional.of(transaction);
            }
        }
        return Optional.empty();
    }

    public boolean removeTransaction(String id) {
        return transactions.removeIf(t -> t.getId().equals(id));
    }

    /** Replaces the stored transaction with the same id, keeping its position. */
    public void replaceTransaction(Transaction transaction) throws ValidationException {
        if (transaction == null) {
            throw new ValidationException("transaction is required");
        }
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                return;
            }
        }
        throw new ValidationException("unknown transaction: " + transaction.getId());
    }

    public List<Transaction> findTransactionsByAccount(String accountId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.involves(accountId)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> findTransactionsByType(TransactionType type) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getType() == type) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Account> getAccounts() {
        return List.copyOf(accounts);
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
