package com.elduin.claude_friend.platform.fabric;

//? fabric {

import com.elduin.claude_friend.ClaudeFriend;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		ClaudeFriend.onInitialize();
	}
}
//?}
