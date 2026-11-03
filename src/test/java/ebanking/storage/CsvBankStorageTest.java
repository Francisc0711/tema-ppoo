package ebanking.storage;

import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.DepositTransaction;
import ebanking.model.Transaction;
import ebanking.model.TransferTransaction;
import ebanking.model.WithdrawalTransaction;
import ebanking.repository.BankRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvBankStorageTest {

    private static final String ACCOUNT_HEADER = "id,createdAt,accountNumber,ownerName,balance,type";
    private static final String TRANSACTION_HEADER = "id,createdAt,type,accountId,targetAccountId,amount";

    @TempDir
    Path dir;

    private Account account(BankRepository repo, String number, String owner) throws ValidationException {
        Account a = new Account(number, owner, new BigDecimal("100.50"), AccountType.CHECKING);
        repo.addAccount(a);
        return a;
    }

    @Test
    @DisplayName("save writes a header and one line per account with six fields")
    void saveWritesAccountsFile() throws Exception {
        BankRepository repo = new BankRepository();
        account(repo, "RO01", "Ana");
        account(repo, "RO02", "Ion");

        new CsvBankStorage(dir).save(repo);

        List<String> lines = Files.readAllLines(dir.resolve("accounts.csv"), StandardCharsets.UTF_8);
        assertEquals(3, lines.size());
        assertEquals(ACCOUNT_HEADER, lines.get(0));
        assertEquals(6, lines.get(1).split(",").length);
    }

    @Test
    @DisplayName("accounts and all transaction subtypes survive a save and load")
    void roundTrip() throws Exception {
        BankRepository repo = new BankRepository();
        Account a = account(repo, "RO01", "Ana");
        Account b = account(repo, "RO02", "Ion");
        repo.addTransaction(new DepositTransaction(a.getId(), new BigDecimal("10.25")));
        repo.addTransaction(new WithdrawalTransaction(a.getId(), new BigDecimal("5")));
        repo.addTransaction(new TransferTransaction(a.getId(), b.getId(), new BigDecimal("3.10")));
        CsvBankStorage storage = new CsvBankStorage(dir);

        storage.save(repo);
        BankRepository loaded = storage.load();

        assertEquals(2, loaded.getAccounts().size());
        for (int i = 0; i < 2; i++) {
            Account x = repo.getAccounts().get(i);
            Account y = loaded.getAccounts().get(i);
            assertEquals(x.getId(), y.getId());
            assertEquals(x.getCreatedAt(), y.getCreatedAt());
            assertEquals(x.getAccountNumber(), y.getAccountNumber());
            assertEquals(x.getOwnerName(), y.getOwnerName());
            assertEquals(x.getBalance(), y.getBalance());
            assertEquals(x.getType(), y.getType());
        }
        assertEquals(3, loaded.getTransactions().size());
        for (int i = 0; i < 3; i++) {
            Transaction x = repo.getTransactions().get(i);
            Transaction y = loaded.getTransactions().get(i);
            assertEquals(x.getClass(), y.getClass());
            assertEquals(x.getId(), y.getId());
            assertEquals(x.getCreatedAt(), y.getCreatedAt());
            assertEquals(x.getAccountId(), y.getAccountId());
            assertEquals(x.getAmount(), y.getAmount());
        }
        TransferTransaction transfer = assertInstanceOf(TransferTransaction.class, loaded.getTransactions().get(2));
        assertEquals(b.getId(), transfer.getTargetAccountId());
    }

    @Test
    @DisplayName("loading from a directory without files returns an empty repository")
    void emptyDirectory() throws Exception {
        BankRepository loaded = new CsvBankStorage(dir).load();

        assertTrue(loaded.getAccounts().isEmpty());
        assertTrue(loaded.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("commas, quotes and line breaks in a field round-trip unchanged")
    void specialCharacters() throws Exception {
        BankRepository repo = new BankRepository();
        String owner = "Popescu, Ion \"Junior\"\nSecond line";
        account(repo, "RO01", owner);
        CsvBankStorage storage = new CsvBankStorage(dir);

        storage.save(repo);

        assertEquals(owner, storage.load().getAccounts().get(0).getOwnerName());
    }

    @Test
    @DisplayName("the history of a loaded account holds the last 10 transactions, oldest first")
    void historyKeepsLastTen() throws Exception {
        BankRepository repo = new BankRepository();
        Account a = account(repo, "RO01", "Ana");
        for (int i = 0; i < 12; i++) {
            repo.addTransaction(new DepositTransaction(a.getId(), BigDecimal.ONE));
        }
        CsvBankStorage storage = new CsvBankStorage(dir);
        storage.save(repo);

        BankRepository loaded = storage.load();

        Transaction[] history = loaded.getAccounts().get(0).getRecentHistory();
        assertEquals(10, history.length);
        for (int i = 0; i < 10; i++) {
            assertEquals(repo.getTransactions().get(i + 2).getId(), history[i].getId());
        }
    }

    @Test
    @DisplayName("an invalid amount fails the load with file name and line")
    void invalidAmountReportsLocation() throws Exception {
        BankRepository repo = new BankRepository();
        Account a = account(repo, "RO01", "Ana");
        repo.addTransaction(new DepositTransaction(a.getId(), BigDecimal.ONE));
        repo.addTransaction(new DepositTransaction(a.getId(), BigDecimal.TEN));
        CsvBankStorage storage = new CsvBankStorage(dir);
        storage.save(repo);
        Path file = dir.resolve("transactions.csv");
        List<String> lines = Files.readAllLines(file);
        lines.set(2, lines.get(2).replace(",10", ",abc"));
        Files.write(file, lines);

        StorageException e = assertThrows(StorageException.class, storage::load);

        assertTrue(e.getMessage().contains("transactions.csv"));
        assertTrue(e.getMessage().contains("line 3"));
    }

    @Test
    @DisplayName("a transaction for an unknown account fails the load")
    void unknownAccountFails() throws IOException {
        Files.writeString(dir.resolve("accounts.csv"), ACCOUNT_HEADER + "\n");
        Files.writeString(dir.resolve("transactions.csv"), TRANSACTION_HEADER
                + "\nt1,2026-01-01T10:00,DEPOSIT,missing,,5\n");

        StorageException e = assertThrows(StorageException.class, () -> new CsvBankStorage(dir).load());

        assertTrue(e.getMessage().contains("unknown account"));
    }
}
