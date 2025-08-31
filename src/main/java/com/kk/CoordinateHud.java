package com.kk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.lwjgl.glfw.GLFW;

/**
 * Coordinate HUD Mod 主类
 * 负责初始化mod、注册HUD元素和按键绑定
 */
public class CoordinateHud implements ClientModInitializer {
    // Minecraft客户端实例
    private static final MinecraftClient client = MinecraftClient.getInstance();
    
    // 切换HUD显示的按键绑定
    private KeyBinding toggleHudKeyBinding;

    // HUD是否可见的标志
    private boolean hudVisible = true;

    // HUD元素的唯一标识符
    private static final Identifier HUD_ELEMENT_ID = Identifier.of("coordinate-hud", "main_hud");
    
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
        // 注册切换HUD的按键绑定，默认为F10
        toggleHudKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.coordinate_hud.toggle",
                GLFW.GLFW_KEY_F10,
                "category.coordinate_hud"
        ));

        // 将HUD渲染方法注册到HUD元素注册表中
        HudElementRegistry.addLast(HUD_ELEMENT_ID, this::renderHud);
    }

    /**
     * HUD渲染方法
     * @param context DrawContext对象，用于绘制文本和图形
     * @param tickCounter RenderTickCounter对象，用于获取渲染相关的时间信息
     */
    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        // 检查并处理按键事件，切换HUD可见性
        if (toggleHudKeyBinding != null) {
            while (toggleHudKeyBinding.wasPressed()) {
                hudVisible = !hudVisible;
            }
        }

        // 如果HUD不可见，或者玩家/世界对象为空，则不进行渲染
        if (!hudVisible || client.player == null || client.world == null) {
            return;
        }

        // 获取玩家位置
        Vec3d playerPos = client.player.getPos();

        // 获取区块内相对位置
        BlockPos playerBlockPos = client.player.getBlockPos();
        int blockX = Math.floorMod(playerBlockPos.getX(), 16);
        int blockY = Math.floorMod(playerBlockPos.getY(), 16);
        int blockZ = Math.floorMod(playerBlockPos.getZ(), 16);

        // 获取生物群系名称
        RegistryEntry<Biome> biomeEntry = client.world.getBiome(playerBlockPos);
        String biomeName = biomeEntry.getKey()
                .map(key -> "biome." + key.getValue().getNamespace() + "." + key.getValue().getPath())
                .filter(I18n::hasTranslation) // 检查是否有翻译
                .map(I18n::translate)
                .filter(s -> !s.isEmpty()) // 确保翻译结果非空
                .orElse(I18n.translate("coordinate_hud.biome.unknown")); // 使用自定义的未知翻译

        // 获取玩家视角角度和方向
        float yaw = wrapAngleTo180(client.player.getYaw());   // 偏航角 (Yaw)
        float pitch = client.player.getPitch();              // 俯仰角 (Pitch)
        String direction = getDirectionFromYaw(yaw);         // 方向 (Direction)

        // 获取当前FPS
        long fps = client.getCurrentFps();

        // 获取当前维度
        String dimension = client.world.getRegistryKey().getValue().toString();

        // 计算另一维度的坐标
        String otherDimensionCoords = "";
        if (dimension.equals("minecraft:overworld")) {
            // 如果在主世界，则计算下界坐标
            otherDimensionCoords = String.format("%s: %.3f, %.3f, %.3f", I18n.translate("coordinate_hud.nether_coord"), playerPos.getX() / 8, playerPos.getY(), playerPos.getZ() / 8);
        } else if (dimension.equals("minecraft:the_nether")) {
            // 如果在下界，则计算主世界坐标
            otherDimensionCoords = String.format("%s: %.3f, %.3f, %.3f", I18n.translate("coordinate_hud.overworld_coord"), playerPos.getX() * 8, playerPos.getY(), playerPos.getZ() * 8);
        } else {
            // 其他维度显示不可用
            otherDimensionCoords = String.format("(%s)", I18n.translate("coordinate_hud.other_dim_coord.na"));
        }

        // 计算视野内的实体数量和世界中的总实体数量
        int[] entityCounts = getEntitiesInViewAndTotal(client.player, yaw, pitch);
        int entityCountInView = entityCounts[0];
        int totalEntityCount = entityCounts[1];

        // 获取地表高度
        int surfaceHeight = getSurfaceHeight(client.world, playerBlockPos.getX(), playerBlockPos.getZ());

        // 获取温度信息
        float temperature = client.world.getBiome(playerBlockPos).value().getTemperature();

        // 准备要显示的文本行
        String coordsText = String.format("%s: %.3f, %.3f, %.3f | %s: %.2f %s: %.2f | %s: %s | %s: %d",
                I18n.translate("coordinate_hud.coord"), playerPos.getX(), playerPos.getY(), playerPos.getZ(),
                I18n.translate("coordinate_hud.yaw"), yaw, I18n.translate("coordinate_hud.pitch"), pitch,
                I18n.translate("coordinate_hud.direction"), direction, I18n.translate("coordinate_hud.fps"), fps);
        String chunkText = String.format("%s: %d, %d, %d", I18n.translate("coordinate_hud.chunk_rel_coord"), blockX, blockY, blockZ);
        String biomeText = I18n.translate("coordinate_hud.biome") + ": " + biomeName;
        String otherCoordsText = otherDimensionCoords;
        String entitiesText = String.format("%s: %d/%d", I18n.translate("coordinate_hud.entities"), entityCountInView, totalEntityCount);
        String surfaceHeightText = String.format("%s: %d", I18n.translate("coordinate_hud.surface_height"), surfaceHeight);
        String temperatureText = String.format("%s: %.2f", I18n.translate("coordinate_hud.temperature"), temperature);

        // 绘制文本行
        context.drawText(client.textRenderer, coordsText, HUD_START_X, HUD_START_Y + 0 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        context.drawText(client.textRenderer, chunkText, HUD_START_X, HUD_START_Y + 1 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        context.drawText(client.textRenderer, biomeText, HUD_START_X, HUD_START_Y + 2 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        context.drawText(client.textRenderer, otherCoordsText, HUD_START_X, HUD_START_Y + 3 * HUD_LINE_HEIGHT, HUD_COLOR, true);       
        context.drawText(client.textRenderer, entitiesText, HUD_START_X, HUD_START_Y + 4 * HUD_LINE_HEIGHT, HUD_COLOR, true);     
        context.drawText(client.textRenderer, surfaceHeightText, HUD_START_X, HUD_START_Y + 5 * HUD_LINE_HEIGHT, HUD_COLOR, true);
        context.drawText(client.textRenderer, temperatureText, HUD_START_X, HUD_START_Y + 6 * HUD_LINE_HEIGHT, HUD_COLOR, true);
    }

    /**
     * 计算玩家视野内的实体数量和世界中的总实体数量
     * @param player PlayerEntity对象
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @return 包含视野内实体数量和总实体数量的数组，索引0为视野内数量，索引1为总数量
     */
    public static int[] getEntitiesInViewAndTotal(PlayerEntity player, float yaw, float pitch) {
        // 获取玩家的视角方向向量
        Vec3d playerViewVector = getPlayerViewVector(yaw, pitch);

        // 获取世界中的所有实体
        Iterable<Entity> entities = client.world.getEntities();

        // 初始化计数器
        int totalEntityCount = 0;
        int entityCountInView = 0;

        // 根据游戏FOV设置获取最大视角范围（弧度）
        // 使用更准确的FOV计算方式，考虑视角的半角
        double maxAngle = Math.toRadians(client.options.getFov().getValue() / 2.0);

        // 遍历所有实体，同时计算总实体数量和视野内实体数量
        for (Entity entity : entities) {
            // 检查存活且不是玩家的实体
            if (entity.isAlive() && entity != player) {
                totalEntityCount++;

                // 计算从玩家眼睛位置到实体的向量
                Vec3d playerEyePos = player.getEyePos();
                Vec3d entityPos = entity.getPos();
                Vec3d toEntity = entityPos.subtract(playerEyePos);

                // 计算距离以用于近似实体在视野内的判断
                double distance = toEntity.length();
                
                // 如果距离为0，跳过该实体避免除零错误
                if (distance == 0) {
                    continue;
                }

                // 计算从玩家到实体的单位向量
                Vec3d entityDirection = toEntity.normalize();

                // 计算玩家视角向量与实体方向向量之间的夹角（弧度）
                double angle = getAngleBetweenVectors(playerViewVector, entityDirection);

                // 如果夹角小于等于最大视角范围，则认为该实体在玩家视野内
                if (angle <= maxAngle) {
                    entityCountInView++;
                }
            }
        }

        return new int[]{entityCountInView, totalEntityCount};
    }

    /**
     * 根据玩家的偏航角和俯仰角计算视角方向向量
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @return 玩家视角方向的单位向量
     */
    private static Vec3d getPlayerViewVector(float yaw, float pitch) {
        // 将角度转换为弧度
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        
        // 根据Minecraft坐标系计算方向向量分量
        // x = -sin(yaw) * cos(pitch)
        // y = -sin(pitch)
        // z = cos(yaw) * cos(pitch)
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        // 返回玩家视角方向的单位向量
        return new Vec3d(x, y, z);
    }

    /**
     * 计算两个向量之间的夹角（弧度）
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 两个向量之间的夹角（弧度）
     */
    private static double getAngleBetweenVectors(Vec3d vector1, Vec3d vector2) {
        // 使用向量点积公式计算夹角: cos(θ) = (A·B) / (|A|*|B|)
        // 由于vector1和vector2都是单位向量，所以|A|*|B| = 1
        double dotProduct = vector1.dotProduct(vector2);
        // 通过反余弦函数得到弧度值
        return Math.acos(dotProduct);
    }

    /**
     * 将角度标准化到[-180, 180)范围内
     * @param angle 输入角度
     * @return 标准化后的角度
     */
    private static float wrapAngleTo180(float angle) {
        // 对360取模，将角度限制在[0, 360)范围内
        angle = angle % 360;
        // 将角度调整到[-180, 180)范围内
        if (angle >= 180) {
            angle -= 360;
        }
        if (angle < -180) {
            angle += 360;
        }
        return angle;
    }
    
    /**
     * 根据角度确定方向
     * @param yaw 偏航角
     * @return 方向字符串
     */
    private String getDirectionFromYaw(float yaw) {
        if (yaw >= -45 && yaw < 45) {
            return I18n.translate("coordinate_hud.direction.south"); // 南
        } else if (yaw >= 45 && yaw < 135) {
            return I18n.translate("coordinate_hud.direction.west");  // 西
        } else if (yaw >= -135 && yaw < -45) {
            return I18n.translate("coordinate_hud.direction.east");  // 东
        } else {
            return I18n.translate("coordinate_hud.direction.north"); // 北
        }
    }

    /**
     * 获取指定XZ坐标下的地表高度（Y坐标）
     *
     * @param world 当前世界对象
     * @param x X坐标
     * @param z Z坐标
     * @return 地表高度Y坐标
     */
    public static int getSurfaceHeight(net.minecraft.world.World world, int x, int z) {
        // 使用World.getTopY方法获取指定XZ坐标的最高点
        return world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z);
    }
}
