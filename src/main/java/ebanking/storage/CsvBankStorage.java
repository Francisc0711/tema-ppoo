package ebanking.storage;

import ebanking.exception.ValidationException;
import ebanking.model.Account;
import ebanking.model.AccountType;
import ebanking.model.DepositTransaction;
import ebanking.model.Transaction;
import ebanking.model.TransactionType;
import ebanking.model.TransferTransaction;
import ebanking.model.WithdrawalTransaction;
import ebanking.repository.BankRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Saves and loads accounts and transactions as CSV files in one directory. */
public class CsvBankStorage {

    static final String ACCOUNTS_FILE = "accounts.csv";
    static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final List<String> ACCOUNT_HEADER =
            List.of("id", "createdAt", "accountNumber", "ownerName", "balance", "type");
    private static final List<String> TRANSACTION_HEADER =
            List.of("id", "createdAt", "type", "accountId", "targetAccountId", "amount");

    private final Path directory;

    public CsvBankStorage(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("directory is required");
        }
        this.directory = directory;
    }

    // TODO(TD-file-persistence-1): write to a temporary file and move it, so a crash cannot truncate data
    public void save(BankRepository repository) throws StorageException {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new StorageException("cannot write " + directory, e);
        }
        try (BufferedWriter out = open(ACCOUNTS_FILE)) {
            writeRecord(out, ACCOUNT_HEADER);
            for (Account a : repository.getAccounts()) {
                writeRecord(out, List.of(a.getId(), a.getCreatedAt().toString(), a.getAccountNumber(),
                        a.getOwnerName(), a.getBalance().toPlainString(), a.getType().name()));
            }
        } catch (IOException e) {
            throw new StorageException("cannot write " + ACCOUNTS_FILE, e);
        }
        try (BufferedWriter out = open(TRANSACTIONS_FILE)) {
            writeRecord(out, TRANSACTION_HEADER);
            for (Transaction t : repository.getTransactions()) {
                String target = t instanceof TransferTransaction transfer ? transfer.getTargetAccountId() : "";
                writeRecord(out, List.of(t.getId(), t.getCreatedAt().toString(), t.getType().name(),
                        t.getAccountId(), target, t.getAmount().toPlainString()));
            }
        } catch (IOException e) {
            throw new StorageException("cannot write " + TRANSACTIONS_FILE, e);
        }
    }

    public BankRepository load() throws StorageException {
        BankRepository repository = new BankRepository();
        readFile(ACCOUNTS_FILE, ACCOUNT_HEADER, fields -> readAccount(fields, repository));
        readFile(TRANSACTIONS_FILE, TRANSACTION_HEADER, fields -> readTransaction(fields, repository));
        return repository;
    }

    private BufferedWriter open(String file) throws IOException {
        return Files.newBufferedWriter(directory.resolve(file), StandardCharsets.UTF_8);
    }

    private static void writeRecord(BufferedWriter out, List<String> fields) throws IOException {
        out.write(CsvCodec.format(fields));
        out.write("\n");
    }

    /** Handles one parsed record. */
    private interface RecordHandler {
        void handle(List<String> fields) throws ValidationException;
    }

    private void readFile(String file, List<String> header, RecordHandler handler) throws StorageException {
        Path path = directory.resolve(file);
        if (Files.notExists(path)) {
            return;
        }
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            int[] lines = {0};
            List<String> first = CsvCodec.readRecord(in, lines);
            if (first == null) {
                return;   // empty file: nothing stored
            }
            if (!first.equals(header)) {
                throw invalid(file, 1, "unexpected header", null);
            }
            while (true) {
                int start = lines[0] + 1;
                List<String> fields = CsvCodec.readRecord(in, lines);
                if (fields == null) {
                    return;
                }
                if (fields.size() == 1 && fields.get(0).isEmpty()) {
                    continue;   // blank line
                }
                try {
                    handler.handle(fields);
                } catch (ValidationException | DateTimeParseException | IllegalArgumentException e) {
                    throw invalid(file, start, e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + file, e);
        }
    }

    private static StorageException invalid(String file, int line, String reason, Exception cause) {
        return new StorageException(file + " line " + line + ": " + reason, cause);
    }

    private static void requireFields(List<String> fields) throws ValidationException {
        if (fields.size() != 6) {
            throw new ValidationException("expected 6 fields, found " + fields.size());
        }
    }

    private static void readAccount(List<String> f, BankRepository repository) throws ValidationException {
        requireFields(f);
        repository.addAccount(new Account(f.get(0), LocalDateTime.parse(f.get(1)), f.get(2), f.get(3),
                new BigDecimal(f.get(4)), AccountType.valueOf(f.get(5))));
    }

    private static void readTransaction(List<String> f, BankRepository repository) throws ValidationException {
        requireFields(f);
        LocalDateTime createdAt = LocalDateTime.parse(f.get(1));
        TransactionType type = TransactionType.valueOf(f.get(2));
        String accountId = f.get(3);
        String targetId = f.get(4);
        BigDecimal amount = new BigDecimal(f.get(5));
        Account source = find(repository, accountId);
        Transaction transaction;
        switch (type) {
            case DEPOSIT -> transaction = new DepositTransaction(f.get(0), createdAt, accountId, amount);
            case WITHDRAWAL -> transaction = new WithdrawalTransaction(f.get(0), createdAt, accountId, amount);
            default -> transaction = new TransferTransaction(f.get(0), createdAt, accountId,
                    targetId, amount);
        }
        if (type != TransactionType.TRANSFER && !targetId.isEmpty()) {
            throw new ValidationException("targetAccountId must be empty for " + type);
        }
        repository.addTransaction(transaction);
        source.addToHistory(transaction);
        if (transaction instanceof TransferTransaction transfer) {
            find(repository, transfer.getTargetAccountId()).addToHistory(transaction);
        }
    }

    private static Account find(BankRepository repository, String id) throws ValidationException {
        return repository.findAccountById(id)
                .orElseThrow(() -> new ValidationException("unknown account: " + id));
    }
}
