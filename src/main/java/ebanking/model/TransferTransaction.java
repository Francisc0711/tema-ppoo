package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A transfer from the source account ({@link #getAccountId()}) to a target account. */
public class TransferTransaction extends Transaction {

    private final String targetAccountId;

    public TransferTransaction(String sourceAccountId, String targetAccountId, BigDecimal amount)
            throws ValidationException {
        super(sourceAccountId, amount);
        validateTarget(sourceAccountId, targetAccountId);
        this.targetAccountId = targetAccountId;
    }

    /** Restores a transfer with a stored id and creation time. */
    public TransferTransaction(String id, LocalDateTime createdAt, String sourceAccountId,
                               String targetAccountId, BigDecimal amount) throws ValidationException {
        super(id, createdAt, sourceAccountId, amount);
        validateTarget(sourceAccountId, targetAccountId);
        this.targetAccountId = targetAccountId;
    }

    private static void validateTarget(String sourceAccountId, String targetAccountId)
            throws ValidationException {
        if (targetAccountId == null || targetAccountId.isBlank()) {
            throw new ValidationException("targetAccountId is required");
        }
        if (targetAccountId.equals(sourceAccountId)) {
            throw new ValidationException("source and target accounts must differ");
        }
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }

    @Override
    public boolean involves(String accountId) {
        return super.involves(accountId) || targetAccountId.equals(accountId);
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }
}
