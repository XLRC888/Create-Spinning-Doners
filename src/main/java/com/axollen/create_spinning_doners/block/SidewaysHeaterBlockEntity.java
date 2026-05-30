package com.axollen.create_spinning_doners.block;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SidewaysHeaterBlockEntity extends KineticBlockEntity {
    public static BlockEntityType<SidewaysHeaterBlockEntity> TYPE;

    public static final int TIER_NONE = 0;
    public static final int MINIMUM_RPM = 64;
    public static final int TIER_LOW = 1;
    public static final int TIER_MEDIUM = 2;
    public static final int TIER_HIGH = 3;
    public static final int TIER_MAX = 4;

    public SidewaysHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        int heat = getHeatLevel();
        if (getBlockState().getValue(SidewaysHeaterBlock.HEAT_LEVEL) != heat) {
            level.setBlock(worldPosition, getBlockState().setValue(SidewaysHeaterBlock.HEAT_LEVEL, heat), 3);
        }
    }

    public int getHeatLevel() {
        float speed = Math.abs(getSpeed());
        if (speed < 64) return TIER_NONE;
        if (speed < 128) return TIER_LOW;
        if (speed < 192) return TIER_MEDIUM;
        if (speed < 256) return TIER_HIGH;
        return TIER_MAX;
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
}
