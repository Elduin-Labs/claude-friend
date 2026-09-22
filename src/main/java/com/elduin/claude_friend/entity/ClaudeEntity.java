package com.elduin.claude_friend.entity;

import java.util.EnumSet;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Claude, Elduin's friend. Follows the player who called him, or waits where
 * he's told to. He can't be hurt (except by a player in creative) and never
 * despawns.
 */
public class ClaudeEntity extends PathfinderMob {

	/** Start walking over when the owner is further than this. */
	private static final double FOLLOW_START = 5.0;
	/** Close enough — stop and just look at them. */
	private static final double FOLLOW_STOP = 2.5;
	/** Too far to walk: pop over next to them. */
	private static final double TELEPORT = 24.0;

	private @Nullable UUID owner;
	private boolean staying;

	public ClaudeEntity(EntityType<? extends ClaudeEntity> type, Level level) {
		super(type, level);
		setCustomName(Component.literal("Claude"));
		setCustomNameVisible(true);
		setPersistenceRequired();
		setInvulnerable(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new FollowOwnerGoal(this));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public @Nullable UUID getOwner() {
		return owner;
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
	}

	public @Nullable Player getOwnerPlayer() {
		return owner == null ? null : level().getPlayerByUUID(owner);
	}

	public void setStaying(boolean staying) {
		this.staying = staying;
		if (staying) {
			getNavigation().stop();
		}
	}

	public void jumpNow() {
		getJumpControl().jump();
	}

	public void teleportNear(Player player) {
		teleportTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0);
		getNavigation().stop();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (owner != null) {
			output.putString("Owner", owner.toString());
		}
		output.putBoolean("Staying", staying);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		owner = input.getString("Owner").map(ClaudeEntity::parseUuid).orElse(null);
		staying = input.getBooleanOr("Staying", false);
	}

	private static @Nullable UUID parseUuid(String text) {
		try {
			return UUID.fromString(text);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** Walks after the owner, and teleports over if they get too far ahead. */
	private static final class FollowOwnerGoal extends Goal {

		private final ClaudeEntity claude;
		private @Nullable Player target;
		private int repathIn;

		FollowOwnerGoal(ClaudeEntity claude) {
			this.claude = claude;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (claude.staying) {
				return false;
			}
			Player owner = claude.getOwnerPlayer();
			if (owner == null || owner.isSpectator() || claude.distanceToSqr(owner) < FOLLOW_START * FOLLOW_START) {
				return false;
			}
			target = owner;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return !claude.staying && target != null && target.isAlive()
					&& claude.distanceToSqr(target) > FOLLOW_STOP * FOLLOW_STOP;
		}

		@Override
		public void start() {
			repathIn = 0;
		}

		@Override
		public void stop() {
			target = null;
			claude.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (target == null) {
				return;
			}
			claude.getLookControl().setLookAt(target, 10.0f, claude.getMaxHeadXRot());
			if (--repathIn > 0) {
				return;
			}
			repathIn = 10;
			if (claude.distanceToSqr(target) > TELEPORT * TELEPORT) {
				claude.teleportNear(target);
			} else {
				claude.getNavigation().moveTo(target, 1.2);
			}
		}
	}
}
