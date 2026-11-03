package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** An immutable money movement on an account. */
public abstract class Transaction extends BankEntity {

    private final String accountId;
    private final BigDecimal amount;

    protected Transaction(String accountId, BigDecimal amount) throws ValidationException {
        validate(accountId, amount);
        this.accountId = accountId;
        this.amount = amount;
    }

    /** Restores a transaction with a stored id and creation time. */
    protected Transaction(String id, LocalDateTime createdAt, String accountId, BigDecimal amount)
            throws ValidationException {
        super(id, createdAt);
        validate(accountId, amount);
        this.accountId = accountId;
        this.amount = amount;
    }

    private static void validate(String accountId, BigDecimal amount) throws ValidationException {
        if (accountId == null || accountId.isBlank()) {
            throw new ValidationException("accountId is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("amount must be positive");
        }
    }

    public abstract TransactionType getType();

    /** Returns whether this transaction concerns the given account. */
    public boolean involves(String accountId) {
        return this.accountId.equals(accountId);
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
