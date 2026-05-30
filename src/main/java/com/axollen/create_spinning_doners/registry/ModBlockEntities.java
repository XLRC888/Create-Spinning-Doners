package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import com.axollen.create_spinning_doners.block.DonerSpinnerBlockEntity;
import com.axollen.create_spinning_doners.block.SidewaysHeaterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SpinningDoners.MOD_ID);

    public static final RegistryObject<BlockEntityType<DonerSpinnerBlockEntity>> DONER_SPINNER = BLOCK_ENTITIES.register("doner_spinner",
            () -> {
                BlockEntityType<DonerSpinnerBlockEntity> type = BlockEntityType.Builder.of(
                        DonerSpinnerBlockEntity::new, ModBlocks.DONER_SPINNER.get()).build(null);
                DonerSpinnerBlockEntity.TYPE = type;
                return type;
            });

    public static final RegistryObject<BlockEntityType<SidewaysHeaterBlockEntity>> SIDEWAYS_HEATER = BLOCK_ENTITIES.register("sideways_heater",
            () -> {
                BlockEntityType<SidewaysHeaterBlockEntity> type = BlockEntityType.Builder.of(
                        SidewaysHeaterBlockEntity::new, ModBlocks.SIDEWAYS_HEATER.get()).build(null);
                SidewaysHeaterBlockEntity.TYPE = type;
                return type;
            });
}
