package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class DonerSpinnerBlockEntity extends KineticBlockEntity {
    public static BlockEntityType<DonerSpinnerBlockEntity> TYPE;

    public enum DonerState { EMPTY, RAW, COOKING, COOKED }

    public enum ChopMode implements INamedIconOptions {
        WHEN_COOKED(AllIcons.I_PAUSE),
        ALWAYS(AllIcons.I_PLAY);

        private final AllIcons icon;

        ChopMode(AllIcons icon) { this.icon = icon; }

        @Override
        public AllIcons getIcon() { return icon; }

        @Override
        public String getTranslationKey() {
            return "create_spinning_doners.chop_mode." + name().toLowerCase();
        }
    }

    private DonerState donerState = DonerState.EMPTY;
    private int piecesRemaining = 0;
    private int cookingProgress = 0;
    private int cookingThreshold = 1200;
    private int cutCooldown = 0;
    private int syncCooldown = 0;

    public ScrollOptionBehaviour<ChopMode> chopMode;

    public final ItemStackHandler outputInv = new ItemStackHandler(9);
    public final ItemStackHandler inputInv = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.RAW_FULL_DONER.get());
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.is(ModItems.RAW_FULL_DONER.get()) && donerState == DonerState.EMPTY) {
                if (!simulate) {
                    placeDoner();
                }
                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                return remainder;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private LazyOptional<ItemStackHandler> outputCap = LazyOptional.of(() -> outputInv);
    private LazyOptional<ItemStackHandler> inputCap = LazyOptional.of(() -> inputInv);

    public DonerSpinnerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        if (isTop()) return;
        chopMode = new ScrollOptionBehaviour<>(ChopMode.class,
            Component.literal("Chop Mode"), this, new CenteredSideValueBoxTransform(
                (state, dir) -> {
                    if (dir.getAxis() == Axis.Y) return false;
                    Direction facing = state.getValue(DonerSpinnerBlock.FACING);
                    return dir.getAxis() != facing.getAxis();
                }));
        chopMode.withCallback(i -> {
            setChanged();
            sendData();
        });
        behaviours.add(chopMode);
    }

    private boolean isTop() {
        return getBlockState().getValue(DonerSpinnerBlock.HALF) == DoubleBlockHalf.UPPER;
    }

    private DonerSpinnerBlockEntity findBottomBE() {
        if (level == null || !isTop()) return this;
        BlockPos below = worldPosition.below();
        if (level.getBlockEntity(below) instanceof DonerSpinnerBlockEntity be && !be.isTop()) {
            return be;
        }
        return null;
    }

    public DonerState getDonerState() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            return bottom != null ? bottom.donerState : DonerState.EMPTY;
        }
        return donerState;
    }

    public int getPiecesRemaining() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            return bottom != null ? bottom.piecesRemaining : 0;
        }
        return piecesRemaining;
    }

    public int getCookingProgress() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            return bottom != null ? bottom.cookingProgress : 0;
        }
        return cookingProgress;
    }

    public void placeDoner() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            if (bottom != null) bottom.placeDoner();
            return;
        }
        donerState = DonerState.RAW;
        piecesRemaining = 50;
        cookingProgress = 0;
        cutCooldown = 0;
        setChanged();
        sendData();
        if (level != null) DonerSpinnerBlock.syncTopDonerState(level, worldPosition);
    }

    public int getCookingSpeed() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            return bottom != null ? bottom.getCookingSpeed() : 0;
        }
        if (level == null) return 0;
        BlockPos topPos = worldPosition.above();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = topPos.relative(dir);
            BlockState state = level.getBlockState(neighbor);
            if (state.getBlock() instanceof SidewaysHeaterBlock) {
                Direction heaterFacing = state.getValue(SidewaysHeaterBlock.FACING);
                if (heaterFacing.getOpposite() == dir) {
                    if (level.getBlockEntity(neighbor) instanceof SidewaysHeaterBlockEntity heater) {
                        return heater.getCookingSpeed();
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        if (isTop()) {
            syncVisualState();
            return;
        }

        processInput();
        if (donerState == DonerState.EMPTY) return;

        boolean spinning = Math.abs(getSpeed()) > 0;

        if (donerState == DonerState.RAW || donerState == DonerState.COOKING) {
            int heat = getCookingSpeed();
            DonerState prevState = donerState;
            if (heat > 0) {
                cookingProgress += heat;
                if (donerState == DonerState.RAW) donerState = DonerState.COOKING;
                if (cookingProgress >= cookingThreshold) {
                    cookingProgress = cookingThreshold;
                    donerState = DonerState.COOKED;
                }
                setChanged();
            } else if (cookingProgress > 0) {
                cookingProgress = Math.max(0, cookingProgress - 2);
                if (cookingProgress == 0) donerState = DonerState.RAW;
                setChanged();
            }
            if (donerState != prevState) {
                sendData();
                syncCooldown = 0;
            } else if (--syncCooldown <= 0) {
                sendData();
                syncCooldown = 20;
            }
        }

        if (spinning && level.hasNeighborSignal(worldPosition)) {
            boolean alwaysChop = chopMode != null && chopMode.get() == ChopMode.ALWAYS;
            boolean canChop = alwaysChop ? donerState != DonerState.EMPTY
                                         : donerState == DonerState.COOKED;

            if (canChop) {
                int interval = Math.max(1, (int)(320.0f / Math.abs(getSpeed())));
                cutCooldown++;
                if (cutCooldown >= interval) {
                    cutCooldown = 0;
                    chopPiece();
                    level.playSound(null, worldPosition, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.6f, 1.0f);
                }
            }
        } else {
            cutCooldown = 0;
        }
    }

    private void syncVisualState() {
        DonerSpinnerBlockEntity bottom = findBottomBE();
        if (bottom != null) {
            BlockState state = getBlockState();
            boolean shouldHaveDoner = bottom.donerState != DonerState.EMPTY;
            boolean shouldBeCooked = bottom.donerState == DonerState.COOKED;
            boolean changed = false;
            if (state.getValue(DonerSpinnerBlock.HAS_DONER) != shouldHaveDoner) {
                state = state.setValue(DonerSpinnerBlock.HAS_DONER, shouldHaveDoner);
                changed = true;
            }
            if (shouldHaveDoner && state.getValue(DonerSpinnerBlock.COOKED) != shouldBeCooked) {
                state = state.setValue(DonerSpinnerBlock.COOKED, shouldBeCooked);
                changed = true;
            }
            if (changed) {
                level.setBlock(worldPosition, state, 3);
            }
        }
    }

    public void chopDirectToInventory(Player player, ItemStack tool) {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            if (bottom != null) bottom.chopDirectToInventory(player, tool);
            return;
        }
        if (piecesRemaining <= 0 || donerState == DonerState.EMPTY) return;
        if (!isKnife(tool)) return;

        ItemStack piece = getPieceForState();
        if (!player.getInventory().add(piece)) {
            Vec3 dropPos = Vec3.atCenterOf(worldPosition.above());
            level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, piece));
        }

        piecesRemaining--;
        if (!player.getAbilities().instabuild && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            tool.hurt(1, level.random, sp);
        }
        if (piecesRemaining <= 0) {
            donerState = DonerState.EMPTY;
            DonerSpinnerBlock.syncTopDonerState(level, worldPosition);
        }
        setChanged();
        sendData();
    }

    private boolean isKnife(ItemStack stack) {
        return stack.is(Items.SHEARS) ||
               stack.is(ItemTags.SWORDS) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "tools/knives"))) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight", "knives"))) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("farmersdelight", "tools/knives"))) ||
               stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("create_spinning_doners", "doner_spinner_tools")));
    }

    private ItemStack getPieceForState() {
        return new ItemStack(donerState == DonerState.COOKED
                ? ModItems.COOKED_DONER_PIECE.get()
                : ModItems.RAW_DONER_PIECE.get());
    }

    private void processInput() {
        ItemStack input = inputInv.getStackInSlot(0);
        if (!input.isEmpty() && donerState == DonerState.EMPTY && input.is(ModItems.RAW_FULL_DONER.get())) {
            inputInv.extractItem(0, 1, false);
            placeDoner();
        }
    }

    private void chopPiece() {
        if (level == null) return;

        ItemStack piece = getPieceForState();

        if (!insertToOutput(piece)) {
            ejectItem(piece);
        }

        piecesRemaining--;
        if (piecesRemaining <= 0) {
            donerState = DonerState.EMPTY;
            DonerSpinnerBlock.syncTopDonerState(level, worldPosition);
        }
        setChanged();
        sendData();
    }

    boolean insertToOutput(ItemStack stack) {
        for (int i = 0; i < outputInv.getSlots(); i++) {
            stack = outputInv.insertItem(i, stack, false);
            if (stack.isEmpty()) return true;
        }
        return false;
    }

    private void ejectItem(ItemStack stack) {
        if (level == null) return;
        Vec3 pos = Vec3.atCenterOf(worldPosition);
        level.addFreshEntity(new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, stack));
    }

    public ItemStack getRetrieveDonerItem() {
        return new ItemStack(ModItems.RAW_FULL_DONER.get());
    }

    public void clearDoner() {
        if (isTop()) {
            DonerSpinnerBlockEntity bottom = findBottomBE();
            if (bottom != null) bottom.clearDoner();
            return;
        }
        donerState = DonerState.EMPTY;
        piecesRemaining = 0;
        cookingProgress = 0;
        cutCooldown = 0;
        setChanged();
        sendData();
    }

    public void dropContents() {
        if (level == null || level.isClientSide || isTop()) return;
        for (int i = 0; i < outputInv.getSlots(); i++) {
            ItemStack stack = outputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack));
            }
        }
        ItemStack input = inputInv.getStackInSlot(0);
        if (!input.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, input));
        }
    }

    @Override
    public float calculateStressApplied() {
        return 1f;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (isTop()) return added;
        if (donerState == DonerState.EMPTY) return added;

        MutableComponent spacing = Component.literal("    ");

        MutableComponent chopsText = Component.literal("Remaining Chops: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(piecesRemaining)).withStyle(ChatFormatting.AQUA));
        tooltip.add(spacing.plainCopy().append(chopsText));

        int heat = getCookingSpeed();
        if (donerState == DonerState.RAW || donerState == DonerState.COOKING || donerState == DonerState.COOKED) {
            MutableComponent cookingText;
            if (donerState == DonerState.COOKED) {
                cookingText = Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Cooked!").withStyle(ChatFormatting.GREEN));
            } else if (heat > 0) {
                cookingText = Component.literal("Cooking: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(cookingProgress + " / " + cookingThreshold).withStyle(ChatFormatting.AQUA));
            } else {
                cookingText = Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Cooling").withStyle(ChatFormatting.RED));
            }
            tooltip.add(spacing.plainCopy().append(cookingText));
        }

        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (isTop()) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            BlockState state = getBlockState();
            Direction facing = state.getValue(DonerSpinnerBlock.FACING);
            if (side == facing) return inputCap.cast();
            if (side == facing.getOpposite()) return outputCap.cast();
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        outputCap.invalidate();
        inputCap.invalidate();
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (isTop()) return;
        donerState = DonerState.values()[tag.getInt("DonerState")];
        piecesRemaining = tag.getInt("Pieces");
        cookingProgress = tag.getInt("Cooking");
        cutCooldown = tag.getInt("CutCooldown");
        if (tag.contains("OutputInv")) outputInv.deserializeNBT(tag.getCompound("OutputInv"));
        if (tag.contains("InputInv")) inputInv.deserializeNBT(tag.getCompound("InputInv"));
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (isTop()) return;
        tag.putInt("DonerState", donerState.ordinal());
        tag.putInt("Pieces", piecesRemaining);
        tag.putInt("Cooking", cookingProgress);
        tag.putInt("CutCooldown", cutCooldown);
        tag.put("OutputInv", outputInv.serializeNBT());
        tag.put("InputInv", inputInv.serializeNBT());
    }

}
