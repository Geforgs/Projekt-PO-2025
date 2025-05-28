package po25;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public abstract class AbstractPlatform implements Platform {

    protected final String baseApiUrl;
    protected Optional<String> sessionToken = Optional.empty();

    private final Path sessionFilePath;

    private static final String APP_CONFIG_DIR_NAME = ".dccp_cli";

    /**
     * Initializes the platform, sets up the configuration directory, and attempts
     * to load and validate an existing session from a local file.
     *
     * @param platformSpecificBaseUrl The base URL for the platform's API.
     */
    public AbstractPlatform(String platformSpecificBaseUrl) {
        this.baseApiUrl = platformSpecificBaseUrl;

        Path configDir = Paths.get(System.getProperty("user.home"), APP_CONFIG_DIR_NAME);
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            System.err.println("Warning: Could not create config directory: " + configDir + ". Session persistence will be disabled. (" + e.getMessage() + ")");
        }

        this.sessionFilePath = configDir.resolve(getPlatformName().toLowerCase() + ".session");
        initializeSessionFromFile();
    }

    /**
     * Performs the platform-specific login operation.
     *
     * @param username The user's username.
     * @param password The user's password.
     * @return A valid session token upon successful login.
     * @throws PlatformException if the login fails.
     */
    protected abstract String performPlatformLogin(String username, char[] password) throws PlatformException;

    /**
     * Validates a given session token with the platform's server.
     *
     * @param token The token to validate.
     * @return true if the token is valid, false otherwise.
     * @throws PlatformException if an error occurs during validation.
     */
    protected abstract boolean validateTokenWithServer(String token) throws PlatformException;

    /**
     * Attempts to load a session token from the local file and validate it with the server.
     * If the token is invalid or the file is corrupted, the session state is cleared.
     */
    private void initializeSessionFromFile() {
        if (!Files.exists(sessionFilePath)) {
            return;
        }

        try {
            String tokenFromFile = Files.readString(sessionFilePath).trim();

            if (tokenFromFile.isEmpty()) {
                System.err.println(getPlatformName() + ": Session file is empty. Deleting it.");
                deleteSessionFile();
                return;
            }

            if (validateTokenWithServer(tokenFromFile)) {
                this.sessionToken = Optional.of(tokenFromFile);
                System.out.println(getPlatformName() + ": Session restored and validated successfully.");
            } else {
                System.err.println(getPlatformName() + ": Existing session token is invalid. Please log in again.");
                deleteSessionFile();
            }
        } catch (IOException | PlatformException e) {
            System.err.println(getPlatformName() + ": Failed to load or validate session token. Reason: " + e.getMessage());
            clearSessionStateAndFile();
        }
    }

    @Override
    public void login(String username, char[] password) throws PlatformException {
        String newToken = performPlatformLogin(username, password);

        if (newToken == null || newToken.isBlank()) {
            clearSessionStateAndFile();
            throw new PlatformException("Login failed for " + getPlatformName() + ": The platform returned an empty token.");
        }

        saveSessionTokenToFile(newToken);
        this.sessionToken = Optional.of(newToken);
        System.out.println(getPlatformName() + ": Successfully logged in and session saved.");
    }

    @Override
    public void logout() {
        clearSessionStateAndFile();
        System.out.println(getPlatformName() + ": Logged out and session cleared.");
    }

    /**
     * Checks if the user is currently authenticated.
     * This is a quick, local check on whether a session token is present in memory.
     *
     * @return true if a session token exists, false otherwise.
     */
    public boolean isAuthenticated() {
        return this.sessionToken.isPresent();
    }

    /*
    @Override
    public boolean isSessionValid() throws PlatformException {
        return sessionToken.isPresent() && validateTokenWithServer(sessionToken.get());
    }
    */

    /**
     * A helper method for API calls to ensure the user is logged in before proceeding.
     *
     * @return The current session token.
     * @throws PlatformException if the user is not logged in.
     */
    protected String getRequiredToken() throws PlatformException {
        return this.sessionToken.orElseThrow(() ->
                new PlatformException("Not logged in to " + getPlatformName() + ". Please login first.")
        );
    }

    private void saveSessionTokenToFile(String token) throws PlatformException {
        try {
            Files.writeString(sessionFilePath, token, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new PlatformException("Failed to save session token for " + getPlatformName() + ": " + e.getMessage(), e);
        }
    }

    private void deleteSessionFile() throws PlatformException {
        try {
            Files.deleteIfExists(sessionFilePath);
        } catch (IOException e) {
            throw new PlatformException("Failed to delete session token file for " + getPlatformName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Clears the in-memory session token and deletes the session file from disk.
     */
    private void clearSessionStateAndFile() {
        this.sessionToken = Optional.empty();
        try {
            deleteSessionFile();
        } catch (PlatformException e) {
            System.err.println(getPlatformName() + ": Error during session file cleanup: " + e.getMessage());
        }
    }
}
