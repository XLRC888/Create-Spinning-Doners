package com.axollen.create_spinning_doners.registry;

import com.axollen.create_spinning_doners.SpinningDoners;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpinningDoners.MOD_ID);

    public static final RegistryObject<Item> RAW_FULL_DONER = ITEMS.register("raw_full_doner",
            () -> new Item(new Item.Properties().food(ModFoods.RAW_FULL_DONER)));
    public static final RegistryObject<Item> COOKED_DONER_PIECE = ITEMS.register("cooked_doner_piece",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_DONER_PIECE)));
    public static final RegistryObject<Item> RAW_DONER_PIECE = ITEMS.register("raw_doner_piece",
            () -> new Item(new Item.Properties().food(ModFoods.RAW_DONER_PIECE)));
    public static final RegistryObject<Item> LAVASH = ITEMS.register("lavash",
            () -> new Item(new Item.Properties().food(ModFoods.LAVASH)));
    public static final RegistryObject<Item> COOKED_FULL_DONER = ITEMS.register("cooked_full_doner",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_FULL_DONER)));
    public static final RegistryObject<Item> LAVASH_DONER = ITEMS.register("lavash_doner",
            () -> new Item(new Item.Properties().food(ModFoods.LAVASH_DONER)));
    public static final RegistryObject<Item> BREAD_DONER = ITEMS.register("bread_doner",
            () -> new Item(new Item.Properties().food(ModFoods.BREAD_DONER)));

    public static final RegistryObject<Item> DONER_SPINNER = ITEMS.register("doner_spinner",
            () -> new BlockItem(ModBlocks.DONER_SPINNER.get(), new Item.Properties()));
    public static final RegistryObject<Item> SIDEWAYS_HEATER = ITEMS.register("sideways_heater",
            () -> new BlockItem(ModBlocks.SIDEWAYS_HEATER.get(), new Item.Properties()));

    public static final RegistryObject<Item> COOKING_STATION = ITEMS.register("cooking_station",
            () -> new BlockItem(ModBlocks.COOKING_STATION.get(), new Item.Properties()));
}
