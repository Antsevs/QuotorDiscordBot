package events;

import org.jetbrains.annotations.NotNull;

import javaQuotorDiscordBot.ChannelManager;
import javaQuotorDiscordBot.QuoteManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;


/**
 * Handles interaction events from Discord slash commands.
 * Delegates channel preference management to the centralized ChannelManager.
 */
public class interactionEventListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		
		super.onSlashCommandInteraction(event);
		
		if(event.getName().equals("setchannel")) {
			TextChannel channel = getTextChannelOption(event);
			if(channel == null) {
				return;
			}

			ChannelManager.setAllChannelPreferences(event.getGuild().getIdLong(), channel.getId(), channel.getName());
			event.reply("Word and quote channels are both set to " + channel.getAsMention()).queue();
			return;
		}

		if(event.getName().equals("setchannelword")) {
			TextChannel channel = getTextChannelOption(event);
			if(channel == null) {
				return;
			}

			ChannelManager.setWordChannelPreference(event.getGuild().getIdLong(), channel.getId(), channel.getName());
			event.reply("Word-of-the-day channel set to " + channel.getAsMention()).queue();
			return;
		}

		if(event.getName().equals("setchannelquote")) {
			TextChannel channel = getTextChannelOption(event);
			if(channel == null) {
				return;
			}

			ChannelManager.setQuoteChannelPreference(event.getGuild().getIdLong(), channel.getId(), channel.getName());
			event.reply("Quote-of-the-day channel set to " + channel.getAsMention()).queue();
			return;
		}

		if(event.getName().equals("enterquote")) {
			OptionMapping option = event.getOption("quote");
			if(option == null || option.getType() != OptionType.STRING) {
				event.reply("Please include a quote to store").setEphemeral(true).queue();
				return;
			}

			String quoteText = option.getAsString().trim();
			if(quoteText.isEmpty()) {
				event.reply("Please include a quote to store").setEphemeral(true).queue();
				return;
			}

			String authorName = getDisplayName(event.getMember(), event.getUser().getName());
			QuoteManager.enqueueUserQuote(authorName, quoteText);
			event.reply("Stored your quote. It is queued for the next quote-of-the-day post. Queue size: " + QuoteManager.getQueuedQuoteCount()).setEphemeral(true).queue();
			
		}
	}

	private TextChannel getTextChannelOption(SlashCommandInteractionEvent event) {
		OptionMapping option = event.getOption("channel");
		if(option == null || option.getType() != OptionType.CHANNEL) {
			event.reply("There was no text channel provided").setEphemeral(true).queue();
			return null;
		}

		if(!(option.getAsChannel() instanceof TextChannel)) {
			event.reply("Please choose a text channel").setEphemeral(true).queue();
			return null;
		}

		return (TextChannel) option.getAsChannel();
	}

	private String getDisplayName(Member member, String fallbackName) {
		if(member != null && member.getEffectiveName() != null && !member.getEffectiveName().trim().isEmpty()) {
			return member.getEffectiveName();
		}
		return fallbackName;
	}
}
	


	

