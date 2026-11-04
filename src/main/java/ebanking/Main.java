package ebanking;

import ebanking.cli.ConsoleMenu;
import ebanking.repository.BankRepository;
import ebanking.service.TransactionService;
import ebanking.storage.CsvBankStorage;
import ebanking.storage.StorageException;

import java.nio.file.Path;

/** Entry point of the e-banking application. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        CsvBankStorage storage = new CsvBankStorage(Path.of("data"));
        BankRepository repository;
        try {
            repository = storage.load(); // empty when the files are missing
        } catch (StorageException e) {
            // do not run on a broken file: the first save would overwrite it
            System.err.println("Cannot load data: " + e.getMessage());
            return;
        }
        new ConsoleMenu(System.in, System.out, repository, new TransactionService(repository, storage)).run();
    }
}
