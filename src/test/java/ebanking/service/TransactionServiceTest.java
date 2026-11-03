package ebanking.service;

import ebanking.exception.InsufficientFundsException;
import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.Transaction;
import ebanking.repository.BankRepository;
import ebanking.storage.CsvBankStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {

    @TempDir
    Path dir;

    private BankRepository repo;
    private CsvBankStorage storage;
    private TransactionService service;
    private Account ana;
    private Account bob;

    @BeforeEach
    void setUp() throws ValidationException {
        repo = new BankRepository();
        storage = new CsvBankStorage(dir);
        service = new TransactionService(repo, storage);
        ana = new Account("RO01", "Ana Pop", new BigDecimal("100"), AccountType.CHECKING);
        bob = new Account("RO02", "Bob Ion", new BigDecimal("20"), AccountType.SAVINGS);
        repo.addAccount(ana);
        repo.addAccount(bob);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static Transaction newest(Account a) {
        Transaction[] h = a.getRecentHistory();
        return h[h.length - 1];
    }

    @Test
    @DisplayName("a withdrawal lowers the balance and is stored and in the history")
    void withdrawalChangesBalance() throws Exception {
        Transaction tx = service.addWithdrawal(ana.getId(), bd("30"));

        assertEquals(bd("70"), ana.getBalance());
        assertEquals(List.of(tx), repo.getTransactions());
        assertSame(tx, newest(ana));
    }

    @Test
    @DisplayName("a transfer without enough funds throws and changes nothing")
    void transferWithoutFundsChangesNothing() {
        assertThrows(InsufficientFundsException.class,
                () -> service.addTransfer(bob.getId(), ana.getId(), bd("50")));

        assertEquals(bd("20"), bob.getBalance());
        assertEquals(bd("100"), ana.getBalance());
        assertEquals(0, repo.getTransactions().size());
    }

    @Test
    @DisplayName("a transfer moves the amount and appears in both histories")
    void transferMovesMoney() throws Exception {
        Transaction tx = service.addTransfer(ana.getId(), bob.getId(), bd("50"));

        assertEquals(bd("50"), ana.getBalance());
        assertEquals(bd("70"), bob.getBalance());
        assertSame(tx, newest(ana));
        assertSame(tx, newest(bob));
    }

    @Test
    @DisplayName("a deposit on an unknown account throws ValidationException and changes nothing")
    void unknownAccountIsRejected() {
        assertThrows(ValidationException.class, () -> service.addDeposit("missing", bd("10")));

        assertEquals(0, repo.getTransactions().size());
    }

    @Test
    @DisplayName("deleting a deposit that was already spent fails; deleting the withdrawal restores money")
    void deleteChecksBalances() throws Exception {
        Transaction deposit = service.addDeposit(bob.getId(), bd("40"));
        Transaction withdrawal = service.addWithdrawal(bob.getId(), bd("60"));
        assertEquals(bd("0"), bob.getBalance());

        assertThrows(InsufficientFundsException.class, () -> service.deleteTransaction(deposit.getId()));
        assertEquals(bd("0"), bob.getBalance());
        assertEquals(2, repo.getTransactions().size());

        service.deleteTransaction(withdrawal.getId());

        assertEquals(bd("60"), bob.getBalance());
        assertEquals(1, repo.getTransactions().size());
        assertSame(deposit, newest(bob));
    }

    @Test
    @DisplayName("updating an amount adjusts the balance and keeps id, creation time and position")
    void updateKeepsIdentity() throws Exception {
        Transaction first = service.addDeposit(ana.getId(), bd("40"));
        service.addDeposit(ana.getId(), bd("5"));

        Transaction updated = service.updateTransactionAmount(first.getId(), bd("10"));

        assertEquals(bd("115"), ana.getBalance());
        assertEquals(first.getId(), updated.getId());
        assertEquals(first.getCreatedAt(), updated.getCreatedAt());
        assertSame(updated, repo.getTransactions().get(0));
        assertSame(updated, ana.getRecentHistory()[0]);
    }

    @Test
    @DisplayName("deleting or updating an unknown transaction throws ValidationException")
    void unknownTransactionIsRejected() {
        assertThrows(ValidationException.class, () -> service.deleteTransaction("missing"));
        assertThrows(ValidationException.class, () -> service.updateTransactionAmount("missing", bd("1")));
    }

    @Test
    @DisplayName("a successful operation is saved; a failed one leaves the files unchanged")
    void operationsAreSaved() throws Exception {
        service.addDeposit(ana.getId(), bd("25"));
        BankRepository loaded = storage.load();

        assertEquals(bd("125"), loaded.findAccountById(ana.getId()).orElseThrow().getBalance());
        assertEquals(1, loaded.getTransactions().size());

        String before = read();
        assertThrows(InsufficientFundsException.class, () -> service.addWithdrawal(ana.getId(), bd("500")));
        assertEquals(before, read());
    }

    private String read() throws IOException {
        return Files.readString(dir.resolve("accounts.csv")) + Files.readString(dir.resolve("transactions.csv"));
    }
}
