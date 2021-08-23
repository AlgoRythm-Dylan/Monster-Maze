package games.glutenfree.maze;

import games.glutenfree.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MazePowerup {

    public static ItemStack repulse(NamespacedKey specialKey){
        ItemStack stack = new ItemStack(Material.COAL);
        Util.setItemName(stack, String.format("%s%sRepulse %s%s - 10 tokens", ChatColor.LIGHT_PURPLE, ChatColor.BOLD,
                ChatColor.RESET, ChatColor.GRAY));
        Util.addItemLore(stack, "10 tokens");
        Util.setSpecialItemKey(specialKey, stack, "repulse");
        return stack;
    }

    public static ItemStack oneUp(NamespacedKey specialKey){
        ItemStack stack = new ItemStack(Material.WARPED_FUNGUS);
        Util.setItemName(stack, String.format("%s%s1UP %s(%s+3 hearts%s) %s- 5 tokens",
                ChatColor.GREEN, ChatColor.BOLD, ChatColor.RESET, ChatColor.RED, ChatColor.RESET, ChatColor.GRAY));
        Util.addItemLore(stack, "5 tokens");
        Util.setSpecialItemKey(specialKey, stack, "one-up");
        return stack;
    }

    public static ItemStack ionCannon(NamespacedKey specialKey){
        ItemStack stack = new ItemStack(Material.NETHER_STAR);
        Util.setItemName(stack, String.format("%s%sIon Cannon", ChatColor.DARK_PURPLE, ChatColor.BOLD));
        Util.addItemLore(stack, "25 tokens");
        Util.setSpecialItemKey(specialKey, stack, "ion-cannon");
        return stack;
    }

    public static ItemStack speedBoost(NamespacedKey specialKey){
        ItemStack stack = new ItemStack(Material.FEATHER);
        Util.setItemName(stack, String.format("%s%sSpeed Boost%s%s - 8 tokens", ChatColor.YELLOW, ChatColor.BOLD,
                ChatColor.RESET, ChatColor.GRAY));
        Util.addItemLore(stack, "8 tokens");
        Util.setSpecialItemKey(specialKey, stack, "speed-boost");
        return stack;
    }

    public static ItemStack tokens(NamespacedKey specialKey, int amount){
        ItemStack stack = new ItemStack(amount == 0 ? Material.IRON_NUGGET : Material.GOLD_NUGGET);
        stack.setAmount(amount == 0 ? 1 : amount);
        Util.setItemName(stack, String.format("%s%sTokens",ChatColor.GOLD, ChatColor.BOLD));
        Util.addItemLore(stack, "Used to buy powerups");
        Util.setSpecialItemKey(specialKey, stack, "tokens");
        return stack;
    }

}
