package javaQuotorDiscordBot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Stores user-submitted quotes in a simple FIFO queue on disk.
 */
public final class QuoteManager {

    private static final Path STORAGE_PATH = ConfigManager.getDataDirectory().resolve("user-quotes.tsv");
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private QuoteManager() {
    }

    public static synchronized void enqueueUserQuote(String author, String quoteText) {
        StoredQuote storedQuote = new StoredQuote(
            Instant.now().toString(),
            encode(author),
            encode(quoteText)
        );

        try {
            Files.createDirectories(STORAGE_PATH.getParent());
            Files.write(
                STORAGE_PATH,
                List.of(storedQuote.toLine()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            System.out.println("Queued user quote from " + author);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save quote submission", e);
        }
    }

    public static synchronized Optional<QuoteContent> pollUserQuote() {
        List<StoredQuote> storedQuotes = readStoredQuotes();
        if (storedQuotes.isEmpty()) {
            System.out.println("No queued user quotes found in " + STORAGE_PATH);
            return Optional.empty();
        }

        StoredQuote nextQuote = storedQuotes.remove(0);
        String author;
        String quoteText;
        try {
            author = decode(nextQuote.authorBase64);
            quoteText = decode(nextQuote.quoteBase64);
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping unreadable queued quote entry: " + e.getMessage());
            writeStoredQuotes(storedQuotes);
            return Optional.empty();
        }

        writeStoredQuotes(storedQuotes);
        System.out.println("Posting queued user quote from " + author + " and removing it from storage");
        return Optional.of(new QuoteContent(quoteText, author, "user submission"));
    }

    public static synchronized int getQueuedQuoteCount() {
        return readStoredQuotes().size();
    }

    private static List<StoredQuote> readStoredQuotes() {
        List<StoredQuote> storedQuotes = new ArrayList<>();
        if (!Files.exists(STORAGE_PATH)) {
            return storedQuotes;
        }

        try {
            List<String> lines = Files.readAllLines(STORAGE_PATH, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = DELIMITER_PATTERN.split(line.trim(), 3);
                if (parts.length != 3) {
                    System.err.println("Skipping malformed stored quote entry: " + line);
                    continue;
                }
                storedQuotes.add(new StoredQuote(parts[0], parts[1], parts[2]));
            }
            System.out.println("Loaded queued user quotes: " + storedQuotes.size());
        } catch (IOException e) {
            System.err.println("Could not read stored quotes: " + e.getMessage());
        }

        return storedQuotes;
    }

    private static void writeStoredQuotes(List<StoredQuote> storedQuotes) {
        List<String> lines = new ArrayList<>();
        for (StoredQuote storedQuote : storedQuotes) {
            lines.add(storedQuote.toLine());
        }

        try {
            Files.createDirectories(STORAGE_PATH.getParent());
            Files.write(
                STORAGE_PATH,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("Could not update stored quotes: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static final class StoredQuote {
        private final String createdAt;
        private final String authorBase64;
        private final String quoteBase64;

        private StoredQuote(String createdAt, String authorBase64, String quoteBase64) {
            this.createdAt = createdAt;
            this.authorBase64 = authorBase64;
            this.quoteBase64 = quoteBase64;
        }

        private String toLine() {
            return createdAt + "\t" + authorBase64 + "\t" + quoteBase64;
        }
    }
}
