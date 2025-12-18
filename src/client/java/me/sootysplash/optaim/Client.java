package me.sootysplash.optaim;

import com.google.common.collect.Streams;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static me.sootysplash.optaim.GLUtils.*;
import static org.lwjgl.opengl.GL32.*;

public class Client {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
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

        if (mc.currentScreen != null)
            return;

        if (getEnt().isEmpty())
            return;

        Entity e = getEnt().get(0);

        Camera cam = mc.gameRenderer.getCamera();

        double cubesize = config.size / 5;

        Box b = e.getBoundingBox().offset(getPosition(e).multiply(-1)).offset(e.getLerpedPos(getTickDelta()));
        if (e instanceof EnderDragonEntity dragon) {
            Vec3d eye = mc.player.getCameraPosVec(getTickDelta());
            Box closest = null;
            double dist = Short.MAX_VALUE;
            for (EnderDragonPart part : dragon.getBodyParts()) {
                Box newBox = part.getBoundingBox().offset(getPosition(part).multiply(-1)).offset(part.getLerpedPos(getTickDelta()));
                double newDist = eye.distanceTo(closestPointToBox(newBox));
                if (newDist < dist) {
                    closest = newBox;
                    dist = newDist;
                }
            }
            b = closest;
        }
        assert b != null;
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
        Vec3d targetpos = new Vec3d(box.minX, box.minY, box.minZ)/*.subtract(cam.getPos())*/;
//            System.out.println("Target pos: " + targetpos);
        Matrix4f model = new Matrix4f();

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

//        System.out.println("target: " + targetpos);
        model = model.translate((float) targetpos.x, (float) (targetpos.y), (float) targetpos.z);
        model = model.scale(x2, y2, z2);

        Vec3d cameraPos = cam.pos;

        Matrix4f view = new Matrix4f();
        view = view.rotate((float) Math.toRadians((float) cam.getPitch()), 1.0f, 0.0f, 0.0f);
        view = view.rotate((float) Math.toRadians((float) cam.getYaw() + 180f), 0.0f, 1.0f, 0.0f);
        view = view.translate(new Vector3f((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z));

        // both are valid, minecraft's projection matrix accounts for zoom from takeHugeScreenshot (I think only that???)
        Matrix4f projectionMc = mc.gameRenderer.getBasicProjectionMatrix(mc.gameRenderer.getFov(cam, getTickDelta(), true));

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
        glViewport(0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
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
    public static Vec3d getPosition(Entity e) {
        return new Vec3d(e.getX(), e.getY(), e.getZ());
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickProgress(true);
    }

    public static Vec3d closestPointToBox(Box box) {
        Vec3d eye = mc.player.getCameraPosVec(getTickDelta());
        return new Vec3d(Math.min(Math.max(eye.x, box.minX), box.maxX), Math.min(Math.max(eye.y, box.minY), box.maxY), Math.min(Math.max(eye.z, box.minZ), box.maxZ));
    }

    public static List<Entity> getEnt() {
        if (mc.world == null) {
            return List.of();
        }
        Stream<Entity> targets;
        targets = Streams.stream(mc.world.getEntities());
        Comparator<Entity> comparator = Comparator.comparing(Client::yaw);
        Config config = Config.getInstance();

        return targets.filter(e -> e != mc.player && mc.player.canSee(e) && e instanceof LivingEntity && mc.player.getCameraPosVec(getTickDelta()).distanceTo(closestPointToBox(e.getBoundingBox())) <= config.dist && e.isAttackable() && !e.isInvisible() && !e.hasPassenger(mc.player)).sorted(comparator).toList();
    }

    public static float yaw(Entity e) {
        Vec3d target = closestPointToBox(e.getBoundingBox());
        float amount = (float) Math.toDegrees(Math.atan2(target.z - mc.player.getZ(), target.x - mc.player.getX())) - 90.0f;
        amount = Math.abs(MathHelper.wrapDegrees(amount - mc.player.getYaw()));
        return amount;
    }
}