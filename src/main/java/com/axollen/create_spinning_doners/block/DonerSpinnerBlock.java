package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModBlockEntities;
import com.axollen.create_spinning_doners.registry.ModItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DonerSpinnerBlock extends KineticBlock implements IBE<DonerSpinnerBlockEntity>, IWrenchable {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FRONT_OUTPUT = BooleanProperty.create("front_output");
    public static final BooleanProperty BACK_OUTPUT = BooleanProperty.create("back_output");
    public static final BooleanProperty HAS_DONER = BooleanProperty.create("has_doner");
    public static final BooleanProperty COOKED = BooleanProperty.create("cooked");

    private static final VoxelShape BASE = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape CASING_NORTH = box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape CASING_SOUTH = box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape CASING_WEST = box(0, 0, 2, 2, 16, 14);
    private static final VoxelShape CASING_EAST = box(14, 0, 2, 16, 16, 14);
    
    private static final VoxelShape TOP_CASING = box(2, 15, 2, 14, 16, 14);
    private static final VoxelShape BOTTOM_CASING = box(2, 0, 2, 14, 1, 14);

    private static final VoxelShape SHAFT = box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape DONER_THICK = box(1, 4, 1, 15, 12, 15);
    private static final VoxelShape DONER_MEDIUM = box(3, 4, 3, 13, 12, 13);
    private static final VoxelShape DONER_THIN = box(5, 4, 5, 11, 12, 11);

    public DonerSpinnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH)
                .setValue(FRONT_OUTPUT, true)
                .setValue(BACK_OUTPUT, true)
                .setValue(HAS_DONER, false)
                .setValue(COOKED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            VoxelShape shape = SHAFT;
            if (state.getValue(HAS_DONER)) {
                DonerSpinnerBlockEntity be = getBlockEntity(world, pos.below());
                if (be != null) {
                    int pieces = be.getPiecesRemaining();
                    if (pieces >= 25) shape = Shapes.or(shape, DONER_THICK);
                    else if (pieces >= 13) shape = Shapes.or(shape, DONER_MEDIUM);
                    else if (pieces > 0) shape = Shapes.or(shape, DONER_THIN);
                } else {
                    shape = Shapes.or(shape, DONER_THICK);
                }
            }
            return shape;
        }

        return Shapes.or(CASING_NORTH, CASING_SOUTH, CASING_WEST, CASING_EAST, BOTTOM_CASING, TOP_CASING, SHAFT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, FRONT_OUTPUT, BACK_OUTPUT, HAS_DONER, COOKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;
        if (!level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        level.blockUpdated(pos, Blocks.AIR);
        state.updateNeighbourShapes(level, pos, 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(2001, otherPos, Block.getId(otherState));
            }
            withBlockEntityDo(level, pos, DonerSpinnerBlockEntity::dropContents);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Axis.Y;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            DonerSpinnerBlockEntity be = getBlockEntity(level, below);
            if (be == null) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);

            if (held.is(ModItems.RAW_FULL_DONER.get()) && be.getDonerState() == DonerSpinnerBlockEntity.DonerState.EMPTY) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                be.placeDoner();
                syncTopDonerState(level, pos, true);
                return InteractionResult.SUCCESS;
            }

            if (be.getDonerState() != DonerSpinnerBlockEntity.DonerState.EMPTY && isKnife(held)) {
                be.chopDirectToInventory(player, held);
                if (be.getDonerState() == DonerSpinnerBlockEntity.DonerState.EMPTY) {
                    syncTopDonerState(level, pos, false);
                }
                return InteractionResult.SUCCESS;
            }

            if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
                DonerSpinnerBlockEntity bottomBe = getBlockEntity(level, pos.below());
                if (bottomBe != null) {
                    for (int i = 0; i < bottomBe.outputInv.getSlots(); i++) {
                        ItemStack extracted = bottomBe.outputInv.extractItem(i, 64, false);
                        if (!extracted.isEmpty()) {
                            if (!player.getInventory().add(extracted)) {
                                Vec3 dropPos = Vec3.atCenterOf(pos);
                                level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y + 0.5, dropPos.z, extracted));
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                return InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }

        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            DonerSpinnerBlockEntity be = getBlockEntity(level, pos);
            if (be != null) {
                for (int i = 0; i < be.outputInv.getSlots(); i++) {
                    ItemStack extracted = be.outputInv.extractItem(i, 64, false);
                    if (!extracted.isEmpty()) {
                        if (!player.getInventory().add(extracted)) {
                            Vec3 dropPos = Vec3.atCenterOf(pos);
                            level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y + 0.5, dropPos.z, extracted));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private void syncTopDonerState(Level level, BlockPos topPos, boolean hasDoner) {
        BlockState topState = level.getBlockState(topPos);
        if (topState.is(this)) {
            level.setBlock(topPos, topState.setValue(HAS_DONER, hasDoner), 3);
        }
    }

    public static void syncTopDonerState(Level level, BlockPos bottomPos) {
        BlockPos topPos = bottomPos.above();
        BlockState topState = level.getBlockState(topPos);
        if (topState.getBlock() instanceof DonerSpinnerBlock && topState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockEntity be = level.getBlockEntity(bottomPos);
            boolean hasDoner = be instanceof DonerSpinnerBlockEntity dbe && dbe.getDonerState() != DonerSpinnerBlockEntity.DonerState.EMPTY;
            if (topState.getValue(DonerSpinnerBlock.HAS_DONER) != hasDoner) {
                level.setBlock(topPos, topState.setValue(DonerSpinnerBlock.HAS_DONER, hasDoner), 3);
            }
        }
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Direction front = state.getValue(FACING);

        if (face == front) {
            level.setBlock(pos, state.cycle(FRONT_OUTPUT), 3);
            IWrenchable.playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }
        if (face == front.getOpposite()) {
            level.setBlock(pos, state.cycle(BACK_OUTPUT), 3);
            IWrenchable.playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
        if (!rotated.canSurvive(level, context.getClickedPos()))
            return InteractionResult.PASS;
        KineticBlockEntity.switchToBlockState(level, pos, updateAfterWrenched(rotated, context));
        if (level.getBlockState(pos) != state)
            IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    private static boolean isKnife(ItemStack stack) {
        return stack.is(Items.SHEARS) ||
               stack.is(ItemTags.SWORDS) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "tools/knives"))) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight", "knives"))) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight", "tools/knives")));
    }

    @Override
    public Class<DonerSpinnerBlockEntity> getBlockEntityClass() {
        return DonerSpinnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DonerSpinnerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DONER_SPINNER.get();
    }
}
