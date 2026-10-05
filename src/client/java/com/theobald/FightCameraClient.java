package com.theobald;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theobald.command.FightCamCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class FightCameraClient implements ClientModInitializer {
	public static Minecraft client;
	private static boolean flashbackLoaded = false;
	public boolean keybindPressed = false;
	public static boolean toggled = false;
	public static boolean active = false;
	public static boolean trackPearls = false;
	public static boolean drawLines = false;
	public static AnchorMode anchor = AnchorMode.AVERAGE;
	public static HeightMode heightMode = HeightMode.AVERAGE;
	public static DistanceMode distanceMode = DistanceMode.AUTO	;
	public static String[] playerStrings = {"", ""};
	public static Player[] players;
	public static float[] groundHeights = {60, 60};
	public static float smoothFactor = 0.7f;
	public static float distance = 3f;
	public static Vec3 lastTargetPos = Vec3.ZERO;
	public static Vec3 currentTargetPos = Vec3.ZERO;
	public static float lastTargetYaw = 0f;
	public static float currentTargetYaw = 0f;
	public static float height = 1.5f;
	public static float xoffset = 0;
	public static float aspectRatio;
	public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	//public static final Identifier WHITE_PIXEL = new Identifier("modid", "textures/misc/white_pixel.png");
	DecimalFormat df = new DecimalFormat();

	public enum AnchorMode {
		P1,
		P2,
		AVERAGE
	}

	public enum HeightMode {
		AVERAGE,
		GROUND
	}

	public enum DistanceMode {
		STATIC,
		AUTO,
	}

	@Override
	public void onInitializeClient() {
		client = Minecraft.getInstance();
		df.setMaximumFractionDigits(1);

		var toggleBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"Toggle Fight Camera",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_SEMICOLON,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fight-camera", "fightcam"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			onUpdate();
			if (toggleBind.isDown()) {
				if (!keybindPressed) {
					toggle();
					keybindPressed = true;
				}
			}
			else {
				keybindPressed = false;
			}
		});
//		WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
//			drawPlayerLines(context.matrixStack(), context.tickCounter().getTickDelta(true));
//		});

		FightCamCommand.register();
		if (FabricLoader.getInstance().isModLoaded("flashback")) { flashbackLoaded = true; }
	}

	private void onUpdate() {
		if (client.player != null) {
			if (toggled) {
				active = updatePlayers();
			} else {
				active = false;
			}

			if (active) {
				updateTarget(CalculateCameraPos());
				updateYawTarget(CalculateCameraYaw());
				updateGroundHeights();

				if (client.options.keyJump.isDown()) {
					height += client.options.keySprint.isDown() ? .2f : .1f;
					client.player.sendSystemMessage(Component.literal("Height: " + df.format(height)));
				}
				if (client.options.keyShift.isDown()) {
					height -= client.options.keySprint.isDown() ? .2f : .1f;
					client.player.sendSystemMessage(Component.literal("Height: " + df.format(height)));
				}
				if (client.options.keyUp.isDown()) {
					distance -= client.options.keySprint.isDown() ? .2f : .1f;
					client.player.sendSystemMessage(Component.literal("Distance: " + df.format(distance)));
				}
				if (client.options.keyDown.isDown()) {
					distance += client.options.keySprint.isDown() ? .2f : .1f;
					client.player.sendSystemMessage(Component.literal("Distance: " + df.format(distance)));
				}
				if (anchor != AnchorMode.AVERAGE) {
					float input = .1f;
					input *= anchor == AnchorMode.P1 ? 1 : -1;
					input *= client.options.keySprint.isDown() ? 2 : 1;

					if (client.options.keyRight.isDown()) {
						xoffset += input;
						client.player.sendSystemMessage(Component.literal("Offset: " + df.format(xoffset)));
					}
					if (client.options.keyLeft.isDown() && anchor != AnchorMode.AVERAGE) {
						xoffset -= input;
						client.player.sendSystemMessage(Component.literal("Offset: " + df.format(xoffset)));
					}
				}
			}
		}
	}

	public static void onRender(float tickDelta) {
		Camera camera = client.gameRenderer.mainCamera();
		if (camera != null && active) {
			//frame interpolation
			Vec3 newPos = getInterpolated(tickDelta);
			float newYaw = lerpAngle(lastTargetYaw,currentTargetYaw,tickDelta);

			((com.theobald.mixin.CameraAccessor) camera).fightcam$setPosition(newPos);
			client.player.setYRot(newYaw);
			client.player.yRotO = newYaw;

			if (client.player.isSpectator()) {
				Vec3 spectatorPos = lastTargetPos.lerp(currentTargetPos, tickDelta);
				client.player.setPos(spectatorPos.x, spectatorPos.y, spectatorPos.z);
			}
		}
	}

	public static Vec3 CalculateCameraPos() {
		Vec3 newTargetPos;

		Vec3 player1Pos;
		Vec3 player2Pos;

		if (trackPearls) {
			player1Pos = getPosIncludingPearls(players[0]);
			player2Pos = getPosIncludingPearls(players[1]);
		} else {
			player1Pos = players[0].position();
			player2Pos = players[1].position();
		}

		Vec3 avg = Util.Average(player1Pos,player2Pos);
		Vec3 distanceVec = player1Pos.subtract(player2Pos);
		Vec3 hDistanceVec = new Vec3(distanceVec.x, 0, distanceVec.z);
		Vec3 orthVec = Util.Orthoganal(distanceVec).normalize();
		Vec3 offset = orthVec.scale(distance).add(new Vec3(0, height, 0));

		//in case last pos is super far away
		if (currentTargetPos.subtract(lastTargetPos).length() > 500) { return avg; }

		//anchor xz position
		switch (anchor) {
			case P1 -> {
				newTargetPos = players[0].position();
				offset = offset.add(hDistanceVec.normalize().scale(-xoffset));
			}
			case P2 -> {
				newTargetPos = players[1].position();
				offset = offset.add(hDistanceVec.normalize().scale(xoffset));
			}
			default -> {
				newTargetPos = avg;
			}
		}

		//anchor height
		switch (heightMode) {
			case AVERAGE -> {

			}
			case GROUND -> {
				float avgGroundHeight = (groundHeights[0] + groundHeights[1]) / 2;
				newTargetPos = new Vec3(newTargetPos.x, avgGroundHeight, newTargetPos.z);
				//System.out.println(avgGroundHeight);
			}
		}

		//auto distance
        if (Objects.requireNonNull(distanceMode) == DistanceMode.AUTO) {
            int fov = client.options.fov().get();
            float autoDist = Util.FindAutoDistance(distanceVec, fov);
            offset = offset.add(orthVec.scale(autoDist));
        }

		newTargetPos = newTargetPos.add(offset);
		return Util.SmoothStep(currentTargetPos, newTargetPos, smoothFactor * ((smoothFactor == 1) ? 1 : Util.DistanceSmoothingCoeff((float) distanceVec.length())));
	}

	public static float CalculateCameraYaw() {
		Vec3 distanceVec;
		if (trackPearls) {
			distanceVec = getPosIncludingPearls(players[0]).subtract(getPosIncludingPearls(players[1]));
		} else {
			distanceVec = players[0].position().subtract(players[1].position());
		}

		Vec3 orthVec = distanceVec.normalize();
		float newTargetYaw = (float) Math.toDegrees((float) Math.atan2(orthVec.z, orthVec.x));
		return lerpAngle(currentTargetYaw, newTargetYaw, smoothFactor * ((smoothFactor == 1) ? 1 : Util.DistanceSmoothingCoeff((float) distanceVec.length())));
	}

	public static void toggle() {
		toggled = !toggled;
		if (toggled) {
			if (client.player != null && updatePlayers()) {
				sendMessage("Fight cam enabled!");

				currentTargetPos = Util.Average(players[0].position(), players[1].position());
				currentTargetYaw = CalculateCameraYaw();
				Window window = client.getWindow();
				aspectRatio = (float) window.getWidth() / window.getHeight();
				updateGroundHeights();
			}
			else {
				// no valid players, don't leave it armed or it turns itself on later
				toggled = false;
				active = false;
				sendMessage("Pick 2 players first: /fightcam players <p1> <p2>");
			}
		}
		else {
			assert client.player != null;
			client.player.sendSystemMessage(Component.literal("Fight cam disabled!"));
		}

	}

	public static boolean getActive() {
		return active;
	}

	public static void setPlayers(Player[] p) {
		players = p;
	}

	public static boolean checkPlayers() {
		return (playerStrings[0] != null && playerStrings[1] != null);
	}

	public static void updateTarget(Vec3 newPos) {
		lastTargetPos = currentTargetPos;
		currentTargetPos = newPos;
	}

	public static void updateYawTarget(float newYaw) {
		lastTargetYaw = currentTargetYaw;
		currentTargetYaw = newYaw;
	}

	public static float lerpAngle(float a, float b, float t) {
		float delta = ((b - a + 180f) % 360f + 360f) % 360f - 180f;
		return a + delta * t;
	}

	public static Vec3 getInterpolated(double tickDelta) {
		return lastTargetPos.lerp(currentTargetPos, tickDelta);
	}

	public static void setDistanceMode(String s) {
		try {
			var f = Float.parseFloat(s);
			distance = f;
			distanceMode = DistanceMode.STATIC;
			sendMessage("Distance set to " + f);
		}
		catch (NumberFormatException e) {
			if (Objects.equals(s, "auto")) {
				distanceMode = DistanceMode.AUTO;
				anchor = AnchorMode.AVERAGE;
				sendMessage("Distance set to " + s);
			}
			else {
				sendMessage("Distance set to " + distanceMode.toString());
			}
		}
	}

	public static void setAnchorMode(String s) {
		switch (s) {
			case "avg" -> {
				anchor = AnchorMode.AVERAGE;
				sendMessage("Anchor set to AVERAGE");
			}
			case "p1" -> {
				anchor = AnchorMode.P1;
				distanceMode = DistanceMode.STATIC;
				sendMessage("Anchor set to player 1");
			}
			case "p2" -> {
				anchor = AnchorMode.P2;
				distanceMode = DistanceMode.STATIC;
				sendMessage("Anchor set to player 2");
			}
			case null, default -> {
				sendMessage("Anchor set to " + anchor.toString());
			}
		}
	}

	public static void setHeightMode(String s) {
		try {
			var f = Float.parseFloat(s);
			height = f;
			sendMessage("Height set to " + f);
		}
		catch (NumberFormatException e) {
			switch (s) {
				case "avg" -> {
					heightMode = HeightMode.AVERAGE;
					sendMessage("Height mode set to AVERAGE");
				}
				case "ground" -> {
					heightMode = HeightMode.GROUND;
					sendMessage("Height mode set to GROUND");
				}
				case null, default -> {
					sendMessage("Height mode set to " + heightMode.toString());
				}
			}
		}
	}

	public static void setSmoothFactor(float smoothFactor) {
		FightCameraClient.smoothFactor = smoothFactor;
	}

	private static void sendMessage(String message) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			player.sendSystemMessage(Component.literal(message));
		}
	}

	public static boolean updatePlayers()
	{
		if (!checkPlayers()) return false;

		Player p1 = Minecraft.getInstance().level.players()
				.stream().filter(p -> p.getName().getString().equals(playerStrings[0])).findFirst().orElse(null);
		Player p2 = Minecraft.getInstance().level.players()
				.stream().filter(p -> p.getName().getString().equals(playerStrings[1])).findFirst().orElse(null);

		if (p1 == null || p2 == null) {
			if (active) sendMessage("One or both players not found.");
			active = false;
			return false;
		}

		FightCameraClient.setPlayers(new Player[]{p1, p2});
		return true;
	}

	public static void updateGroundHeights() {
		for (int i = 0; i < 2; i++) {
			Player player = players[i];
			BlockPos blockPos = player.blockPosition().below();
			BlockState state = player.level().getBlockState(blockPos);
			VoxelShape shape = state.getCollisionShape(client.level, blockPos);

			if (!shape.isEmpty()) {
				groundHeights[i] = blockPos.getY() + 1;
			}
		}
	}

	public static Vec3 getPosIncludingPearls(Player player) {
		Vec3 playerPos = player.position();
		List<Vec3> playerPearls = Util.GetPlayerPearlsPos(player.level(), player);
		if (playerPearls.isEmpty()) {
			return playerPos;
		}

		return Util.Average(playerPos, Util.Average(playerPearls));
	}
}
