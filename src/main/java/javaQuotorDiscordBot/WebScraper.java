package javaQuotorDiscordBot;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Handles web scraping for retrieving word-of-the-day definitions.
 * Separated from main Bot class for better organization and testability.
 */
public class WebScraper {
    
    private static final String MERRIAM_WEBSTER_URL = "https://www.merriam-webster.com/word-of-the-day";
    private static final String WORD_SELECTOR = ".word-header .word-and-pronunciation .word-header-txt";
    private static final String DEFINITION_SELECTOR = ".wod-article-container .wod-definition-container p";
    
    /**
     * Retrieves the word of the day from Merriam-Webster website.
     * 
     * @return Formatted string containing the word and definition for Discord
     */
    public static String getWordOfTheDay() {
        try {
            Connection.Response response = Jsoup.connect(MERRIAM_WEBSTER_URL).execute();
            int statusCode = response.statusCode();
            System.out.println("Merriam-Webster response: HTTP " + statusCode);
            Document doc = response.parse();
            
            // Extract word and definition
            Element wordElement = doc.selectFirst(WORD_SELECTOR);
            Element definitionElement = doc.selectFirst(DEFINITION_SELECTOR);
            
            if (wordElement == null || definitionElement == null) {
                return "Could not parse word of the day";
            }
            
            String wordOfTheDay = wordElement.text();
            String definition = definitionElement.text();
            
            // Format for Discord message
            return "**Word of the day:** **" + wordOfTheDay + "** - " + definition;
        } catch (IOException e) {
            System.err.println("Error fetching word of the day: " + e.getMessage());
            e.printStackTrace();
            return "Could not find word of the day";
        }
    }
}
