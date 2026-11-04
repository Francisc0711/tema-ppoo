package ebanking.cli;

import ebanking.exception.InsufficientFundsException;
import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.Transaction;
import ebanking.model.TransferTransaction;
import ebanking.repository.BankRepository;
import ebanking.service.TransactionService;
import ebanking.storage.StorageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Text menu over the repository and the transaction service. Never prints internal ids. */
public class ConsoleMenu {

    private static final String MENU = String.join(System.lineSeparator(),
            "",
            "=== E-Banking ===",
            "1. Create account",
            "2. List accounts",
            "3. Deposit",
            "4. Withdraw",
            "5. Transfer",
            "6. List transactions of an account",
            "7. Update transaction amount",
            "8. Delete transaction",
            "9. Recent history of an account",
            "0. Exit",
            "Choice: ");

    private final BufferedReader in;
    private final PrintStream out;
    private final BankRepository repository;
    private final TransactionService service;

    public ConsoleMenu(InputStream in, PrintStream out, BankRepository repository, TransactionService service) {
        if (in == null || out == null || repository == null || service == null) {
            throw new IllegalArgumentException("in, out, repository and service are required");
        }
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
        this.repository = repository;
        this.service = service;
    }

    /** Runs until the user chooses 0 or the input ends. */
    public void run() {
        while (true) {
            out.print(MENU);
            String choice = readLine();
            if (choice == null || choice.trim().equals("0")) {
                out.println();
                return;
            }
            try {
                handle(choice.trim());
            } catch (ValidationException | InsufficientFundsException e) {
                out.println("Error: " + e.getMessage());
            } catch (StorageException e) {
                out.println("Storage error: " + e.getMessage());
            }
        }
    }

    private void handle(String choice) throws ValidationException, StorageException {
        switch (choice) {
            case "1" -> createAccount();
            case "2" -> listAccounts();
            case "3" -> deposit();
            case "4" -> withdraw();
            case "5" -> transfer();
            case "6" -> listTransactions();
            case "7" -> updateTransaction();
            case "8" -> deleteTransaction();
            case "9" -> recentHistory();
            default -> out.println("Unknown choice: " + choice);
        }
    }

    private void createAccount() throws ValidationException, StorageException {
        String number = prompt("Account number: ");
        String owner = prompt("Owner name: ");
        BigDecimal balance = readAmount("Initial balance: ");
        AccountType type = readType("Type (CHECKING/SAVINGS): ");
        Account account = service.createAccount(number, owner, balance, type);
        out.println("Created account " + account.getAccountNumber());
    }

    private void listAccounts() {
        List<Account> accounts = repository.getAccounts();
        if (accounts.isEmpty()) {
            out.println("No accounts.");
        }
        for (Account a : accounts) {
            out.println(a.getAccountNumber() + ", " + a.getOwnerName() + ", " + a.getType()
                    + ", " + a.getBalance());
        }
    }

    private void deposit() throws ValidationException, StorageException {
        Account account = readAccount("Account number: ");
        BigDecimal amount = readAmount("Amount: ");
        service.addDeposit(account.getId(), amount);
        out.println("Deposited " + amount + " on " + account.getAccountNumber()
                + ". Balance: " + account.getBalance());
    }

    private void withdraw() throws ValidationException, StorageException {
        Account account = readAccount("Account number: ");
        BigDecimal amount = readAmount("Amount: ");
        service.addWithdrawal(account.getId(), amount);
        out.println("Withdrew " + amount + " from " + account.getAccountNumber()
                + ". Balance: " + account.getBalance());
    }

    private void transfer() throws ValidationException, StorageException {
        Account source = readAccount("Source account number: ");
        Account target = readAccount("Target account number: ");
        BigDecimal amount = readAmount("Amount: ");
        service.addTransfer(source.getId(), target.getId(), amount);
        out.println("Transferred " + amount + " from " + source.getAccountNumber() + " to "
                + target.getAccountNumber() + ". Balance: " + source.getBalance());
    }

    private void listTransactions() throws ValidationException {
        Account account = readAccount("Account number: ");
        List<Transaction> list = repository.findTransactionsByAccount(account.getId());
        if (list.isEmpty()) {
            out.println("No transactions.");
        }
        for (int i = 0; i < list.size(); i++) {
            out.println((i + 1) + ". " + describe(list.get(i)));
        }
    }

    private void updateTransaction() throws ValidationException, StorageException {
        Account account = readAccount("Account number: ");
        Transaction tx = readTransaction(account);
        BigDecimal amount = readAmount("New amount: ");
        service.updateTransactionAmount(tx.getId(), amount);
        out.println("Updated. Balance: " + account.getBalance());
    }

    private void deleteTransaction() throws ValidationException, StorageException {
        Account account = readAccount("Account number: ");
        Transaction tx = readTransaction(account);
        service.deleteTransaction(tx.getId());
        out.println("Deleted. Balance: " + account.getBalance());
    }

    private void recentHistory() throws ValidationException {
        Account account = readAccount("Account number: ");
        Transaction[] history = account.getRecentHistory();
        if (history.length == 0) {
            out.println("No transactions.");
        }
        for (int i = history.length - 1; i >= 0; i--) { // newest first
            out.println(describe(history[i]));
        }
    }

    private Account readAccount(String prompt) throws ValidationException {
        String number = prompt(prompt);
        for (Account a : repository.getAccounts()) {
            if (a.getAccountNumber().equals(number)) {
                return a;
            }
        }
        throw new ValidationException("unknown account: " + number);
    }

    private BigDecimal readAmount(String prompt) throws ValidationException {
        String text = prompt(prompt);
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("not a number: " + text);
        }
    }

    private AccountType readType(String prompt) throws ValidationException {
        String text = prompt(prompt);
        try {
            return AccountType.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("unknown account type: " + text);
        }
    }

    /** Lists the account's transactions, asks for a number and returns that transaction. */
    private Transaction readTransaction(Account account) throws ValidationException {
        List<Transaction> list = repository.findTransactionsByAccount(account.getId());
        for (int i = 0; i < list.size(); i++) {
            out.println((i + 1) + ". " + describe(list.get(i)));
        }
        String text = prompt("Transaction number: ");
        int number;
        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("unknown transaction number: " + text);
        }
        if (number < 1 || number > list.size()) {
            throw new ValidationException("unknown transaction number: " + text);
        }
        return list.get(number - 1);
    }

    private String describe(Transaction t) {
        String text = t.getType() + " " + t.getAmount();
        if (t instanceof TransferTransaction transfer) {
            text += " from " + numberOf(t.getAccountId()) + " to " + numberOf(transfer.getTargetAccountId());
        }
        return text + " (" + t.getCreatedAt() + ")";
    }

    private String numberOf(String accountId) {
        return repository.findAccountById(accountId).map(Account::getAccountNumber).orElse("?");
    }

    /** Prints the prompt and returns the trimmed answer; end of input counts as an empty answer. */
    private String prompt(String text) {
        out.print(text);
        String line = readLine();
        return line == null ? "" : line.trim();
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
