package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration request entity.
 */
@Entity
@Table(name = "T_USER_REQUEST")
public class UserRequest {
    /**
     * Request ID.
     */
    @Id
    @Column(name = "USRQ_ID_C", length = 36)
    private String id;

    /**
     * Username.
     */
    @Column(name = "USRQ_USERNAME_C", nullable = false, length = 50)
    private String username;

    /**
     * Password.
     */
    @Column(name = "USRQ_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Email address.
     */
    @Column(name = "USRQ_EMAIL_C", nullable = false, length = 100)
    private String email;

    /**
     * Private key.
     */
    @Column(name = "USRQ_PRIVATEKEY_C", length = 100)
    private String privateKey;

    /**
     * Creation date.
     */
    @Column(name = "USRQ_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Request status.
     */
    @Column(name = "USRQ_STATUS_C", nullable = false, length = 10)
    private String status;

    /**
     * Process date.
     */
    @Column(name = "USRQ_PROCESSDATE_D")
    private Date processDate;

    /**
     * ID of admin who processed the request.
     */
    @Column(name = "USRQ_PROCESSEDBY_C", length = 36)
    private String processedBy;

    /**
     * Locale ID.
     */
    @Column(name = "USRQ_IDLOCALE_C", length = 10)
    private String localeId;

    // Getters and setters with fluent pattern like in User.java
    public String getId() {
        return id;
    }

    public UserRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public UserRequest setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public UserRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public UserRequest setProcessDate(Date processDate) {
        this.processDate = processDate;
        return this;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public UserRequest setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
        return this;
    }

    public String getLocaleId() {
        return localeId;
    }

    public UserRequest setLocaleId(String localeId) {
        this.localeId = localeId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .add("status", status)
                .toString();
    }
}