package me.sootysplash;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.atan2;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class Client implements ClientModInitializer {
    MinecraftClient mc = MinecraftClient.getInstance();
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END.register(context -> {
            Config config = Config.getInstance();
            if (!config.enabled)
                return;

            if (mc.player == null)
                return;

            if (getEnt().isEmpty())
                return;

            Entity e = getEnt().get(0);

            Camera cam = context.camera();
            MatrixStack matstack = new MatrixStack();
            matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

            double cubesize = config.size / 5;

			Box b = e.getBoundingBox().offset(e.getPos().multiply(-1)).offset(MathHelper.lerp(mc.getTickDelta(), e.prevX, e.getX()), MathHelper.lerp(mc.getTickDelta(), e.prevY, e.getY()), MathHelper.lerp(mc.getTickDelta(), e.prevZ, e.getZ()));
            Vec3d opt = closestPointToBox(b);


            Vec3d optmin = opt.add(-cubesize, -cubesize, -cubesize);
            Vec3d optmax = opt.add(cubesize, cubesize, cubesize);

            Vec3d optmincomp = new Vec3d(-(optmin.getX() - Math.max(optmin.getX(), b.minX)), -(optmin.getY() - Math.max(optmin.getY(), b.minY)), -(optmin.getZ() - Math.max(optmin.getZ(), b.minZ)));
            Vec3d optmaxcomp = new Vec3d(-(optmax.getX() - Math.min(optmax.getX(), b.maxX)), -(optmax.getY() - Math.min(optmax.getY(), b.maxY)), -(optmax.getZ() - Math.min(optmax.getZ(), b.maxZ)));

            if (config.hitbox) {

                optmin = optmin.add(optmincomp.add(optmaxcomp));
                optmax = optmax.add(optmaxcomp.add(optmincomp));

            }

            Box box = new Box(optmin, optmax);
            Vec3d targetpos = new Vec3d(box.minX, box.minY, box.minZ).subtract(cam.getPos());
            matstack.translate(targetpos.x, targetpos.y, targetpos.z);

            box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

            float x1 = (float) box.minX;
            float y1 = (float) box.minY;
            float z1 = (float) box.minZ;
            float x2 = (float) box.maxX;
            float y2 = (float) box.maxY;
            float z2 = (float) box.maxZ;

            Color col = new Color(config.color);
            int red = col.getRed();
            int green = col.getGreen();
            int blue = col.getBlue();
            int alpha = (int) (config.transparency * 2.55);

            Matrix4f posMat = matstack.peek().getPositionMatrix();
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffy = tessy.getBuffer();

            buffy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            //north
            buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha).next();

            //west
            buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).next();

            //up
            buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).next();

            //down
            buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha).next();

            //east
            buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).next();

            //south
            buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).next();
            buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).next();

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            tessy.draw();

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();

        });
    }

    public Vec3d closestPointToBox(Box box) {
        return new Vec3d(Math.min(Math.max(mc.player.getCameraPosVec(mc.getTickDelta()).x, box.minX), box.maxX), Math.min(Math.max(mc.player.getCameraPosVec(mc.getTickDelta()).y, box.minY), box.maxY), Math.min(Math.max(mc.player.getCameraPosVec(mc.getTickDelta()).z, box.minZ), box.maxZ));
    }

    public List<Entity> getEnt() {
        if (mc.world == null) {
            return null;
        }
        Stream<Entity> targets;
        targets = Streams.stream(mc.world.getEntities());
        Comparator<Entity> comparator = Comparator.comparing(this::yaw);
        Config config = Config.getInstance();

        return targets.filter(e -> e != mc.player && mc.player.canSee(e) && e instanceof LivingEntity && mc.player.getCameraPosVec(mc.getTickDelta()).distanceTo(closestPointToBox(e.getBoundingBox())) <= config.dist && e.isAttackable() && !e.isInvisible()).sorted(comparator).toList();
    }

    public float yaw(Entity e) {
        Vec3d target = closestPointToBox(e.getBoundingBox());
        float amount = (float) Math.toDegrees(atan2(target.z - mc.player.getZ(), target.x - mc.player.getX())) - 90.0f;
        amount = Math.abs(wrapDegrees(amount - mc.player.getYaw()));
        return amount;
    }
}