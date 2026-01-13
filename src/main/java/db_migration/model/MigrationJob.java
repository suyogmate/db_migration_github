package db_migration.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "migration_job")
public class MigrationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sourceDbId;
    private Long targetDbId;
    private String status;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Column(length = 4000)
    private String errorMessage;

    // ✅ REQUIRED GETTER
    public Long getId() {
        return id;
    }

    // setters & getters
    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceDbId() {
        return sourceDbId;
    }

    public void setSourceDbId(Long sourceDbId) {
        this.sourceDbId = sourceDbId;
    }

    public Long getTargetDbId() {
        return targetDbId;
    }

    public void setTargetDbId(Long targetDbId) {
        this.targetDbId = targetDbId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}