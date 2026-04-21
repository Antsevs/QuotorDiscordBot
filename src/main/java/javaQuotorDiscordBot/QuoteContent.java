package javaQuotorDiscordBot;

/**
 * Represents a quote ready to post.
 */
public class QuoteContent {

    private final String text;
    private final String author;
    private final String source;

    public QuoteContent(String text, String author, String source) {
        this.text = text;
        this.author = author;
        this.source = source;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }
}
