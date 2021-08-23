package games.glutenfree;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static void setSpecialItemKey(NamespacedKey key, ItemStack item, String keyName){
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key,
                PersistentDataType.STRING, keyName);
        item.setItemMeta(meta);
    }

    public static String getSpecialItemKey(NamespacedKey key, ItemStack item){
        return item.getItemMeta().getPersistentDataContainer()
                .get(key, PersistentDataType.STRING);
    }

    public static void setItemName(ItemStack item, String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static void addItemLore(ItemStack item, String lore){
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = meta.getLore();
        if(loreList == null) loreList = new ArrayList<>();
        loreList.add(lore);
        meta.setLore(loreList);
        item.setItemMeta(meta);
    }

}
