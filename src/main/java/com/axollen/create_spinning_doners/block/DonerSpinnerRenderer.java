package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class DonerSpinnerRenderer extends KineticBlockEntityRenderer<DonerSpinnerBlockEntity> {
    public DonerSpinnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(DonerSpinnerBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = getRenderedBlockState(be);
        RenderType renderType = getRenderType(be, blockState);
        renderRotatingBuffer(be, getRotatedModel(be, blockState), ms,
                buffer.getBuffer(renderType), light);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(DonerSpinnerBlockEntity be, BlockState state) {
        if (state.getValue(DonerSpinnerBlock.HALF) == DoubleBlockHalf.LOWER) {
            return CachedBuffers.partial(ModPartialModels.SPINNING_SHAFT, state);
        }
        if (!state.getValue(DonerSpinnerBlock.HAS_DONER)) {
            return CachedBuffers.partial(ModPartialModels.SPINNING_SHAFT, state);
        }

        int pieces = be.getPiecesRemaining();
        boolean cooked = state.getValue(DonerSpinnerBlock.COOKED);

        if (pieces >= 25) {
            return CachedBuffers.partial(cooked ? ModPartialModels.SPINNING_COOKED_THICK : ModPartialModels.SPINNING_RAW_THICK, state);
        } else if (pieces >= 13) {
            return CachedBuffers.partial(cooked ? ModPartialModels.SPINNING_COOKED_MEDIUM : ModPartialModels.SPINNING_RAW_MEDIUM, state);
        } else if (pieces > 0) {
            return CachedBuffers.partial(cooked ? ModPartialModels.SPINNING_COOKED_THIN : ModPartialModels.SPINNING_RAW_THIN, state);
        }
        return CachedBuffers.partial(ModPartialModels.SPINNING_SHAFT, state);
    }
}
