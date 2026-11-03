package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A bank account with a fixed-size buffer of its most recent transactions. */
public class Account extends BankEntity {

    public static final int HISTORY_CAPACITY = 10;

    private final String accountNumber;
    private String ownerName;
    private final BigDecimal balance;
    private final AccountType type;

    private final Transaction[] recentHistory = new Transaction[HISTORY_CAPACITY];
    private int historyStart = 0;   // index of the oldest entry
    private int historySize = 0;

    public Account(String accountNumber, String ownerName, BigDecimal initialBalance, AccountType type)
            throws ValidationException {
        validate(accountNumber, ownerName, initialBalance, type);
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.type = type;
    }

    /** Restores an account with a stored id and creation time. */
    public Account(String id, LocalDateTime createdAt, String accountNumber, String ownerName,
                   BigDecimal balance, AccountType type) throws ValidationException {
        super(id, createdAt);
        validate(accountNumber, ownerName, balance, type);
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.type = type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) throws ValidationException {
        validateOwnerName(ownerName);
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountType getType() {
        return type;
    }

    /** Adds a transaction to the history; when full, the oldest entry is overwritten. */
    public void addToHistory(Transaction transaction) throws ValidationException {
        if (transaction == null) {
            throw new ValidationException("transaction is required");
        }
        int end = (historyStart + historySize) % HISTORY_CAPACITY;
        recentHistory[end] = transaction;
        if (historySize < HISTORY_CAPACITY) {
            historySize++;
        } else {
            historyStart = (historyStart + 1) % HISTORY_CAPACITY; // overwrote the oldest
        }
    }

    /** Returns a copy of the history, oldest first. */
    public Transaction[] getRecentHistory() {
        Transaction[] copy = new Transaction[historySize];
        for (int i = 0; i < historySize; i++) {
            copy[i] = recentHistory[(historyStart + i) % HISTORY_CAPACITY];
        }
        return copy;
    }

    private static void validate(String accountNumber, String ownerName, BigDecimal balance, AccountType type)
            throws ValidationException {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new ValidationException("accountNumber is required");
        }
        validateOwnerName(ownerName);
        if (type == null) {
            throw new ValidationException("type is required");
        }
        if (balance == null || balance.signum() < 0) {
            throw new ValidationException("initial balance must be zero or positive");
        }
    }

    private static void validateOwnerName(String ownerName) throws ValidationException {
        if (ownerName == null || ownerName.isBlank()) {
            throw new ValidationException("ownerName is required");
        }
    }
}
