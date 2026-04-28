package org.cts.accountservice.auditlogs;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class inherited by every auditable entity in BankCore360.
 *
 * Provides:
 *   - createdAt     : auto-set on INSERT
 *   - updatedAt     : auto-set on INSERT and UPDATE
 *   - isDeleted     : soft-delete flag (false by default)
 *   - deletedAt     : timestamp of soft deletion
 *   - deletedBy     : email of the user who performed the deletion
 *
 * Usage: extend this class in any entity that needs soft delete.
 *
 *   @Entity
 *   public class Customer extends Auditable { ... }
 *
 * Enable JPA Auditing in your main config:
 *   @EnableJpaAuditing on @SpringBootApplication or a @Configuration class.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /** Timestamp when the record was first created — never changes after INSERT. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last UPDATE — refreshed automatically by JPA Auditing. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-delete flag.
     *
     * false (default) → record is active and visible in all normal queries.
     * true            → record is logically deleted; excluded from @Query and
     *                   Spring Data derived queries via @Where filter below.
     *
     * The row is NEVER physically removed from the database.
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** Timestamp of when soft-delete was performed. Null for active records. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Email (or username) of the actor who soft-deleted this record.
     * Null for active records.
     * Useful for compliance audits — "who deleted customer X and when?"
     */
    @Column(name = "deleted_by", length = 150)
    private String deletedBy;
}