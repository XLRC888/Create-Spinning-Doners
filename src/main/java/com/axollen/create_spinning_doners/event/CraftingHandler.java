package com.axollen.create_spinning_doners.event;

import com.axollen.create_spinning_doners.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CraftingHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack mainHand = event.getItemStack();
        ItemStack offHand = player.getOffhandItem();

        if (tryCombine(player, mainHand, offHand, ModItems.LAVASH.get(), ModItems.COOKED_DONER_PIECE.get(), ModItems.LAVASH_DONER.get()))
            return;
        if (tryCombine(player, mainHand, offHand, ModItems.LAVASH.get(), ModItems.RAW_DONER_PIECE.get(), ModItems.LAVASH_DONER.get()))
            return;
        tryCombine(player, mainHand, offHand, ModItems.BREAD_DONER.get(), ModItems.COOKED_DONER_PIECE.get(), ModItems.BREAD_DONER.get());
    }

    private static boolean tryCombine(Player player, ItemStack mainHand, ItemStack offHand, Item wrapper, Item filling, Item result) {
        boolean mainIsWrapper = mainHand.is(wrapper);
        boolean offIsWrapper = offHand.is(wrapper);
        boolean mainIsFilling = mainHand.is(filling);
        boolean offIsFilling = offHand.is(filling);

        if (!(mainIsWrapper && offIsFilling) && !(mainIsFilling && offIsWrapper))
            return false;

        if (player.level().isClientSide) return true;

        mainHand.shrink(1);
        offHand.shrink(1);

        player.getInventory().add(new ItemStack(result));
        return true;
    }
}
