package com.kk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 * Coordinate HUD Mod 主类
 * 负责初始化mod、注册HUD元素和按键绑定
 */
public class CoordinateHud implements ClientModInitializer {
    // Minecraft客户端实例
    private static final Minecraft minecraft = Minecraft.getInstance();

    // HUD元素的唯一标识符
    private static final Identifier HUD_ELEMENT_ID = Identifier.fromNamespaceAndPath("coordinate-hud", "main");

    // 切换HUD显示的按键绑定
    private KeyMapping toggleHudKeyMapping;
    private KeyMapping.Category helpF10Category = KeyMapping.Category.register(HUD_ELEMENT_ID);

    // HUD是否可见的标志
    private boolean hudVisible = true;

    // 配置文件路径
    private static final String CONFIG_FILE_NAME = "coordinate_hud.properties";

    // HUD常量设置
    private static final int HUD_COLOR = 0xFFFFFFFF; // 文本颜色，白色不透明
    private static final int HUD_START_X = 4;        // HUD起始X坐标
    private static final int HUD_START_Y = 4;        // HUD起始Y坐标
    private static final int HUD_LINE_HEIGHT = 10;   // 行高

    /**
     * 客户端初始化方法
     * 注册按键绑定和HUD元素
     */
    @Override
    public void onInitializeClient() {
        // 读取配置文件中的HUD可见性设置
        loadConfig();

        // 注册切换HUD的按键绑定，默认为F10
        toggleHudKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.coordinate_hud.toggle",
                GLFW.GLFW_KEY_F10,
                helpF10Category
        ));

        // 将HUD渲染方法注册到HUD元素注册表中
        HudElementRegistry.addLast(HUD_ELEMENT_ID, this::renderHud);
    }

    /**
     * HUD渲染方法
     * @param graphics GuiGraphicsExtractor对象，用于绘制文本和图形
     * @param tickCounter DeltaTracker对象，用于获取渲染相关的时间信息
     */
    private void renderHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        // 检查并处理按键事件，切换HUD可见性
        if (toggleHudKeyMapping != null) {
            while (toggleHudKeyMapping.consumeClick()) {
                hudVisible = !hudVisible;
                // 保存配置到文件
                saveConfig();
            }
        }

        // 如果HUD不可见，或者玩家/世界对象为空，或者Minecraft的F3调试窗口打开，则不进行渲染
        if (!hudVisible || minecraft.player == null || minecraft.level == null || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        // 获取玩家位置
        Vec3 playerPos = minecraft.player.position();

        // 获取区块内相对位置
        BlockPos playerBlockPos = minecraft.player.blockPosition();
        int blockX = Math.floorMod(playerBlockPos.getX(), 16);
        int blockY = Math.floorMod(playerBlockPos.getY(), 16);
        int blockZ = Math.floorMod(playerBlockPos.getZ(), 16);

        // 获取生物群系名称
        Holder<Biome> biomeEntry = minecraft.level.getBiome(playerBlockPos);
        String biomeName = biomeEntry.unwrapKey()
                .map(key -> "biome." + key.identifier().getNamespace() + "." + key.identifier().getPath())
                .filter(I18n::exists)
                .map(I18n::get)
                .filter(s -> !s.isEmpty())
                .orElse(I18n.get("coordinate_hud.biome.unknown"));

        // 获取玩家视角角度和方向
        float yaw = wrapAngleTo180(minecraft.player.getYRot());   // 偏航角 (Yaw)
        float pitch = minecraft.player.getXRot();                 // 俯仰角 (Pitch)
        String direction = getDirectionFromYaw(yaw);              // 方向 (Direction)

        // 获取当前FPS
        int fps = minecraft.getFps();

        // 获取当前维度
        String dimension = minecraft.level.dimension().identifier().toString();

        // 计算另一维度的坐标
        String otherDimensionCoords = "";
        if (dimension.equals("minecraft:overworld")) {
            otherDimensionCoords = String.format("%s: %.3f, %.3f, %.3f", I18n.get("coordinate_hud.nether_coord"), playerPos.x / 8, playerPos.y, playerPos.z / 8);
        } else if (dimension.equals("minecraft:the_nether")) {
            otherDimensionCoords = String.format("%s: %.3f, %.3f, %.3f", I18n.get("coordinate_hud.overworld_coord"), playerPos.x * 8, playerPos.y, playerPos.z * 8);
        } else {
            otherDimensionCoords = String.format("(%s)", I18n.get("coordinate_hud.other_dim_coord.na"));
        }

        // 计算视野内的实体数量和世界中的总实体数量
        int[] entityCounts = getEntitiesInViewAndTotal(minecraft.player, yaw, pitch);
        int entityCountInView = entityCounts[0];
        int totalEntityCount = entityCounts[1];

        // 获取地表高度
        int surfaceHeight = getSurfaceHeight(minecraft.level, playerBlockPos.getX(), playerBlockPos.getZ());

        // 获取温度信息
        float temperature = minecraft.level.getBiome(playerBlockPos).value().getBaseTemperature();

        // 准备要显示的文本行
        String coordsText = String.format("%s: %.3f, %.3f, %.3f | %s: %d, %d, %d | %s: %d",
                I18n.get("coordinate_hud.coord"), playerPos.x, playerPos.y, playerPos.z,
                I18n.get("coordinate_hud.chunk_rel_coord"), blockX, blockY, blockZ,
                I18n.get("coordinate_hud.fps"), fps);
        String chunkText = String.format("%s: %.2f %s: %.2f | %s: %s",
                I18n.get("coordinate_hud.yaw"), yaw, I18n.get("coordinate_hud.pitch"), pitch,
                I18n.get("coordinate_hud.direction"), direction);
        String biomeText = I18n.get("coordinate_hud.biome") + ": " + biomeName;
        String otherCoordsText = otherDimensionCoords;
        String entitiesText = String.format("%s: %d/%d", I18n.get("coordinate_hud.entities"), entityCountInView, totalEntityCount);
        String surfaceHeightText = String.format("%s: %d", I18n.get("coordinate_hud.surface_height"), surfaceHeight);
        String temperatureText = String.format("%s: %.2f", I18n.get("coordinate_hud.temperature"), temperature);

        // 绘制文本行
        graphics.text(minecraft.font, coordsText, HUD_START_X, HUD_START_Y + 0 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, chunkText, HUD_START_X, HUD_START_Y + 1 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, biomeText, HUD_START_X, HUD_START_Y + 2 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, otherCoordsText, HUD_START_X, HUD_START_Y + 3 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, entitiesText, HUD_START_X, HUD_START_Y + 4 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, surfaceHeightText, HUD_START_X, HUD_START_Y + 5 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        graphics.text(minecraft.font, temperatureText, HUD_START_X, HUD_START_Y + 6 * HUD_LINE_HEIGHT, HUD_COLOR, true);
    }

    /**
     * 计算玩家视野内的实体数量和世界中的总实体数量
     */
    public static int[] getEntitiesInViewAndTotal(Player player, float yaw, float pitch) {
        Vec3 playerViewVector = getPlayerViewVector(yaw, pitch);
        Iterable<Entity> entities = minecraft.level.entitiesForRendering();

        int totalEntityCount = 0;
        int entityCountInView = 0;
        double maxAngle = Math.toRadians(minecraft.options.fov().get() / 2.0);

        for (Entity entity : entities) {
            if (entity.isAlive() && entity != player) {
                totalEntityCount++;

                Vec3 playerEyePos = player.getEyePosition();
                Vec3 entityPos = entity.position();
                Vec3 toEntity = entityPos.subtract(playerEyePos);

                double distance = toEntity.length();
                if (distance == 0) {
                    continue;
                }

                Vec3 entityDirection = toEntity.normalize();
                double angle = getAngleBetweenVectors(playerViewVector, entityDirection);

                if (angle <= maxAngle) {
                    entityCountInView++;
                }
            }
        }

        return new int[]{entityCountInView, totalEntityCount};
    }

    private static Vec3 getPlayerViewVector(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vec3(x, y, z);
    }

    private static double getAngleBetweenVectors(Vec3 vector1, Vec3 vector2) {
        double dotProduct = vector1.dot(vector2);
        return Math.acos(dotProduct);
    }

    private static float wrapAngleTo180(float angle) {
        angle = angle % 360;
        if (angle >= 180) {
            angle -= 360;
        }
        if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    private String getDirectionFromYaw(float yaw) {
        if (yaw >= -45 && yaw < 45) {
            return I18n.get("coordinate_hud.direction.south");
        } else if (yaw >= 45 && yaw < 135) {
            return I18n.get("coordinate_hud.direction.west");
        } else if (yaw >= -135 && yaw < -45) {
            return I18n.get("coordinate_hud.direction.east");
        } else {
            return I18n.get("coordinate_hud.direction.north");
        }
    }

    public static int getSurfaceHeight(Level level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    private void loadConfig() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        File configFile = new File(configDir, CONFIG_FILE_NAME);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Properties props = new Properties();
                props.load(reader);
                hudVisible = Boolean.parseBoolean(props.getProperty("hudVisible", "true"));
            } catch (Exception e) {
                hudVisible = true;
            }
        }
    }

    private void saveConfig() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, CONFIG_FILE_NAME);
        try (FileWriter writer = new FileWriter(configFile)) {
            Properties props = new Properties();
            props.setProperty("hudVisible", String.valueOf(hudVisible));
            props.store(writer, "Coordinate HUD Configuration");
        } catch (Exception e) {
            // 静默忽略保存错误
        }
    }
}
