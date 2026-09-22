package com.elduin.claude_friend.platform.fabric;

//? fabric {

import com.elduin.claude_friend.ClaudeFriend;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClaudeFriend.onInitializeClient();
	}

}
//?}
