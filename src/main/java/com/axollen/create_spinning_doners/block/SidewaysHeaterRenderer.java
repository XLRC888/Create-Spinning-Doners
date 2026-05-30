package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SidewaysHeaterRenderer extends KineticBlockEntityRenderer<SidewaysHeaterBlockEntity> {
    public SidewaysHeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SidewaysHeaterBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = getRenderedBlockState(be);
        RenderType renderType = getRenderType(be, blockState);

        renderRotatingBuffer(be, getRotatedModel(be, blockState), ms,
                buffer.getBuffer(renderType), light);

        if (blockState.getValue(SidewaysHeaterBlock.HAS_BACK_SHAFT)) {
            Direction backFace = blockState.getValue(SidewaysHeaterBlock.FACING).getOpposite();
            SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, backFace);
            float angle = getAngleForBe(be, be.getBlockPos(), backFace.getAxis());
            kineticRotationTransform(shaft, be, backFace.getAxis(), angle, light);
            shaft.renderInto(ms, buffer.getBuffer(renderType));
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SidewaysHeaterBlockEntity be, BlockState state) {
        return CachedBuffers.partial(ModPartialModels.HEATER_SHAFT, state);
    }
}
