package com.theobald;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Util
{
    public static Vec3 Average(Vec3 a, Vec3 b) {
        float x = (float) (a.x + b.x) / 2;
        float y = (float) (a.y + b.y) / 2;
        float z = (float) (a.z + b.z) / 2;

        return new Vec3(x,y,z);
    }

    public static Vec3 Average(List<Vec3> vecs) {
        Vec3 sum = new Vec3(0, 0, 0);
        int size = vecs.size();

        if (size == 0) {
            return sum;
        }

        for (Vec3 vec : vecs) {
            sum = sum.add(vec);
        }
        return new Vec3(sum.x/size, sum.y/size, sum.z/size);
    }

    public static Vec3 Orthoganal(Vec3 a)
    {
        return new Vec3(a.z, 0, -a.x);
    }

    public static float FindAutoDistance(Vec3 playerDist, int fov) {
        float hFov = fov * FightCameraClient.aspectRatio * .8f;
        double hDist = playerDist.horizontalDistance();
        float hAutoDistance = Math.abs((float)((hDist/2) / Math.tan(Math.toRadians((double) hFov/2))));
        if (FightCameraClient.heightMode == FightCameraClient.HeightMode.GROUND) {
            return hAutoDistance;
        }

        float vFov = fov;
        double vDist = playerDist.y;
        float vAutoDistance = Math.abs((float)((vDist/2) / Math.tan(Math.toRadians((double) vFov/2))));

        return Math.max(hAutoDistance, vAutoDistance);
    }

    public static Vec3 SmoothStep(Vec3 a, Vec3 b, float t) {
        double x = a.x + ((b.x - a.x) * t);
        double y = a.y + ((b.y - a.y) * t);
        double z = a.z + ((b.z - a.z) * t);

        return new Vec3(x, y, z);
    }

    public static float DistanceSmoothingCoeff(float x)
    {
        float z = .2f;
        float a = 4f;
        float b = -4.2f;
        float k = 1 - z;

        //sigmoid
        return (k / (1 + (float) Math.pow(Math.E, a + (b * x)))) + z;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static List<Vec3> GetPlayerPearlsPos(net.minecraft.world.level.Level world, Player player) {
        if (world == null || player == null) return Collections.emptyList();

        List<Vec3> pearlsPos = new ArrayList<>();
        Vec3 playerPos = player.position();
        AABB worldAABB = new AABB(
                playerPos.x-500, -64, playerPos.z-500,
                playerPos.x+500, 320, playerPos.z+500
        );

        for (Entity entity : world.getEntities((Entity) null, worldAABB, e -> e instanceof ThrownEnderpearl)) {
            if (entity instanceof ThrownEnderpearl pearl) {
                Entity owner = pearl.getOwner();
                if (owner != null && owner.equals(player)) {
                    pearlsPos.add(pearl.position());
                }
            }
        }

        return pearlsPos;
    }

    public static Vec3 getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float g = -yaw * ((float)Math.PI / 180F);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3((double)(i * j), (double)(-k), (double)(h * j));
    }

    public static Vec3 getInterpolatedPos(Entity entity, float tickDelta) {
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
        return new Vec3(x, y, z);
    }

    public static Vec3 getInterpolatedLook(Entity entity, float tickDelta) {
        float yaw = Mth.lerp(tickDelta, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(tickDelta, entity.xRotO, entity.getXRot());

        return getRotationVector(pitch, yaw);
    }
}
