package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public class CookingStationRenderer extends KineticBlockEntityRenderer<CookingStationBlockEntity> {
    public CookingStationRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CookingStationBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = getRenderedBlockState(be);
        RenderType renderType = getRenderType(be, blockState);
        Direction facing = blockState.getValue(CookingStationBlock.FACING);
        Axis horizontalAxis = facing.getClockWise().getAxis();

        
        SuperByteBuffer verticalShaft = CachedBuffers.partial(ModPartialModels.SPINNING_SHAFT, blockState);
        float verticalAngle = getAngleForBe(be, be.getBlockPos(), Axis.Y);
        kineticRotationTransform(verticalShaft, be, Axis.Y, verticalAngle, light);
        verticalShaft.renderInto(ms, buffer.getBuffer(renderType));

        
        float horizontalAngle = getAngleForBe(be, be.getBlockPos(), horizontalAxis);
        if (blockState.getValue(CookingStationBlock.LEFT_SHAFT))
            renderSideShaft(be, ms, buffer, renderType, light, horizontalAxis, horizontalAngle, facing.getCounterClockWise());
        if (blockState.getValue(CookingStationBlock.RIGHT_SHAFT))
            renderSideShaft(be, ms, buffer, renderType, light, horizontalAxis, horizontalAngle, facing.getClockWise());
    }

    private void renderSideShaft(CookingStationBlockEntity be, PoseStack ms, MultiBufferSource buffer,
                                 RenderType renderType, int light, Axis axis, float angle, Direction side) {
        SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), side);
        kineticRotationTransform(shaft, be, axis, angle, light);
        shaft.renderInto(ms, buffer.getBuffer(renderType));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CookingStationBlockEntity be, BlockState state) {
        return CachedBuffers.partial(ModPartialModels.SPINNING_SHAFT, state);
    }
}
