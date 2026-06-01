package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
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
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CookingStationBlock extends KineticBlock implements IBE<CookingStationBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LEFT_SHAFT = BooleanProperty.create("left_shaft");
    public static final BooleanProperty RIGHT_SHAFT = BooleanProperty.create("right_shaft");
    public static final BooleanProperty HAS_DONER = BooleanProperty.create("has_doner");
    public static final BooleanProperty COOKED = BooleanProperty.create("cooked");

    private static final VoxelShape OUTER_NORTH = box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape OUTER_SOUTH = box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape OUTER_WEST = box(0, 0, 2, 2, 16, 14);
    private static final VoxelShape OUTER_EAST = box(14, 0, 2, 16, 16, 14);
    private static final VoxelShape FRAME_TOP = box(2, 14, 2, 14, 16, 14);
    private static final VoxelShape FRAME_BOTTOM = box(2, 0, 2, 14, 2, 14);
    private static final VoxelShape VERTICAL_SHAFT = box(6, 0, 6, 10, 16, 10);
    
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[4];

    public CookingStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(LEFT_SHAFT, true)
                .setValue(RIGHT_SHAFT, true)
                .setValue(HAS_DONER, false)
                .setValue(COOKED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        int f = facing.get2DDataValue();

        if (SHAPE_CACHE[f] == null) {
            SHAPE_CACHE[f] = Shapes.or(OUTER_NORTH, OUTER_SOUTH, OUTER_WEST, OUTER_EAST, FRAME_TOP, FRAME_BOTTOM, VERTICAL_SHAFT);
        }
        return SHAPE_CACHE[f];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEFT_SHAFT, RIGHT_SHAFT, HAS_DONER, COOKED);
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
        if (face.getAxis().isVertical()) return true;
        if (face.getAxis() == state.getValue(FACING).getAxis()) return false;
        Direction facing = state.getValue(FACING);
        if (face == facing.getCounterClockWise()) return state.getValue(LEFT_SHAFT);
        if (face == facing.getClockWise()) return state.getValue(RIGHT_SHAFT);
        return false;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return null; 
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        if (face == left || face == right) {
            BooleanProperty property = face == left ? LEFT_SHAFT : RIGHT_SHAFT;
            BlockState newState = state.cycle(property);
            if (!newState.canSurvive(level, pos))
                return InteractionResult.PASS;

            KineticBlockEntity.switchToBlockState(level, pos, updateAfterWrenched(newState, context));
            refreshKinetics(level, pos);
            refreshKinetics(level, pos.relative(face));

            if (level.getBlockState(pos) != state)
                IWrenchable.playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        BlockState rotated = getRotatedBlockState(state, face);
        if (!rotated.canSurvive(level, pos))
            return InteractionResult.PASS;
        KineticBlockEntity.switchToBlockState(level, pos, updateAfterWrenched(rotated, context));
        if (level.getBlockState(pos) != state)
            IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    private static void refreshKinetics(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KineticBlockEntity kbe) {
            kbe.detachKinetics();
            kbe.attachKinetics();
        }
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.FAST;
    }

    @Override
    public Class<CookingStationBlockEntity> getBlockEntityClass() {
        return CookingStationBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CookingStationBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COOKING_STATION.get();
    }
}
