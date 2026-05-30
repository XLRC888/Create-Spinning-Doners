package com.axollen.create_spinning_doners.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FoodPoisoningEffect extends MobEffect {
    public FoodPoisoningEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A3B2E);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.hurt(entity.damageSources().magic(), 1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 25 >> amplifier;
        if (interval > 0) {
            return duration % interval == 0;
        }
        return true;
    }
}
