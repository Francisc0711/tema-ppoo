package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalTransaction extends Transaction {

    public WithdrawalTransaction(String accountId, BigDecimal amount) throws ValidationException {
        super(accountId, amount);
    }

    public WithdrawalTransaction(String id, LocalDateTime createdAt, String accountId, BigDecimal amount)
            throws ValidationException {
        super(id, createdAt, accountId, amount);
    }

    @Override
    public WithdrawalTransaction withAmount(BigDecimal amount) throws ValidationException {
        return new WithdrawalTransaction(getId(), getCreatedAt(), getAccountId(), amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAWAL;
    }
}
