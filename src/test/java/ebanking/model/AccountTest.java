package ebanking.model;

import ebanking.exception.InsufficientFundsException;
import ebanking.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    @DisplayName("the restore constructor keeps the stored id and creation time")
    void restoreConstructorKeepsIdAndCreatedAt() throws ValidationException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 2, 3, 4, 5);

        Account account = new Account("id-1", created, "RO01", "Ana Pop", BigDecimal.TEN, AccountType.SAVINGS);

        assertEquals("id-1", account.getId());
        assertEquals(created, account.getCreatedAt());
    }

    @Test
    @DisplayName("the restore constructor with a blank id throws ValidationException")
    void restoreConstructorRejectsBlankId() {
        assertThrows(ValidationException.class, () -> new Account(" ", LocalDateTime.now(), "RO01",
                "Ana Pop", BigDecimal.TEN, AccountType.CHECKING));
    }

    @Test
    @DisplayName("deposit and withdraw change the balance; overdrawing throws and keeps it")
    void depositAndWithdrawChangeBalance() throws ValidationException {
        Account account = new Account("RO01", "Ana Pop", new BigDecimal("50"), AccountType.CHECKING);

        account.deposit(new BigDecimal("10"));
        account.withdraw(new BigDecimal("20"));
        assertEquals(new BigDecimal("40"), account.getBalance());

        assertThrows(InsufficientFundsException.class, () -> account.withdraw(new BigDecimal("80")));
        assertEquals(new BigDecimal("40"), account.getBalance());
    }

    @Test
    @DisplayName("adding a transaction of another account to the history throws ValidationException")
    void foreignTransactionIsRejectedInHistory() throws ValidationException {
        Account account = new Account("RO01", "Ana Pop", BigDecimal.ZERO, AccountType.CHECKING);

        assertThrows(ValidationException.class,
                () -> account.addToHistory(new DepositTransaction("other-id", BigDecimal.TEN)));
    }
}
