package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepositTransaction extends Transaction {

    public DepositTransaction(String accountId, BigDecimal amount) throws ValidationException {
        super(accountId, amount);
    }

    public DepositTransaction(String id, LocalDateTime createdAt, String accountId, BigDecimal amount)
            throws ValidationException {
        super(id, createdAt, accountId, amount);
    }

    @Override
    public DepositTransaction withAmount(BigDecimal amount) throws ValidationException {
        return new DepositTransaction(getId(), getCreatedAt(), getAccountId(), amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
}
