package ebanking.model;

import ebanking.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    @DisplayName("a valid account keeps its initial balance and gets an id and creation time")
    void validAccountIsCreated() throws ValidationException {
        Account account = new Account("RO01", "Ana Pop", new BigDecimal("100.50"), AccountType.CHECKING);

        assertEquals(new BigDecimal("100.50"), account.getBalance());
        assertNotNull(account.getId());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    @DisplayName("a null account number throws ValidationException")
    void nullAccountNumberIsRejected() {
        assertThrows(ValidationException.class,
                () -> new Account(null, "Ana Pop", BigDecimal.TEN, AccountType.CHECKING));
    }

    @Test
    @DisplayName("a negative initial balance throws ValidationException")
    void negativeBalanceIsRejected() {
        assertThrows(ValidationException.class,
                () -> new Account("RO01", "Ana Pop", new BigDecimal("-1"), AccountType.SAVINGS));
    }

    @Test
    @DisplayName("adding the 11th transaction discards the oldest")
    void historyDiscardsOldestWhenFull() throws ValidationException {
        Account account = new Account("RO01", "Ana Pop", BigDecimal.ZERO, AccountType.CHECKING);
        Transaction[] added = new Transaction[Account.HISTORY_CAPACITY + 1];
        for (int i = 0; i < added.length; i++) {
            added[i] = new DepositTransaction(account.getId(), BigDecimal.valueOf(i + 1));
            account.addToHistory(added[i]);
        }

        Transaction[] history = account.getRecentHistory();

        assertEquals(Account.HISTORY_CAPACITY, history.length);
        assertSame(added[1], history[0]);
        assertSame(added[10], history[history.length - 1]);
    }
}
