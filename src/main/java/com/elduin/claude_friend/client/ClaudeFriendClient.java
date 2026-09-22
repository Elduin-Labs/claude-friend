package com.elduin.claude_friend.client;

import com.elduin.claude_friend.entity.ClaudeEntities;
import com.elduin.claude_friend.net.Speak;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class ClaudeFriendClient {

	private ClaudeFriendClient() {
	}

	public static void register() {
		EntityRendererRegistry.register(ClaudeEntities.CLAUDE, ClaudeRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(Speak.TYPE, (payload, context) -> Voice.say(payload.text()));
	}
}
