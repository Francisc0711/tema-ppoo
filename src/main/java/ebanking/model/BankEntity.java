package ebanking.model;

import ebanking.exception.ValidationException;

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

    /** Restores an entity with a stored id and creation time. */
    protected BankEntity(String id, LocalDateTime createdAt) throws ValidationException {
        if (id == null || id.isBlank()) {
            throw new ValidationException("id is required");
        }
        if (createdAt == null) {
            throw new ValidationException("createdAt is required");
        }
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
