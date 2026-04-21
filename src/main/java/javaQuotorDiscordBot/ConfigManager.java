package javaQuotorDiscordBot;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages configuration and environment variables for the Discord bot.
 * Loads sensitive data like API tokens from environment variables rather than hardcoding them.
 */
public class ConfigManager {
    
    private static final String DISCORD_TOKEN_ENV = "DISCORD_TOKEN";
    private static final String BOT_DATA_DIR_ENV = "BOT_DATA_DIR";
    
    /**
     * Retrieves the Discord bot token from environment variables.
     * Throws an exception if the token is not set.
     * 
     * @return The Discord bot token
     * @throws IllegalStateException if the token environment variable is not set
     */
    public static String getDiscordToken() {
        String token = System.getenv(DISCORD_TOKEN_ENV);
        
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException(
                "Discord token not found. Please set the " + DISCORD_TOKEN_ENV + 
                " environment variable. See .env.example for instructions."
            );
        }
        
        return token;
    }

    public static Path getDataDirectory() {
        String configuredPath = System.getenv(BOT_DATA_DIR_ENV);
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            return Paths.get("bot-data");
        }
        return Paths.get(configuredPath.trim());
    }
    
    /**
     * Validates that all required configuration is present.
     * 
     * @throws IllegalStateException if any required configuration is missing
     */
    public static void validateConfig() {
        try {
            getDiscordToken();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Configuration validation failed: " + e.getMessage());
        }
    }
}
