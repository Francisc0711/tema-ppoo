package ebanking.repository;

import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.DepositTransaction;
import ebanking.model.Transaction;
import ebanking.model.TransferTransaction;
import ebanking.model.WithdrawalTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankRepositoryTest {

    @Test
    @DisplayName("an added account is found by its id and an unknown id finds nothing")
    void accountIsFoundById() throws ValidationException {
        BankRepository repository = new BankRepository();
        Account account = new Account("RO01", "Ana Pop", BigDecimal.TEN, AccountType.CHECKING);
        repository.addAccount(account);

        Optional<Account> found = repository.findAccountById(account.getId());

        assertTrue(found.isPresent());
        assertSame(account, found.get());
        assertTrue(repository.findAccountById("unknown").isEmpty());
    }

    @Test
    @DisplayName("transactions of an account include incoming transfers and exclude other accounts")
    void transactionsAreFoundByAccount() throws ValidationException {
        BankRepository repository = new BankRepository();
        Transaction deposit = new DepositTransaction("acc-a", BigDecimal.TEN);
        Transaction incoming = new TransferTransaction("acc-b", "acc-a", BigDecimal.ONE);
        Transaction other = new WithdrawalTransaction("acc-b", BigDecimal.ONE);
        repository.addTransaction(deposit);
        repository.addTransaction(incoming);
        repository.addTransaction(other);

        List<Transaction> result = repository.findTransactionsByAccount("acc-a");

        assertEquals(List.of(deposit, incoming), result);
    }

    @Test
    @DisplayName("adding a second account with the same account number throws ValidationException")
    void duplicateAccountNumberIsRejected() throws ValidationException {
        BankRepository repository = new BankRepository();
        repository.addAccount(new Account("RO01", "Ana Pop", BigDecimal.TEN, AccountType.CHECKING));
        Account duplicate = new Account("RO01", "Ion Ionescu", BigDecimal.ONE, AccountType.SAVINGS);

        assertThrows(ValidationException.class, () -> repository.addAccount(duplicate));
        assertEquals(1, repository.getAccounts().size());
    }
}
