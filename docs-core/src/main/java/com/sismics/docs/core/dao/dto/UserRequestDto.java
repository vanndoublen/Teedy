package com.sismics.docs.core.dao.dto;

/**
 * User request DTO.
 */
public class UserRequestDto {
    /**
     * Request ID.
     */
    private String id;

    /**
     * Username.
     */
    private String username;

    /**
     * Email.
     */
    private String email;

    /**
     * Status.
     */
    private String status;

    /**
     * Create date.
     */
    private Long createTimestamp;

    /**
     * Process date.
     */
    private Long processTimestamp;

    /**
     * Processed by username.
     */
    private String processedBy;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(Long processTimestamp) {
        this.processTimestamp = processTimestamp;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
}