package com.axollen.create_spinning_doners.mixin;

import com.axollen.create_spinning_doners.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    private Map<MobEffect, MobEffectInstance> activeEffects;

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void createSpinningDoners$hasEffect(MobEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == MobEffects.CONFUSION && activeEffects.containsKey(ModEffects.FOOD_POISONING.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
    private void createSpinningDoners$getEffect(MobEffect effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (effect == MobEffects.CONFUSION && activeEffects.containsKey(ModEffects.FOOD_POISONING.get())) {
            MobEffectInstance foodPoison = activeEffects.get(ModEffects.FOOD_POISONING.get());
            if (foodPoison != null) {
                cir.setReturnValue(new MobEffectInstance(MobEffects.CONFUSION, foodPoison.getDuration(), foodPoison.getAmplifier(), false, false, false));
            }
        }
    }
}
