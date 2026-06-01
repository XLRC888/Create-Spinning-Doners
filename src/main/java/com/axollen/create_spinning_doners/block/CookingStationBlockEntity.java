package com.axollen.create_spinning_doners.block;

import com.axollen.create_spinning_doners.registry.ModItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class CookingStationBlockEntity extends KineticBlockEntity {
    public static BlockEntityType<CookingStationBlockEntity> TYPE;

    public enum DonerState { EMPTY, RAW, COOKING, COOKED }

    public static final int MINIMUM_RPM = 64;
    public static final int TIER_NONE = 0;
    public static final int TIER_LOW = 1;
    public static final int TIER_MEDIUM = 2;
    public static final int TIER_HIGH = 3;
    public static final int TIER_MAX = 4;

    private DonerState donerState = DonerState.EMPTY;
    private int cookingProgress = 0;
    private int cookingThreshold = 1200;
    private int syncCooldown = 0;

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

    public CookingStationBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public DonerState getDonerState() {
        return donerState;
    }

    public void placeDoner() {
        donerState = DonerState.RAW;
        cookingProgress = 0;
        setChanged();
        sendData();
        syncBlockState();
    }

    public int getCookingSpeed() {
        return switch (getHeatLevel()) {
            case TIER_NONE -> 0;
            case TIER_LOW -> 1;
            case TIER_MEDIUM -> 2;
            case TIER_HIGH -> 4;
            case TIER_MAX -> 12;
            default -> 0;
        };
    }

    public int getHeatLevel() {
        float speed = Math.abs(getSpeed());
        if (speed < 64) return TIER_NONE;
        if (speed < 128) return TIER_LOW;
        if (speed < 192) return TIER_MEDIUM;
        if (speed < 256) return TIER_HIGH;
        return TIER_MAX;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        processInput();

        if (donerState == DonerState.EMPTY) return;

        if (donerState == DonerState.RAW || donerState == DonerState.COOKING) {
            int heat = getCookingSpeed();
            DonerState prevState = donerState;
            if (heat > 0) {
                cookingProgress += heat;
                if (donerState == DonerState.RAW) donerState = DonerState.COOKING;
                if (cookingProgress >= cookingThreshold) {
                    cookingProgress = cookingThreshold;
                    donerState = DonerState.COOKED;
                    syncBlockState();
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

        if (donerState == DonerState.COOKED) {
            outputCookedDoner();
        }
    }

    private void processInput() {
        ItemStack input = inputInv.getStackInSlot(0);
        if (!input.isEmpty() && donerState == DonerState.EMPTY && input.is(ModItems.RAW_FULL_DONER.get())) {
            inputInv.extractItem(0, 1, false);
            placeDoner();
        }
    }

    private void outputCookedDoner() {
        ItemStack cooked = new ItemStack(ModItems.COOKED_FULL_DONER.get());
        if (!insertToOutput(cooked)) {
            ejectItem(cooked);
        }
        donerState = DonerState.EMPTY;
        cookingProgress = 0;
        setChanged();
        sendData();
        syncBlockState();
    }

    private void syncBlockState() {
        if (level == null) return;
        BlockState state = getBlockState();
        boolean shouldHaveDoner = donerState != DonerState.EMPTY;
        boolean shouldBeCooked = donerState == DonerState.COOKED;
        boolean changed = false;
        if (state.getValue(CookingStationBlock.HAS_DONER) != shouldHaveDoner) {
            state = state.setValue(CookingStationBlock.HAS_DONER, shouldHaveDoner);
            changed = true;
        }
        if (shouldHaveDoner && state.getValue(CookingStationBlock.COOKED) != shouldBeCooked) {
            state = state.setValue(CookingStationBlock.COOKED, shouldBeCooked);
            changed = true;
        }
        if (changed) {
            level.setBlock(worldPosition, state, 3);
        }
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

    public void dropContents() {
        if (level == null || level.isClientSide) return;
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
    public boolean isSpeedRequirementFulfilled() {
        return Math.abs(getSpeed()) >= MINIMUM_RPM;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        MutableComponent spacing = Component.literal("    ");

        if (donerState == DonerState.COOKED) {
            MutableComponent statusText = Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Cooked!").withStyle(ChatFormatting.GREEN));
            tooltip.add(spacing.plainCopy().append(statusText));
        } else if (donerState != DonerState.EMPTY) {
            int heat = getCookingSpeed();
            MutableComponent cookingText;
            if (heat > 0) {
                cookingText = Component.literal("Cooking: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(cookingProgress + " / " + cookingThreshold).withStyle(ChatFormatting.AQUA));
            } else {
                cookingText = Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Cooling").withStyle(ChatFormatting.RED));
            }
            tooltip.add(spacing.plainCopy().append(cookingText));
        }

        String heatName = switch (getHeatLevel()) {
            case TIER_NONE -> "None";
            case TIER_LOW -> "Low";
            case TIER_MEDIUM -> "Medium";
            case TIER_HIGH -> "High";
            case TIER_MAX -> "Max";
            default -> "Unknown";
        };

        MutableComponent heatText = Component.literal("Heat Level: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(heatName).withStyle(ChatFormatting.GOLD));
        tooltip.add(spacing.plainCopy().append(heatText));

        MutableComponent speedText = Component.literal("Cooking Speed: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(getCookingSpeed()) + "x").withStyle(ChatFormatting.AQUA));
        tooltip.add(spacing.plainCopy().append(speedText));

        if (Math.abs(getSpeed()) < MINIMUM_RPM) {
            MutableComponent warnText = Component.literal("Needs at least " + MINIMUM_RPM + " RPM").withStyle(ChatFormatting.RED);
            tooltip.add(spacing.plainCopy().append(warnText));
        }

        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            BlockState state = getBlockState();
            Direction facing = state.getValue(CookingStationBlock.FACING);
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
        donerState = DonerState.values()[tag.getInt("DonerState")];
        cookingProgress = tag.getInt("Cooking");
        if (tag.contains("OutputInv")) outputInv.deserializeNBT(tag.getCompound("OutputInv"));
        if (tag.contains("InputInv")) inputInv.deserializeNBT(tag.getCompound("InputInv"));
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("DonerState", donerState.ordinal());
        tag.putInt("Cooking", cookingProgress);
        tag.put("OutputInv", outputInv.serializeNBT());
        tag.put("InputInv", inputInv.serializeNBT());
    }
}
