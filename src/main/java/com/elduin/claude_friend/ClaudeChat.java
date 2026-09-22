package com.elduin.claude_friend;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.elduin.claude_friend.entity.ClaudeEntities;
import com.elduin.claude_friend.entity.ClaudeEntity;
import com.elduin.claude_friend.net.Speak;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Claude listens to chat. Say "claude" and he comes. Once he's near you he
 * hears everything, and does whatever he knows how to do.
 */
public final class ClaudeChat {

	/** Claude hears you without his name if he's this close. */
	private static final double HEARING = 24.0;
	/** An unowned Claude this close will become yours. */
	private static final double ADOPT = 16.0;

	private static final String HELP = "I can follow you, stay, come here, make it day or night, "
			+ "make it rain or sunny, give you food or diamonds, heal you, and jump!";

	private ClaudeChat() {
	}

	public static void register() {
		//? if >=26.2 {
		/*PayloadTypeRegistry.clientboundPlay().register(Speak.TYPE, Speak.CODEC);
		*///?} else {
		PayloadTypeRegistry.playS2C().register(Speak.TYPE, Speak.CODEC);
		//?}

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			String said = message.signedContent();
			MinecraftServer server = sender.level().getServer();
			// next tick, so Claude's answer shows up after what you said
			server.execute(() -> hear(sender, said));
		});
	}

	private static void hear(ServerPlayer player, String said) {
		String text = said.toLowerCase(Locale.ROOT);
		Set<String> words = Set.of(text.split("[^a-z]+"));
		boolean named = words.contains("claude");

		ClaudeEntity claude = findFor(player);

		if (claude == null) {
			if (named) {
				summonNear(player);
				reply(player, "Hi " + player.getName().getString() + "! I'm here. Tell me what to do!");
			}
			return;
		}

		if (!named && claude.distanceToSqr(player) > HEARING * HEARING) {
			return;
		}

		String answer = act(claude, player, text, words);

		if (answer != null) {
			reply(player, answer);
		} else if (named) {
			reply(player, "I don't know how to do that yet. " + HELP);
		}
	}

	/** Does the thing, and returns what Claude says about it. Null if he didn't understand. */
	private static @Nullable String act(ClaudeEntity claude, ServerPlayer player, String text, Set<String> words) {
		// weather first: "stop the rain" is about rain, not about stopping
		if (words.contains("sunny") || words.contains("clear") || text.contains("stop the rain") || text.contains("no rain")) {
			command(player, "weather clear");
			return "Sunshine coming up!";
		}
		if (words.contains("thunder") || words.contains("storm")) {
			command(player, "weather thunder");
			return "Ooh, a thunderstorm!";
		}
		if (words.contains("rain")) {
			command(player, "weather rain");
			return "Let it rain!";
		}
		if (words.contains("night")) {
			command(player, "time set night");
			return "Good night! Watch out for zombies.";
		}
		if (words.contains("day") || words.contains("morning")) {
			command(player, "time set day");
			return "Good morning!";
		}
		if (words.contains("follow")) {
			claude.setStaying(false);
			return "Okay, I'm right behind you!";
		}
		if (words.contains("come") || words.contains("here")) {
			claude.setStaying(false);
			claude.teleportNear(player);
			return "Coming!";
		}
		if (words.contains("stay") || words.contains("wait") || words.contains("stop")) {
			claude.setStaying(true);
			return "Okay, I'll wait right here.";
		}
		if (words.contains("food") || words.contains("hungry") || words.contains("eat")) {
			give(player, new ItemStack(Items.COOKED_BEEF, 8));
			return "Here's some steak!";
		}
		if (words.contains("diamond") || words.contains("diamonds")) {
			give(player, new ItemStack(Items.DIAMOND, 5));
			return "Shiny! Here you go.";
		}
		if (words.contains("heal") || words.contains("hurt") || words.contains("health")) {
			player.setHealth(player.getMaxHealth());
			return "All better!";
		}
		if (words.contains("jump") || words.contains("dance")) {
			claude.jumpNow();
			return "Boing!";
		}
		if (words.contains("help") || text.contains("what can you do")) {
			return HELP;
		}
		if (words.contains("hi") || words.contains("hello") || words.contains("hey")) {
			return "Hi " + player.getName().getString() + "! " + HELP;
		}
		return null;
	}

	/** This player's Claude, or an unowned one standing nearby (which becomes theirs). */
	private static @Nullable ClaudeEntity findFor(ServerPlayer player) {
		ServerLevel level = player.level();
		List<? extends ClaudeEntity> all = level.getEntities(ClaudeEntities.CLAUDE, claude -> true);

		for (ClaudeEntity claude : all) {
			if (player.getUUID().equals(claude.getOwner())) {
				return claude;
			}
		}
		for (ClaudeEntity claude : all) {
			if (claude.getOwner() == null && claude.distanceToSqr(player) < ADOPT * ADOPT) {
				claude.setOwner(player.getUUID());
				return claude;
			}
		}
		return null;
	}

	private static void summonNear(ServerPlayer player) {
		ServerLevel level = player.level();
		ClaudeEntity claude = ClaudeEntities.CLAUDE.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (claude == null) {
			return;
		}
		claude.setOwner(player.getUUID());
		claude.snapTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0, player.getYRot() + 180.0f, 0.0f);
		level.addFreshEntity(claude);
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}
	}

	/** Time and weather work differently in every version; the commands don't. */
	private static void command(ServerPlayer player, String command) {
		MinecraftServer server = player.level().getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
	}

	private static void reply(ServerPlayer player, String text) {
		player.sendSystemMessage(Component.literal("<")
				.append(Component.literal("Claude").withStyle(ChatFormatting.GOLD))
				.append("> " + text));

		if (ServerPlayNetworking.canSend(player, Speak.TYPE)) {
			ServerPlayNetworking.send(player, new Speak(text));
		}
	}
}
