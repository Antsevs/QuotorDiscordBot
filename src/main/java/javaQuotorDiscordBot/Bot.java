package javaQuotorDiscordBot;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import events.interactionEventListener;

public class Bot extends ListenerAdapter {
	
	public static void main(String[] args) throws LoginException, InterruptedException{
		
		// Load Discord token from environment variable for security
		ConfigManager.validateConfig();
		final String token = ConfigManager.getDiscordToken();
		
		JDA jda = JDABuilder.createDefault(token)

				.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
				.addEventListeners(new Bot(), new interactionEventListener())
				.build();
		
		jda.getPresence().setActivity(Activity.watching("Ant code"));
		
		schedulePost(jda);
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("The bot is ready");
		System.out.println("Queued user quotes currently stored: " + QuoteManager.getQueuedQuoteCount());
		event.getJDA().getGuilds().forEach(guild -> {
			registerGuildCommands(guild);
			postDailyContent(guild);
		});
	}

	private static void registerGuildCommands(Guild guild) {
		guild.updateCommands()
			.addCommands(
				Commands.slash("setchannel", "Sets both the word and quote channels for Quotor")
					.addOption(OptionType.CHANNEL, "channel", "preferred text channel for Quotor", true),
				Commands.slash("setchannelword", "Sets the preferred channel for word-of-the-day posts")
					.addOption(OptionType.CHANNEL, "channel", "preferred text channel for word posts", true),
				Commands.slash("setchannelquote", "Sets the preferred channel for quote-of-the-day posts")
					.addOption(OptionType.CHANNEL, "channel", "preferred text channel for quote posts", true),
				Commands.slash("enterquote", "Adds a user-submitted quote to the posting queue")
					.addOption(OptionType.STRING, "quote", "quote text to store and post later", true)
			)
			.queue(
				success -> System.out.println("Synced slash commands for guild " + guild.getName()),
				error -> System.err.println("Could not sync slash commands for guild " + guild.getName() + ": " + error.getMessage())
			);
	}
	
	public static void postDailyContent(Guild guild) {
		TextChannel wordChannel = resolveWordChannel(guild);
		if(wordChannel != null) {
			postWOD(wordChannel);
		}

		TextChannel quoteChannel = resolveQuoteChannel(guild);
		if(quoteChannel != null) {
			postQuoteOfTheDay(quoteChannel);
		}
	}
	
	public static void postWOD(TextChannel channel) {
		String wordOfTheDay = WebScraper.getWordOfTheDay();
		channel.sendMessage(wordOfTheDay).queue(
			success -> System.out.println("Posted word of the day to #" + channel.getName() + " in guild " + channel.getGuild().getName()),
			error -> System.err.println("Could not post word of the day to #" + channel.getName() + ": " + error.getMessage())
		);
	}

	public static void postQuoteOfTheDay(TextChannel channel) {
		QuoteContent quote = QuoteService.getQuoteOfTheDay();
		String message = formatQuoteMessage(quote);
		channel.sendMessage(message).queue(
			success -> System.out.println("Posted " + quote.getSource() + " quote to #" + channel.getName() + " in guild " + channel.getGuild().getName()),
			error -> System.err.println("Could not post quote of the day to #" + channel.getName() + ": " + error.getMessage())
		);
	}

	private static String formatQuoteMessage(QuoteContent quote) {
		String blockQuotedText = quote.getText().replace("\r", "").replace("\n", "\n> ");
		return "**Quote of the day**\n> " + blockQuotedText + "\n- ***" + quote.getAuthor() + "***";
	}

	private static TextChannel resolveWordChannel(Guild guild) {
		return resolveChannel(
			guild,
			ChannelManager.getWordChannelPreference(guild.getIdLong()),
			ChannelManager.getDefaultWordChannelName(),
			"word"
		);
	}

	private static TextChannel resolveQuoteChannel(Guild guild) {
		return resolveChannel(
			guild,
			ChannelManager.getQuoteChannelPreference(guild.getIdLong()),
			ChannelManager.getDefaultQuoteChannelName(),
			"quote"
		);
	}

	private static TextChannel resolveChannel(Guild guild, java.util.Optional<String> channelPreference, String defaultChannelName, String channelType) {
		if(channelPreference.isPresent()) {
			String channelId = channelPreference.get();
			TextChannel configuredChannel = guild.getTextChannelById(channelId);
			if(configuredChannel != null) {
				return configuredChannel;
			}
			System.err.println("Saved " + channelType + " channel ID " + channelId + " was not found in guild " + guild.getName());
		}

		if(!guild.getTextChannelsByName(defaultChannelName, true).isEmpty()) {
			return guild.getTextChannelsByName(defaultChannelName, true).get(0);
		}

		TextChannel systemChannel = guild.getSystemChannel();
		if(systemChannel != null) {
			System.out.println("Using system channel #" + systemChannel.getName() + " for " + channelType + " posts in guild " + guild.getName());
			return systemChannel;
		}

		if(!guild.getTextChannels().isEmpty()) {
			TextChannel firstChannel = guild.getTextChannels().get(0);
			System.out.println("Using first available text channel #" + firstChannel.getName() + " for " + channelType + " posts in guild " + guild.getName());
			return firstChannel;
		}

		System.err.println("No text channel available for " + channelType + " posts in guild " + guild.getName());
		return null;
	}

	public static void schedulePost(JDA jda) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
            jda.getGuilds().forEach(Bot::postDailyContent);
        }, 24, 24, TimeUnit.HOURS);
    }
}