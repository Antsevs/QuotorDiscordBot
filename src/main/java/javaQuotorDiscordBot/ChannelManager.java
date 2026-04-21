package javaQuotorDiscordBot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Persists per-guild preferred channel IDs for word and quote posts.
 */
public final class ChannelManager {

    private static final String DEFAULT_WORD_CHANNEL_NAME = "word-of-the-day";
    private static final String DEFAULT_QUOTE_CHANNEL_NAME = "quote-of-the-day";
    private static final Path STORAGE_PATH = ConfigManager.getDataDirectory().resolve("channel-preferences.properties");
    private static final Properties channelPreferences = new Properties();

    static {
        loadPreferences();
    }

    private ChannelManager() {
    }

    public static synchronized void setWordChannelPreference(long guildId, String channelId, String channelName) {
        setPreference(guildId, "word", channelId, channelName);
    }

    public static synchronized void setQuoteChannelPreference(long guildId, String channelId, String channelName) {
        setPreference(guildId, "quote", channelId, channelName);
    }

    public static synchronized void setAllChannelPreferences(long guildId, String channelId, String channelName) {
        setWordChannelPreference(guildId, channelId, channelName);
        setQuoteChannelPreference(guildId, channelId, channelName);
    }

    public static synchronized Optional<String> getWordChannelPreference(long guildId) {
        return Optional.ofNullable(channelPreferences.getProperty(buildKey(guildId, "word")));
    }

    public static synchronized Optional<String> getQuoteChannelPreference(long guildId) {
        return Optional.ofNullable(channelPreferences.getProperty(buildKey(guildId, "quote")));
    }

    public static String getDefaultWordChannelName() {
        return DEFAULT_WORD_CHANNEL_NAME;
    }

    public static String getDefaultQuoteChannelName() {
        return DEFAULT_QUOTE_CHANNEL_NAME;
    }

    private static void setPreference(long guildId, String channelType, String channelId, String channelName) {
        if (channelId == null || channelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel ID cannot be empty");
        }

        channelPreferences.setProperty(buildKey(guildId, channelType), channelId.trim());
        savePreferences();
        System.out.println("Stored " + channelType + " channel for guild " + guildId + ": " + channelName + " (" + channelId + ")");
    }

    private static String buildKey(long guildId, String channelType) {
        return guildId + "." + channelType;
    }

    private static synchronized void loadPreferences() {
        channelPreferences.clear();

        if (!Files.exists(STORAGE_PATH)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(STORAGE_PATH)) {
            channelPreferences.load(inputStream);
            System.out.println("Loaded saved channel preference entries: " + channelPreferences.size());
        } catch (IOException e) {
            System.err.println("Could not load channel preferences: " + e.getMessage());
        }
    }

    private static synchronized void savePreferences() {
        try {
            Files.createDirectories(STORAGE_PATH.getParent());
            try (OutputStream outputStream = Files.newOutputStream(STORAGE_PATH)) {
                channelPreferences.store(outputStream, "Quotor channel preferences");
            }
        } catch (IOException e) {
            System.err.println("Could not save channel preferences: " + e.getMessage());
        }
    }
}
