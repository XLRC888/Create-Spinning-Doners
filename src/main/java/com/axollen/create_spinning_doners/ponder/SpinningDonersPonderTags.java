package com.axollen.create_spinning_doners.ponder;

import com.axollen.create_spinning_doners.SpinningDoners;
import com.axollen.create_spinning_doners.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class SpinningDonersPonderTags {
    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.addTagToComponent(ModBlocks.DONER_SPINNER.getId(),
                ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
        helper.addTagToComponent(ModBlocks.SIDEWAYS_HEATER.getId(),
                ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
        helper.addTagToComponent(ModBlocks.COOKING_STATION.getId(),
                ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
    }
}
