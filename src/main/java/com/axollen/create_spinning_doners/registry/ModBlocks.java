package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import com.axollen.create_spinning_doners.block.CookingStationBlock;
import com.axollen.create_spinning_doners.block.DonerSpinnerBlock;
import com.axollen.create_spinning_doners.block.SidewaysHeaterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SpinningDoners.MOD_ID);

    public static final RegistryObject<DonerSpinnerBlock> DONER_SPINNER = BLOCKS.register("doner_spinner",
            () -> new DonerSpinnerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0F, 6.0F).noOcclusion()));

    public static final RegistryObject<SidewaysHeaterBlock> SIDEWAYS_HEATER = BLOCKS.register("sideways_heater",
            () -> new SidewaysHeaterBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0F, 6.0F).noOcclusion()));

    public static final RegistryObject<CookingStationBlock> COOKING_STATION = BLOCKS.register("cooking_station",
            () -> new CookingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0F, 6.0F).noOcclusion()));
}
