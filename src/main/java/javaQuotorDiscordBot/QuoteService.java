package javaQuotorDiscordBot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves a quote from the queued user submissions first, then falls back to an online API.
 */
public final class QuoteService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"q\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("\"a\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final String TODAY_QUOTE_URL = "https://zenquotes.io/api/today";
    private static final String RANDOM_QUOTE_URL = "https://zenquotes.io/api/random";

    private QuoteService() {
    }

    public static QuoteContent getQuoteOfTheDay() {
        Optional<QuoteContent> queuedQuote = QuoteManager.pollUserQuote();
        if (queuedQuote.isPresent()) {
            return queuedQuote.get();
        }

        Optional<QuoteContent> onlineQuote = fetchOnlineQuote(TODAY_QUOTE_URL);
        if (onlineQuote.isPresent()) {
            return onlineQuote.get();
        }

        onlineQuote = fetchOnlineQuote(RANDOM_QUOTE_URL);
        if (onlineQuote.isPresent()) {
            return onlineQuote.get();
        }

        System.err.println("Falling back to built-in quote because online quote retrieval failed");
        return new QuoteContent(
            "Small daily steps still count as progress.",
            "Quotor",
            "built-in fallback"
        );
    }

    private static Optional<QuoteContent> fetchOnlineQuote(String url) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .header("User-Agent", "javaQuotorDiscordBot/1.0")
            .GET()
            .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Quote API response from " + url + ": HTTP " + response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            String quoteText = extractJsonValue(QUOTE_PATTERN, response.body());
            String author = extractJsonValue(AUTHOR_PATTERN, response.body());

            if (quoteText == null || author == null) {
                System.err.println("Could not parse quote payload from " + url);
                return Optional.empty();
            }

            return Optional.of(new QuoteContent(unescapeJson(quoteText), unescapeJson(author), "online"));
        } catch (IOException | InterruptedException e) {
            System.err.println("Could not retrieve online quote from " + url + ": " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }

    private static String extractJsonValue(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private static String unescapeJson(String value) {
        return value
            .replace("\\\"", "\"")
            .replace("\\n", " ")
            .replace("\\r", " ")
            .replace("\\t", " ")
            .replace("\\\\", "\\");
    }
}
