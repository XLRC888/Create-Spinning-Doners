package com.axollen.create_spinning_doners.ponder;

import com.axollen.create_spinning_doners.SpinningDoners;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class SpinningDonersPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return SpinningDoners.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        SpinningDonersPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        SpinningDonersPonderTags.register(helper);
    }
}
