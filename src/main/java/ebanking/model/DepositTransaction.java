package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;

public class DepositTransaction extends Transaction {

    public DepositTransaction(String accountId, BigDecimal amount) throws ValidationException {
        super(accountId, amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
}
