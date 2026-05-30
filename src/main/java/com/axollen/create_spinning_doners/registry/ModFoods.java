package com.axollen.create_spinning_doners.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModFoods {
    public static final FoodProperties COOKED_DONER_PIECE = new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.6f).meat().build();
    public static final FoodProperties RAW_DONER_PIECE = new FoodProperties.Builder()
            .nutrition(1).saturationMod(0.2f).meat()
            .effect(() -> new MobEffectInstance(ModEffects.FOOD_POISONING.get(), 600, 0), 1.0f)
            .build();
    public static final FoodProperties RAW_FULL_DONER = new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.3f).meat()
            .effect(() -> new MobEffectInstance(ModEffects.FOOD_POISONING.get(), 900, 0), 1.0f)
            .effect(() -> new MobEffectInstance(ModEffects.OVEREATEN.get(), 1200, 0), 1.0f)
            .build();
    public static final FoodProperties COOKED_FULL_DONER = new FoodProperties.Builder()
            .nutrition(12).saturationMod(0.8f).meat().build();
    public static final FoodProperties LAVASH = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.3f).build();
    public static final FoodProperties LAVASH_DONER = new FoodProperties.Builder()
            .nutrition(8).saturationMod(0.8f).meat()
            .effect(nourishment(3600, 0), 1.0f)
            .build();
    public static final FoodProperties BREAD_DONER = new FoodProperties.Builder()
            .nutrition(7).saturationMod(0.75f).meat()
            .effect(nourishment(3600, 0), 1.0f)
            .build();

    private static Supplier<MobEffectInstance> nourishment(int duration, int amplifier) {
        return () -> {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                    new ResourceLocation("farmersdelight", "nourishment"));
            if (effect != null) {
                return new MobEffectInstance(effect, duration, amplifier);
            }
            return null;
        };
    }
}
