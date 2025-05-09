package com.sismics.docs.core.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRequestDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class UserRequestService  {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserRequestService.class);

    private final UserRequestDao userRequestDao;
    private final UserDao userDao;

    public UserRequestService() {
        this.userRequestDao = new UserRequestDao();
        this.userDao = new UserDao();
    }

    /**
     * Hash the password using BCrypt.
     */
    private String hashPassword(String password) {
        int bcryptWork = Constants.DEFAULT_BCRYPT_WORK;
        String envBcryptWork = System.getenv(Constants.BCRYPT_WORK_ENV);
        if (!Strings.isNullOrEmpty(envBcryptWork)) {
            try {
                int envBcryptWorkInt = Integer.parseInt(envBcryptWork);
                if (envBcryptWorkInt >= 4 && envBcryptWorkInt <= 31) {
                    bcryptWork = envBcryptWorkInt;
                } else {
                    log.warn(Constants.BCRYPT_WORK_ENV + " needs to be in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
                }
            } catch (NumberFormatException e) {
                log.warn(Constants.BCRYPT_WORK_ENV + " needs to be a number in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
            }
        }
        return BCrypt.withDefaults().hashToString(bcryptWork, password.toCharArray());
    }

    public UserRequest createRequest(String username, String password, String email) {
        // Check for existing pending request
        UserRequest existingRequest = userRequestDao.getActiveByUsername(username);
        if (existingRequest != null) {
            throw new IllegalStateException("AlreadyExistingRequest");
        }

        // Check for existing user
        if (userDao.getActiveByUsername(username) != null) {
            throw new IllegalStateException("AlreadyExistingUsername");
        }

        // Create the request with hashed password
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setPassword(hashPassword(password));
        userRequest.setEmail(email);
        userRequest.setLocaleId("en");

        return userRequestDao.create(userRequest);
    }

    public UserRequest approve(String requestId, String processedById) {
        UserRequest userRequest = userRequestDao.getById(requestId);
        if (userRequest == null || !"PENDING".equals(userRequest.getStatus())) {
            throw new IllegalStateException("InvalidRequest");
        }

        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword()); // Password is already hashed
        user.setEmail(userRequest.getEmail());
        user.setOnboarding(true);
        try {
            userDao.create(user, processedById);
        } catch (Exception e) {
            log.error("Error while creating the internal user", e);
            return null;
        }

        // Update request status
        userRequest.setStatus("APPROVED");
        userRequest.setProcessDate(new Date());
        userRequest.setProcessedBy(processedById);
        return userRequestDao.update(userRequest);
    }

    public UserRequest reject(String requestId, String processedById) {
        UserRequest userRequest = userRequestDao.getById(requestId);
        if (userRequest == null || !"PENDING".equals(userRequest.getStatus())) {
            throw new IllegalStateException("InvalidRequest");
        }

        userRequest.setStatus("REJECTED");
        userRequest.setProcessDate(new Date());
        userRequest.setProcessedBy(processedById);
        return userRequestDao.update(userRequest);
    }
}