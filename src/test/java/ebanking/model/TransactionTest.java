package ebanking.model;

import ebanking.exception.ValidationException;
import ebanking.repository.BankRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    @Test
    @DisplayName("each subtype reports its type and filtering by type returns exactly that transaction")
    void subtypesReportTheirType() throws ValidationException {
        Transaction deposit = new DepositTransaction("acc-a", BigDecimal.TEN);
        Transaction withdrawal = new WithdrawalTransaction("acc-a", BigDecimal.ONE);
        Transaction transfer = new TransferTransaction("acc-a", "acc-b", BigDecimal.ONE);
        BankRepository repository = new BankRepository();
        for (Transaction transaction : List.of(deposit, withdrawal, transfer)) {
            repository.addTransaction(transaction);
        }

        assertEquals(TransactionType.DEPOSIT, deposit.getType());
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.getType());
        assertEquals(TransactionType.TRANSFER, transfer.getType());
        assertEquals(List.of(deposit), repository.findTransactionsByType(TransactionType.DEPOSIT));
        assertEquals(List.of(withdrawal), repository.findTransactionsByType(TransactionType.WITHDRAWAL));
        assertEquals(List.of(transfer), repository.findTransactionsByType(TransactionType.TRANSFER));
    }

    @Test
    @DisplayName("a transaction with a zero amount throws ValidationException")
    void zeroAmountIsRejected() {
        assertThrows(ValidationException.class,
                () -> new DepositTransaction("acc-1", BigDecimal.ZERO));
    }
}
