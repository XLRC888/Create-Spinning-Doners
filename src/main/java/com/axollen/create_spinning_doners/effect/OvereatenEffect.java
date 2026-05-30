package com.axollen.create_spinning_doners.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class OvereatenEffect extends MobEffect {
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160891");

    public OvereatenEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(),
                -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
