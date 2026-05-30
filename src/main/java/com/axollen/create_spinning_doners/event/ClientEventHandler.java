package com.axollen.create_spinning_doners.event;

import com.axollen.create_spinning_doners.block.DonerSpinnerRenderer;
import com.axollen.create_spinning_doners.block.SidewaysHeaterRenderer;
import com.axollen.create_spinning_doners.ponder.SpinningDonersPonderPlugin;
import com.axollen.create_spinning_doners.registry.ModBlockEntities;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new SpinningDonersPonderPlugin());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DONER_SPINNER.get(), DonerSpinnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIDEWAYS_HEATER.get(), SidewaysHeaterRenderer::new);
    }
}
