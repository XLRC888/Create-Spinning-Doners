package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import com.axollen.create_spinning_doners.effect.FoodPoisoningEffect;
import com.axollen.create_spinning_doners.effect.OvereatenEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SpinningDoners.MOD_ID);

    public static final RegistryObject<MobEffect> FOOD_POISONING = EFFECTS.register("food_poisoning",
            FoodPoisoningEffect::new);

    public static final RegistryObject<MobEffect> OVEREATEN = EFFECTS.register("overeaten",
            OvereatenEffect::new);
}
