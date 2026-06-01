package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpinningDoners.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_spinning_doners"))
                    .icon(() -> ModItems.RAW_FULL_DONER.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DONER_SPINNER.get());
                        output.accept(ModItems.SIDEWAYS_HEATER.get());
                        output.accept(ModItems.COOKING_STATION.get());
                        output.accept(ModItems.RAW_FULL_DONER.get());
                        output.accept(ModItems.COOKED_FULL_DONER.get());
                        output.accept(ModItems.RAW_DONER_PIECE.get());
                        output.accept(ModItems.COOKED_DONER_PIECE.get());
                        output.accept(ModItems.LAVASH.get());
                        output.accept(ModItems.LAVASH_DONER.get());
                        output.accept(ModItems.BREAD_DONER.get());
                    })
                    .build());
}
