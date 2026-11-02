package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;

/** A transfer from the source account ({@link #getAccountId()}) to a target account. */
public class TransferTransaction extends Transaction {

    private final String targetAccountId;

    public TransferTransaction(String sourceAccountId, String targetAccountId, BigDecimal amount)
            throws ValidationException {
        super(sourceAccountId, amount);
        if (targetAccountId == null || targetAccountId.isBlank()) {
            throw new ValidationException("targetAccountId is required");
        }
        if (targetAccountId.equals(sourceAccountId)) {
            throw new ValidationException("source and target accounts must differ");
        }
        this.targetAccountId = targetAccountId;
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
