package javaQuotorDiscordBot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to help set up the bot environment.
 * Provides instructions for configuring the Discord token.
 */
public class EnvSetupHelper {
    
    /**
     * Prints setup instructions to help users configure the bot.
     */
    public static void printSetupInstructions() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════╗\n" +
            "║            DISCORD BOT SETUP INSTRUCTIONS                 ║\n" +
            "╠════════════════════════════════════════════════════════════╣\n" +
            "║                                                            ║\n" +
            "║ To run the bot, set your Discord token as an environment  ║\n" +
            "║ variable before running the application:                  ║\n" +
            "║                                                            ║\n" +
            "║ LINUX/MAC:                                                ║\n" +
            "║   export DISCORD_TOKEN='your_bot_token_here'              ║\n" +
            "║   java -jar target/javaQuotorDiscordBot.jar               ║\n" +
            "║                                                            ║\n" +
            "║ WINDOWS (Command Prompt):                                 ║\n" +
            "║   set DISCORD_TOKEN=your_bot_token_here                   ║\n" +
            "║   java -jar target/javaQuotorDiscordBot.jar               ║\n" +
            "║                                                            ║\n" +
            "║ WINDOWS (PowerShell):                                     ║\n" +
            "║   $env:DISCORD_TOKEN='your_bot_token_here'                ║\n" +
            "║   java -jar target/javaQuotorDiscordBot.jar               ║\n" +
            "║                                                            ║\n" +
            "║ ALTERNATIVE - Create .env file:                           ║\n" +
            "║   1. Copy .env.example to .env in the project root        ║\n" +
            "║   2. Edit .env and add your bot token                     ║\n" +
            "║   3. Run: source .env (Linux/Mac) or Create .env          ║\n" +
            "║                                                            ║\n" +
            "║ NEVER commit your .env file to version control!           ║\n" +
            "║ .env is included in .gitignore for security.              ║\n" +
            "║                                                            ║\n" +
            "╚════════════════════════════════════════════════════════════╝\n"
        );
    }
    
    /**
     * Writes a sample .env file with instructions.
     * Only creates if it doesn't already exist.
     */
    public static void createSampleEnvFile() {
        Path envPath = Paths.get(".env");
        Path envExamplePath = Paths.get(".env.example");
        
        try {
            if (!Files.exists(envPath)) {
                String content = "# Discord Bot Configuration\n" +
                                "# Add your Discord bot token below\n" +
                                "DISCORD_TOKEN=your_bot_token_here\n";
                Files.write(envPath, content.getBytes());
                System.out.println("Created .env file. Please add your Discord token.");
            }
        } catch (IOException e) {
            System.err.println("Could not create .env file: " + e.getMessage());
        }
    }
}
