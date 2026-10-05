package com.argentinaflags.client;

import com.argentinaflags.ArgentinaFlags;
import com.argentinaflags.block.FlagBlock;
import com.argentinaflags.block.FlagBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dibuja la tela de las banderas.
 * - Bandera de suelo/mastil (wall=false): la tela ondea con una onda senoidal anclada en el mastil.
 * - Bandera de pared (wall=true): la tela es completamente estatica.
 *
 * Es solo geometria del renderer: no hay entidades ni actualizaciones en el servidor.
 * Todas las medidas estan en unidades de 1/16 de bloque, con la bandera mirando al norte
 * (el renderer rota todo segun FACING).
 */
public class FlagRenderer implements BlockEntityRenderer<FlagBlockEntity> {

    private static final ResourceLocation PLANKS =
            ResourceLocation.withDefaultNamespace("textures/block/spruce_planks.png");
    private static final ResourceLocation GOLD =
            ResourceLocation.withDefaultNamespace("textures/block/gold_block.png");

    /** La bandera ocupa 112x72 px arriba a la izquierda de un lienzo de 128x128. */
    private static final float U_MAX = 112f / 128f;
    private static final float V_MAX = 72f / 128f;

    private static final int ROWS = 4;

    public FlagRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FlagBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof FlagBlock block)) {
            return;
        }
        boolean wall = state.getValue(FlagBlock.WALL);
        boolean large = block.isLarge();
        Direction facing = state.getValue(FlagBlock.FACING);

        // Geometria de la tela (ancho x alto), borde del mastil/barra (xRight) y borde superior (yTop)
        float width;
        float height;
        float xRight;
        float yTop;
        float z;
        if (wall) {
            width = large ? 56f : 28f;
            height = large ? 36f : 18f;
            xRight = large ? 36f : 22f;
            yTop = 13f;
            z = 12.75f;
        } else {
            width = large ? 42f : 21f;
            height = large ? 27f : 13.5f;
            xRight = 5f;
            yTop = large ? 29f : 16f;
            z = 8f;
        }

        Level level = entity.getLevel();
        float time = level == null ? 0f : (level.getGameTime() + partialTick);
        float amplitude = wall ? 0f : (large ? 4.0f : 2.2f);
        float waveNumber = (float) (2 * Math.PI / (width * 0.55f));
        int columns = large ? 24 : 16;

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                ArgentinaFlags.MOD_ID, "textures/block/" + block.getTextureId() + ".png");

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(facing.toYRot() + 180.0f)));
        poseStack.translate(-0.5, 0.0, -0.5);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer cloth = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        drawCloth(cloth, pose, xRight, yTop, z, width, height, columns, amplitude, waveNumber, time * 0.2f, packedLight);

        // La barra de las banderas grandes de pared supera el limite de tamano de los modelos JSON
        if (wall && large) {
            VertexConsumer rod = bufferSource.getBuffer(RenderType.entityCutoutNoCull(PLANKS));
            box(rod, pose, -20.5f, 13f, 13f, 36.5f, 14f, 14f, packedLight);
            VertexConsumer caps = bufferSource.getBuffer(RenderType.entityCutoutNoCull(GOLD));
            box(caps, pose, -21.5f, 12.5f, 12.5f, -20.5f, 14.5f, 14.5f, packedLight);
            box(caps, pose, 36.5f, 12.5f, 12.5f, 37.5f, 14.5f, 14.5f, packedLight);
        }

        poseStack.popPose();
    }

    /** Malla de la tela: u=0 en el borde del mastil; la onda crece con la distancia al mastil. */
    private static void drawCloth(VertexConsumer vc, PoseStack.Pose pose, float xRight, float yTop, float z0,
                                  float width, float height, int columns, float amplitude, float waveNumber,
                                  float t, int light) {
        for (int i = 0; i < columns; i++) {
            float u0 = i / (float) columns;
            float u1 = (i + 1) / (float) columns;
            for (int j = 0; j < ROWS; j++) {
                float v0 = j / (float) ROWS;
                float v1 = (j + 1) / (float) ROWS;

                float x0 = xRight - u0 * width;
                float x1 = xRight - u1 * width;
                float yA = yTop - v0 * height;
                float yB = yTop - v1 * height;
                float zA0 = z0 + wave(u0, v0, width, amplitude, waveNumber, t);
                float zA1 = z0 + wave(u1, v0, width, amplitude, waveNumber, t);
                float zB0 = z0 + wave(u0, v1, width, amplitude, waveNumber, t);
                float zB1 = z0 + wave(u1, v1, width, amplitude, waveNumber, t);

                float tu0 = u0 * U_MAX;
                float tu1 = u1 * U_MAX;
                float tv0 = v0 * V_MAX;
                float tv1 = v1 * V_MAX;

                // Cara delantera (mira al norte) y trasera (espejada, como una tela estampada)
                vertex(vc, pose, x0, yA, zA0, tu0, tv0, 0, 0, -1, light);
                vertex(vc, pose, x0, yB, zB0, tu0, tv1, 0, 0, -1, light);
                vertex(vc, pose, x1, yB, zB1, tu1, tv1, 0, 0, -1, light);
                vertex(vc, pose, x1, yA, zA1, tu1, tv0, 0, 0, -1, light);

                vertex(vc, pose, x1, yA, zA1, tu1, tv0, 0, 0, 1, light);
                vertex(vc, pose, x1, yB, zB1, tu1, tv1, 0, 0, 1, light);
                vertex(vc, pose, x0, yB, zB0, tu0, tv1, 0, 0, 1, light);
                vertex(vc, pose, x0, yA, zA0, tu0, tv0, 0, 0, 1, light);
            }
        }
    }

    private static float wave(float u, float v, float width, float amplitude, float waveNumber, float t) {
        if (amplitude == 0f) {
            return 0f;
        }
        return amplitude * u * (float) Math.sin(waveNumber * u * width - t + v * 1.2f);
    }

    private static void box(VertexConsumer vc, PoseStack.Pose pose, float x1, float y1, float z1,
                            float x2, float y2, float z2, int light) {
        quad(vc, pose, x2, y2, z1, x1, y2, z1, x1, y1, z1, x2, y1, z1, 0, 0, -1, light);
        quad(vc, pose, x1, y2, z2, x2, y2, z2, x2, y1, z2, x1, y1, z2, 0, 0, 1, light);
        quad(vc, pose, x2, y2, z2, x2, y2, z1, x2, y1, z1, x2, y1, z2, 1, 0, 0, light);
        quad(vc, pose, x1, y2, z1, x1, y2, z2, x1, y1, z2, x1, y1, z1, -1, 0, 0, light);
        quad(vc, pose, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0, 1, 0, light);
        quad(vc, pose, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0, -1, 0, light);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, int light) {
        vertex(vc, pose, ax, ay, az, 0f, 0f, nx, ny, nz, light);
        vertex(vc, pose, bx, by, bz, 1f, 0f, nx, ny, nz, light);
        vertex(vc, pose, cx, cy, cz, 1f, 1f, nx, ny, nz, light);
        vertex(vc, pose, dx, dy, dz, 0f, 1f, nx, ny, nz, light);
    }

    /** Las posiciones llegan en 1/16 de bloque. */
    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz, int light) {
        vc.addVertex(pose, x / 16f, y / 16f, z / 16f)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
