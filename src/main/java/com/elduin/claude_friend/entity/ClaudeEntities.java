package com.elduin.claude_friend.entity;

import com.elduin.claude_friend.ClaudeFriend;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ClaudeEntities {

	public static final ResourceKey<EntityType<?>> CLAUDE_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, ClaudeFriend.id("claude"));

	// MISC, not CREATURE, so Claude never counts against the animal spawn cap.
	public static final EntityType<ClaudeEntity> CLAUDE = Registry.register(BuiltInRegistries.ENTITY_TYPE, CLAUDE_KEY,
			EntityType.Builder.<ClaudeEntity>of(ClaudeEntity::new, MobCategory.MISC)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build(CLAUDE_KEY));

	private ClaudeEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(CLAUDE, ClaudeEntity.createAttributes());
	}
}
