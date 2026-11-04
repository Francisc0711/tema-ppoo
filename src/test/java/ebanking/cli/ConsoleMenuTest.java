package ebanking.cli;

import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.repository.BankRepository;
import ebanking.service.TransactionService;
import ebanking.storage.CsvBankStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleMenuTest {

    @TempDir
    Path dir;

    private BankRepository repo;
    private CsvBankStorage storage;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        repo = new BankRepository();
        storage = new CsvBankStorage(dir.resolve("data")); // directory does not exist yet
        service = new TransactionService(repo, storage);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private Account account(String number, String balance) throws Exception {
        return service.createAccount(number, "Owner " + number, bd(balance), AccountType.CHECKING);
    }

    /** Runs the menu with the given lines (followed by 0) and returns everything it printed. */
    private String run(String... lines) {
        String input = String.join("\n", lines) + "\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleMenu(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), repo, service).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("T1: choice 0 prints the menu and returns")
    void exitReturns() {
        String out = run();
        assertTrue(out.contains("1. Create account"));
        assertTrue(out.contains("0. Exit"));
    }

    @Test
    @DisplayName("T1: end of input ends the loop")
    void endOfInputExits() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleMenu(new ByteArrayInputStream(new byte[0]),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), repo, service).run();
        assertTrue(bytes.size() > 0);
    }

    @Test
    @DisplayName("T2: with no data files the account list is empty")
    void emptyRepository() {
        assertTrue(run("2").contains("No accounts."));
    }

    @Test
    @DisplayName("T3: creating an account stores it in memory and in accounts.csv")
    void createAccountIsSaved() throws Exception {
        String out = run("1", "RO10", "Ana Pop", "25.50", "savings");

        assertTrue(out.contains("Created account RO10"));
        assertEquals(1, repo.getAccounts().size());
        assertEquals(AccountType.SAVINGS, repo.getAccounts().get(0).getType());
        assertTrue(Files.readString(dir.resolve("data/accounts.csv")).contains("RO10"));
    }

    @Test
    @DisplayName("T4: listing prints number, owner, type and balance of each account")
    void listAccounts() throws Exception {
        account("RO01", "100");
        service.createAccount("RO02", "Bob Ion", bd("20"), AccountType.SAVINGS);

        String out = run("2");

        assertTrue(out.contains("RO01, Owner RO01, CHECKING, 100"));
        assertTrue(out.contains("RO02, Bob Ion, SAVINGS, 20"));
    }

    @Test
    @DisplayName("T5: deposit, withdrawal and transfer change balances and are saved")
    void transactionsChangeBalances() throws Exception {
        Account a = account("RO01", "100");
        Account b = account("RO02", "0");

        String out = run("3", "RO01", "50", "4", "RO01", "30", "5", "RO01", "RO02", "20");

        assertEquals(bd("100"), a.getBalance());
        assertEquals(bd("20"), b.getBalance());
        assertTrue(out.contains("Deposited 50 on RO01. Balance: 150"));
        assertEquals(3, Files.readAllLines(dir.resolve("data/transactions.csv")).stream()
                .filter(l -> !l.isBlank()).count() - headerLines());
        assertFalse(out.contains(a.getId()));
    }

    private long headerLines() throws Exception {
        return Files.readAllLines(dir.resolve("data/transactions.csv")).get(0).startsWith("id") ? 1 : 0;
    }

    @Test
    @DisplayName("T6: the transaction list is numbered from 1 and names transfer accounts")
    void listTransactionsNumbered() throws Exception {
        account("RO01", "100");
        account("RO02", "0");
        run("3", "RO01", "10", "4", "RO01", "5", "5", "RO01", "RO02", "1");

        String out = run("6", "RO01");

        assertTrue(out.contains("1. DEPOSIT 10"));
        assertTrue(out.contains("2. WITHDRAWAL 5"));
        assertTrue(out.contains("3. TRANSFER 1 from RO01 to RO02"));
    }

    @Test
    @DisplayName("T7: delete and update by transaction number adjust the balance")
    void updateAndDelete() throws Exception {
        Account a = account("RO01", "0");
        run("3", "RO01", "50", "3", "RO01", "20");
        assertEquals(bd("70"), a.getBalance());

        run("7", "RO01", "1", "10");
        assertEquals(bd("30"), a.getBalance());

        run("8", "RO01", "1");
        assertEquals(bd("20"), a.getBalance());
        assertEquals(1, repo.getTransactions().size());
    }

    @Test
    @DisplayName("T8: recent history prints at most 10 entries, newest first")
    void recentHistoryLimit() throws Exception {
        account("RO01", "0");
        for (int i = 1; i <= 12; i++) {
            run("3", "RO01", String.valueOf(i));
        }

        String out = run("9", "RO01");

        assertEquals(10, out.lines().filter(l -> l.contains("DEPOSIT ")).count());
        assertTrue(out.indexOf("DEPOSIT 12") < out.indexOf("DEPOSIT 3 "));
        assertFalse(out.contains("DEPOSIT 2 "));
    }

    @Test
    @DisplayName("T9: a withdrawal above the balance prints an error and changes nothing")
    void insufficientFunds() throws Exception {
        Account a = account("RO01", "10");

        String out = run("4", "RO01", "500");

        assertTrue(out.contains("Error: insufficient funds"));
        assertEquals(bd("10"), a.getBalance());
        assertEquals(2, out.split("=== E-Banking ===", -1).length - 1);
    }

    @Test
    @DisplayName("T10: invalid input prints a message, shows the menu again and changes nothing")
    void invalidInput() throws Exception {
        Account a = account("RO01", "10");

        assertTrue(run("3", "RO01", "abc").contains("Error: not a number: abc"));
        assertTrue(run("3", "RO99", "5").contains("Error: unknown account: RO99"));
        assertTrue(run("8", "RO01", "7").contains("Error: unknown transaction number: 7"));
        assertTrue(run("8", "RO01", "x").contains("Error: unknown transaction number: x"));
        assertTrue(run("1", "RO05", "Ana", "5", "gold").contains("Error: unknown account type: gold"));
        assertTrue(run("42").contains("Unknown choice: 42"));

        assertEquals(bd("10"), a.getBalance());
        assertEquals(0, repo.getTransactions().size());
        assertEquals(1, repo.getAccounts().size());
    }
}
