package me.sootysplash.optaim;

import com.google.common.collect.Streams;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Client {
    public static final Minecraft mc = Minecraft.getInstance();

    public static void renderOptimalAimBox() {
        Config config = Config.getInstance();
        if (!config.enabled)
            return;

        if (mc.player == null)
            return;

        if (mc.screen != null)
            return;

        if (getEnt().isEmpty())
            return;

        Entity e = getEnt().get(0);

        double cubesize = config.size / 5;

        AABB b = e.getBoundingBox().move(getPosition(e).scale(-1)).move(e.getPosition(getTickDelta()));
        if (e instanceof EnderDragon dragon) {
            Vec3 eye = mc.player.getEyePosition(getTickDelta());
            AABB closest = null;
            double dist = Short.MAX_VALUE;
            for (EnderDragonPart part : dragon.getSubEntities()) {
                AABB newBox = part.getBoundingBox().move(getPosition(part).scale(-1)).move(part.getPosition(getTickDelta()));
                double newDist = eye.distanceTo(closestPointToBox(newBox));
                if (newDist < dist) {
                    closest = newBox;
                    dist = newDist;
                }
            }
            b = closest;
        }
        assert b != null;
        Vec3 opt = closestPointToBox(b);


        Vec3 optmin = opt.add(-cubesize, -cubesize, -cubesize);
        Vec3 optmax = opt.add(cubesize, cubesize, cubesize);

        Vec3 optmincomp = new Vec3(-(optmin.x() - Math.max(optmin.x(), b.minX)), -(optmin.y() - Math.max(optmin.y(), b.minY)), -(optmin.z() - Math.max(optmin.z(), b.minZ)));
        Vec3 optmaxcomp = new Vec3(-(optmax.x() - Math.min(optmax.x(), b.maxX)), -(optmax.y() - Math.min(optmax.y(), b.maxY)), -(optmax.z() - Math.min(optmax.z(), b.maxZ)));

        if (config.hitbox) {

            optmin = optmin.add(optmincomp.add(optmaxcomp));
            optmax = optmax.add(optmaxcomp.add(optmincomp));

        }

        AABB box = new AABB(optmin, optmax);
        GizmoStyle gz = GizmoStyle.fill(ARGB.color((int) (config.transparency * 2.55), config.color));
        GizmoProperties gp = Gizmos.cuboid(box, gz);
        gp.setAlwaysOnTop();
    }

    // this is for 1.21.5-1.21.10 support without any pain
    public static Vec3 getPosition(Entity e) {
        return new Vec3(e.getX(), e.getY(), e.getZ());
    }

    public static float getTickDelta() {
        return mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static Vec3 closestPointToBox(AABB box) {
        Vec3 eye = mc.player.getEyePosition(getTickDelta());
        return new Vec3(Math.min(Math.max(eye.x, box.minX), box.maxX), Math.min(Math.max(eye.y, box.minY), box.maxY), Math.min(Math.max(eye.z, box.minZ), box.maxZ));
    }

    public static List<Entity> getEnt() {
        if (mc.level == null) {
            return List.of();
        }
        Stream<Entity> targets;
        targets = Streams.stream(mc.level.entitiesForRendering());
        Comparator<Entity> comparator = Comparator.comparing(Client::yaw);
        Config config = Config.getInstance();

        return targets.filter(e -> e != mc.player && mc.player.hasLineOfSight(e) && e instanceof LivingEntity && mc.player.getEyePosition(getTickDelta()).distanceTo(closestPointToBox(e.getBoundingBox())) <= config.dist && e.isAttackable() && !e.isInvisible() && !e.hasPassenger(mc.player)).sorted(comparator).toList();
    }

    public static float yaw(Entity e) {
        Vec3 target = closestPointToBox(e.getBoundingBox());
        float amount = (float) Math.toDegrees(Math.atan2(target.z - mc.player.getZ(), target.x - mc.player.getX())) - 90.0f;
        amount = Math.abs(Mth.wrapDegrees(amount - mc.player.getYRot()));
        return amount;
    }
}