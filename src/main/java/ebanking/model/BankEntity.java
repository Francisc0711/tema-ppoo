package ebanking.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Common base of every stored entity: a unique id and a creation time. */
public abstract class BankEntity {

    private final String id;
    private final LocalDateTime createdAt;

    protected BankEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
