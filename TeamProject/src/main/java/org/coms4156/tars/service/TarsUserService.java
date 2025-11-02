package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.coms4156.tars.model.TarsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@code TarsUserService} provides services related to TarsUser management.
 */
@Service 
public class TarsUserService {
    private static final Logger logger = LoggerFactory.getLogger(TarsUserService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final File userFile;
    private final AtomicLong idCounter = new AtomicLong(0);
    private List<TarsUser> users;

    /**
     * {@code TarsUserService} constructor.
     * Initializes a new instance of TarsUserService.
     * Loads user data from the JSON user database.
     */
    public TarsUserService(
        @Value("${tars.users.path:./data/users.json}") String userFilePath) {
        this.userFile = new File(userFilePath);
        ensureFile();
        this.users = load();
        // Initialize counter to max existing id
        users.stream()
            .map(TarsUser::getUserId)
            .filter(id -> id != null)
            .max(Long::compareTo)
            .ifPresent(idCounter::set);
    }

    private void ensureFile() {
        if (!userFile.exists()) {
            try {
                File parent = userFile.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                mapper.writeValue(userFile, new ArrayList<TarsUser>());
                if (logger.isInfoEnabled()) {
                    logger.info("Created user store at {}", userFile.getAbsolutePath());
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to initialize user store {}", userFile.getPath(), e);
                }
            }
        }
    }

    private synchronized List<TarsUser> load() {
        try {
            return mapper.readValue(userFile, new TypeReference<List<TarsUser>>() {});
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to read users file {}", userFile.getPath(), e);
            }
            return new ArrayList<>();
        }
    }

    private synchronized void persist() {
        File tmp = new File(userFile.getParent(), userFile.getName() + ".tmp");
        try {
            mapper.writeValue(tmp, users);
            Files.move(
                tmp.toPath(),
                userFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to persist users to {}", userFile.getPath(), e);
            }
            if (tmp.exists()) {
                tmp.delete();
            }
        }
    }

    public synchronized List<TarsUser> listUsers() {
        return Collections.unmodifiableList(new ArrayList<>(users));
    }

    public synchronized TarsUser findById(Long id) {
        if (id == null) {
            return null;
        }
        return users.stream()
            .filter(u -> id.equals(u.getUserId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * {@code createUser} Creates a new TarsUser based on provided parameters.
     *
     * @param clientId client association
     * @param username chosen username
     * @param role role string
     * @return created user or null if invalid inputs
     */
    public synchronized TarsUser createUser(Long clientId, String username, String role) {
        if (clientId == null || username == null || role == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("createUser called with null required parameters (clientId={}, username={}, role={})",
                    clientId, username, role);
            }
            return null;
        }
        long newId = idCounter.incrementAndGet();
        TarsUser user = new TarsUser(clientId, username, role);
        user.setUserId(newId);
        user.setSignUpDate(Instant.now().toString());
        user.setLastLogin("");
        user.setActive(true);

        users.add(user);
        persist();
        if (logger.isInfoEnabled()) {
            logger.info("Created TarsUser id={} clientId={} username={}", newId, clientId, username);
        }
        return user;
    }

    public synchronized boolean deactivateUser(Long id) {
        TarsUser u = findById(id);
        if (u == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("deactivateUser: user not found id={}", id);
            }
            return false;
        }
        u.setActive(false);
        persist();
        if (logger.isInfoEnabled()) {
            logger.info("User deactivated id={}", id);
        }
        return true;
    }

    public synchronized boolean updateLastLogin(Long id) {
        TarsUser u = findById(id);
        if (u == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("updateLastLogin: user not found id={}", id);
            }
            return false;
        }
        u.setLastLogin(Instant.now().toString());
        persist();
        if (logger.isInfoEnabled()) {
            logger.info("Updated lastLogin for user id={}", id);
        }
        return true;
    }
}
