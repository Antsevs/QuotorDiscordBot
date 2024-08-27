package javaQuotorDiscordBot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.Route.Channels;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Bot extends ListenerAdapter {
	
	public static void main(String[] args) throws LoginException, InterruptedException{
		
		JDA jda = JDABuilder.createDefault("enterdiscordtokenhere")

				.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
				.addEventListeners(new Bot())
				.build();
		
		schedulePost(jda);
	}
	
	public void onReady(ReadyEvent event) {
		
		System.out.println("The bot is ready");
		System.out.println(event.getJDA().getToken());
	}
	
	//@Override
	//public void onMessageReceived(MessageReceivedEvent event) {  
	public static void postWOD(JDA jda) {
		//notifies if user has sent a message in the server
		/*if(event.getAuthor().isBot())
				return;
		else
			System.out.println("A user sent a message");
		
		*/
		//if(event.getMessage().getContentRaw().equalsIgnoreCase("!wordoftheday")) {
			
		String wordOfTheDay = getWordOfTheDay(); //implement web scraping
		String channelName = "👂word-of-the-day👂";
		
		//List<TextChannel> channels = event.getGuild().getTextChannelsByName(channelName, true);
		List<TextChannel> channels = jda.getTextChannelsByName(channelName, true);
		//TextChannel channel = event.getTextChannel(/*"👂word-of-the-day👂"*/); 
		if(!channels.isEmpty()) {
			TextChannel channel = channels.get(0);
			channel.sendMessage("The word of the day is" + wordOfTheDay).queue();
		} else {
			System.err.println("Error 404: Channel Not Found");
		}

	}

	
	public static void schedulePost(JDA jda) {
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
		//long initialDelay = calcualteInitialDelay(hour, minute);
		
		scheduler.scheduleAtFixedRate(() -> postWOD(jda), 0, 24, TimeUnit.HOURS);
		
	}
	
	private static String getWordOfTheDay() {
		try {
			Document doc = Jsoup.connect("https://www.merriam-webster.com/word-of-the-day").get();
			
			Element wordElement = doc.selectFirst(".word-header .word-and-pronunciation .word-header-txt");
			Element definitionElement = doc.selectFirst(".wod-article-container .wod-definition-container p");
			
			String wordOfTheDay = wordElement.text();
			String definition = definitionElement.text();
			
			return " **" + wordOfTheDay + "**" + " - " + definition;
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not find word of the day";
		}
	}
}
