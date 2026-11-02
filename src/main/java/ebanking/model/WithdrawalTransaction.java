package ebanking.model;

import ebanking.exception.ValidationException;

import java.math.BigDecimal;

public class WithdrawalTransaction extends Transaction {

    public WithdrawalTransaction(String accountId, BigDecimal amount) throws ValidationException {
        super(accountId, amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAWAL;
    }
}
