package me.varmetek.renchant.listener;

import me.varmetek.renchant.Renchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class EnchantListener implements Listener
{


  private static final String permission ="renchant.use";
  
  private Renchant plugin;

  public EnchantListener (Renchant plugin)
  {
    this.plugin = plugin;
  }


  /***
   *
   *  This event removes old enchantments on the item to make room for the new enchantments
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void commitEnchant(EnchantItemEvent ev){

    if (!ev.getEnchanter().hasPermission(permission)) return;
    EnchantingInventory inv = (EnchantingInventory) ev.getInventory();
    Player player = ev.getEnchanter();

    ItemStack item = ev.getItem();
    //Clear past enchantments
    Map<Enchantment,Integer> enchantments = ev.getItem().getEnchantments();
    for (Map.Entry<Enchantment,Integer> ench : enchantments.entrySet()) {
      inv.getItem().removeEnchantment(ench.getKey());
    }
  }

  /**
   *
   * This event allows for the items to accept enchantment options
   * */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void preEnchantEvent(PrepareItemEnchantEvent ev){

    Player player = ev.getEnchanter();
    if (!player.hasPermission(permission)) return;
    ev.setCancelled(false); //Allows the enchantment options to render regardless if the item is already enchanted

    extractEnchantments(ev.getItem(),player);

  }


  /**
   *
   * This event puts all enchantments back on the item
   * */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void clickEvent(InventoryClickEvent ev){
    Player player = (Player) ev.getWhoClicked();
    if (!player.hasPermission(permission)) return;
    if (ev.getClickedInventory() == null) return;
    if (ev.getClickedInventory().getType() != InventoryType.ENCHANTING) return;
    depositEnchantments( ev.getCurrentItem(),player);

  }

  /**
   *
   * This event puts all enchantments back on the item
   * */

  @EventHandler(priority = EventPriority.HIGHEST)
  public void closeEnchant(InventoryCloseEvent ev)
  {
    if(!ev.getPlayer().hasPermission( permission))return;
    if(ev.getInventory().getType() != InventoryType.ENCHANTING)return;

    EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
    Player player = (Player)ev.getPlayer();

   depositEnchantments(inv.getItem(),player);
  }


  /***
   * Converts a regular book to an enchantbook if the book has enchantments
   */

  private void depositEnchantments(ItemStack item,Player  player){
    if (isItemEmpty(item)) return;
    if(!item.hasItemMeta())return;
    if(item.getType() == Material.BOOK){
      //Pull cached enchantments
      Map<Enchantment,Integer> enchantments =item.getEnchantments();
      if (enchantments == null) return;
      EnchantmentStorageMeta meta  = (EnchantmentStorageMeta) Bukkit.getItemFactory().asMetaFor(item.getItemMeta(),Material.ENCHANTED_BOOK);
      item.setType(Material.ENCHANTED_BOOK);
      for (Map.Entry<Enchantment,Integer> ench : enchantments.entrySet()) {
        meta.addStoredEnchant(ench.getKey(), ench.getValue(),true);
        meta.removeEnchant(ench.getKey());
      }
      item.setItemMeta(meta);

    }
  }

  /***
   * Converts an enchanted book to a book with enchantments
   */
  private void extractEnchantments(ItemStack item,Player  player){
    if (isItemEmpty(item)) return;
    if(!item.hasItemMeta())return;
    if(item.getType() == Material.ENCHANTED_BOOK){
      //Cache stored enchantments and set the item to a book
      EnchantmentStorageMeta meta = ((EnchantmentStorageMeta)item.getItemMeta());
      Map<Enchantment,Integer> enchantments = meta.getStoredEnchants();
      if (enchantments == null || enchantments.isEmpty()) return;
      for (Map.Entry<Enchantment,Integer> ench : enchantments.entrySet()) {
        meta.removeStoredEnchant(ench.getKey() );
        meta.addEnchant(ench.getKey(),ench.getValue(),true);
      }
      item.setItemMeta(meta);
      item.setType(Material.BOOK);


    }
  }


  /***
   *
   * Checks if {@param stack} is null or air
   */

  private  static boolean isItemEmpty(ItemStack stack){
    return stack == null ||stack.getType() == Material.AIR;
  }



}
