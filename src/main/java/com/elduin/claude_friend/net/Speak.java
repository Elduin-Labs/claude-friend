package com.elduin.claude_friend.net;

import com.elduin.claude_friend.ClaudeFriend;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> one client: say this out loud in Claude's voice. */
public record Speak(String text) implements CustomPacketPayload {

	public static final Type<Speak> TYPE = new Type<>(ClaudeFriend.id("speak"));

	public static final StreamCodec<FriendlyByteBuf, Speak> CODEC = StreamCodec.of(
			(buf, value) -> buf.writeUtf(value.text()),
			buf -> new Speak(buf.readUtf()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
