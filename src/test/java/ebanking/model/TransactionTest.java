package ebanking.model;

import ebanking.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    @Test
    @DisplayName("a transaction with a zero amount throws ValidationException")
    void zeroAmountIsRejected() {
        assertThrows(ValidationException.class,
                () -> new DepositTransaction("acc-1", BigDecimal.ZERO));
    }
}
