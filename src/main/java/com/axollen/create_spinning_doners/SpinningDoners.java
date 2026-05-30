package com.axollen.create_spinning_doners;

import com.axollen.create_spinning_doners.registry.ModBlockEntities;
import com.axollen.create_spinning_doners.registry.ModBlocks;
import com.axollen.create_spinning_doners.registry.ModCreativeTab;
import com.axollen.create_spinning_doners.registry.ModEffects;
import com.axollen.create_spinning_doners.registry.ModItems;
import com.axollen.create_spinning_doners.registry.ModPartialModels;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpinningDoners.MOD_ID)
public class SpinningDoners {
    public static final String MOD_ID = "create_spinning_doners";
    public static final Logger LOGGER = LogManager.getLogger();

    public SpinningDoners() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModEffects.EFFECTS.register(bus);
        ModCreativeTab.TABS.register(bus);
        ModPartialModels.init();
    }
}