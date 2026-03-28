package me.sootysplash.optaim;

import com.google.common.collect.Streams;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static me.sootysplash.optaim.GLUtils.*;
import static org.lwjgl.opengl.GL32.*;

public class Client {
    public static final Minecraft mc = Minecraft.getInstance();
    public static boolean initialized = false;
    public static int
            cubeDrawShaderProgram,
            cubeArrayObject,

            projectionLocation,
            viewLocation,
            modelLocation,

            customColorLocation;

    private static void initializeRenderingObjects() {
        int vertFrameBuf = makeShader("assets/optimalaim/shaders/plainThreeDV.vert", GL_VERTEX_SHADER);
        int fragFrameBuf = makeShader("assets/optimalaim/shaders/customColorF.frag", GL_FRAGMENT_SHADER);
        cubeDrawShaderProgram = makeShaderProgram(vertFrameBuf, fragFrameBuf);

        cubeArrayObject = makeCubeObject();

        glUseProgram(cubeDrawShaderProgram);

        projectionLocation = glGetUniformLocation(cubeDrawShaderProgram, "projection");
        viewLocation = glGetUniformLocation(cubeDrawShaderProgram, "view");
        modelLocation = glGetUniformLocation(cubeDrawShaderProgram, "model");
        customColorLocation = glGetUniformLocation(cubeDrawShaderProgram, "customColor");
    }

    public static void renderOptimalAimBox() {
        if (!initialized) {
            initializeRenderingObjects();
            initialized = true;
        }
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

        Camera cam = mc.gameRenderer.getMainCamera();

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
        Vec3 targetpos = new Vec3(box.minX, box.minY, box.minZ)/*.subtract(cam.getPos())*/;
//            System.out.println("Target pos: " + targetpos);
        Matrix4f model = new Matrix4f();

        box = box.move(new Vec3(box.minX, box.minY, box.minZ).reverse());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

//        System.out.println("target: " + targetpos);
        model = model.translate((float) targetpos.x, (float) (targetpos.y), (float) targetpos.z);
        model = model.scale(x2, y2, z2);

        Vec3 cameraPos = cam.position();

        Matrix4f view = new Matrix4f();
//        view = view.rotate((float) Math.toRadians((float) cam.xRot()), 1.0f, 0.0f, 0.0f);
//        view = view.rotate((float) Math.toRadians((float) cam.yRot() + 180f), 0.0f, 1.0f, 0.0f);
        view = view.translate(new Vector3f((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z));

        Matrix4f projectionMc = mc.gameRenderer.getMainCamera().getViewRotationProjectionMatrix(new Matrix4f());

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(cubeDrawShaderProgram);

        glUniformMatrix4fv(projectionLocation, false, projectionMc.get(new float[4 * 4]));
        glUniformMatrix4fv(viewLocation, false, view.get(new float[4 * 4]));
        glUniformMatrix4fv(modelLocation, false, model.get(new float[4 * 4]));

        drawCube();
    }

    public static void drawCube() {
        if (!initialized) {
            initializeRenderingObjects();
            initialized = true;
        }
        Config config = Config.getInstance();
        Color col = new Color(config.color);
        int red = col.getRed();
        int green = col.getGreen();
        int blue = col.getBlue();
        int alpha = (int) (config.transparency * 2.55);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDrawBuffer(GL_BACK);
        glViewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
        glUseProgram(cubeDrawShaderProgram);
        glUniform4fv(customColorLocation, new float[]{(red / 255f), (green / 255f), (blue / 255f), (alpha / 255f)});

        glDisable(GL_DEPTH_TEST); // using this worked

        glBindVertexArray(cubeArrayObject);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);

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