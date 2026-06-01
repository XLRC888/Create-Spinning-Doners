package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class ModPartialModels {
    public static final PartialModel SPINNING_SHAFT = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_shaft"));
    public static final PartialModel SPINNING_RAW_THICK = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_raw_thick"));
    public static final PartialModel SPINNING_RAW_MEDIUM = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_raw_medium"));
    public static final PartialModel SPINNING_COOKED_THICK = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_cooked_thick"));
    public static final PartialModel SPINNING_COOKED_MEDIUM = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_cooked_medium"));
    public static final PartialModel SPINNING_RAW_THIN = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_raw_thin"));
    public static final PartialModel SPINNING_COOKED_THIN = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/doner_spinner/spinning_cooked_thin"));
    public static final PartialModel HEATER_SHAFT = PartialModel.of(
            new ResourceLocation(SpinningDoners.MOD_ID, "block/heater/heater_shaft"));
    public static void init() {}
}
