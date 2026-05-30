package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SidewaysHeaterBlock extends KineticBlock implements IBE<SidewaysHeaterBlockEntity>, IRotate {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BACK_SHAFT = BooleanProperty.create("has_back_shaft");
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, 4);

    private static final VoxelShape MAIN_SHAPE = box(0, 1, 0, 16, 15, 16);
    private static final VoxelShape SHAFT_SHAPE = box(6, 0, 6, 10, 16, 10);

    public SidewaysHeaterBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(HEAT_LEVEL) * 3));
        registerDefaultState(defaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(HAS_BACK_SHAFT, false)
            .setValue(HEAT_LEVEL, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(MAIN_SHAPE, SHAFT_SHAPE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BACK_SHAFT, HEAT_LEVEL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        if (face == Direction.UP || face == Direction.DOWN) return true;
        return state.getValue(HAS_BACK_SHAFT) && face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.FAST;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Direction side = context.getClickedFace();
        if (side == state.getValue(FACING).getOpposite()) {
            BlockState newState = state.cycle(HAS_BACK_SHAFT);
            if (!newState.canSurvive(context.getLevel(), context.getClickedPos()))
                return InteractionResult.PASS;

            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
                updateAfterWrenched(newState, context));

            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
            if (be instanceof GeneratingKineticBlockEntity gen)
                gen.reActivateSource = true;

            BlockPos backPos = context.getClickedPos().relative(state.getValue(FACING).getOpposite());
            BlockEntity backBe = context.getLevel().getBlockEntity(backPos);
            if (backBe instanceof KineticBlockEntity kbe) {
                kbe.detachKinetics();
                kbe.attachKinetics();
            }

            if (context.getLevel().getBlockState(context.getClickedPos()) != state)
                IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());

            return InteractionResult.SUCCESS;
        }
        return super.onWrenched(state, context);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState state, Direction side) {
        if (side == state.getValue(FACING).getOpposite()) return state;
        if (side.getAxis().isVertical())
            return state.setValue(FACING, state.getValue(FACING).getClockWise());
        return state.setValue(FACING, side.getOpposite());
    }

    @Override
    public Class<SidewaysHeaterBlockEntity> getBlockEntityClass() {
        return SidewaysHeaterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SidewaysHeaterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SIDEWAYS_HEATER.get();
    }
}
