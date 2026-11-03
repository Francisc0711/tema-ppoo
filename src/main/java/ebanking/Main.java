package ebanking;

import ebanking.repository.BankRepository;
import ebanking.storage.CsvBankStorage;
import ebanking.storage.StorageException;

import java.nio.file.Path;

/** Entry point of the e-banking application. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        CsvBankStorage storage = new CsvBankStorage(Path.of("data"));
        try {
            BankRepository repository = storage.load();
            System.out.println("Loaded " + repository.getAccounts().size() + " accounts and "
                    + repository.getTransactions().size() + " transactions from data/");
        } catch (StorageException e) {
            System.err.println("Cannot load data: " + e.getMessage());
        }
        // TODO(TD-app-foundation-1): replace with the console menu (cli-menu)
        System.out.println("E-Banking app - console menu coming in cli-menu");
    }
}
