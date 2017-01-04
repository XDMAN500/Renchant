package me.varmetek.renchant;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;


public class Renchant extends JavaPlugin implements Listener
{
	public void onEnbale(){

		getLogger().info("Renchant Activated");
		getServer().getPluginManager().registerEvents(this,this);

	}

	@EventHandler
	public void updateEnchant(InventoryClickEvent ev)
	{
			if(!ev.getWhoClicked().hasPermission("renchant.use"))return;
		if(ev.getClickedInventory() == null)return;
		if(ev.getClickedInventory() .getType() != InventoryType.ENCHANTING)return;
		EnchantingInventory inv = (EnchantingInventory)ev.getClickedInventory() ;
		undoSwap(inv);

	}

	/***
	 *
	 *  Gives the enchantments back to the item when the enchanting Inventory is closed
	 */
	@EventHandler
	public void closeEnchant(InventoryCloseEvent ev)
	{
		if(!ev.getPlayer().hasPermission("renchant.use"))return;
		if(ev.getInventory().getType() != InventoryType.ENCHANTING)return;
		EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
		undoSwap(inv);

	}
	/***
	 *
	 *  Gives the Enchantments to the lapis and combines prospected enchantments with the current enchantments
	 *  if replaceEnchants is false;
	 */
	@EventHandler
	public void commitEnchant(EnchantItemEvent ev)
	{
		if(!ev.getEnchanter().hasPermission("renchant.use"))return;
		EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
		ItemStack lapis = inv.getSecondary();
		boolean invalid = (lapis == null || lapis.getType()  != Material.INK_SACK || lapis.getEnchantments().isEmpty() );
		if(invalid)return;

		Map<Enchantment,Integer> map = lapis.getEnchantments();



			for(Enchantment e : map.keySet()){// Remove all Enchantments Before item is overriden
				lapis.removeEnchantment(e);
			}


		run(() -> doSwap(inv));

	}
	@EventHandler
	public void enchant(PrepareItemEnchantEvent ev){
		if(!ev.getEnchanter().hasPermission("renchant.use"))return;
		EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
		ItemStack item =  inv.getItem();
		ItemStack  lapis =inv.getSecondary();

		boolean invalid = (lapis == null || item == null ||
				                   lapis.getType()  != Material.INK_SACK || item.getType() == Material.AIR );
		if(invalid ||  item.getEnchantments().isEmpty())return;


		doSwap(inv);


		run(() -> ev.getEnchanter().updateInventory());

	}
	/***
	 *
	 *  Transfers the enchants from the lapis to the item;
	 *
	 */

	private  boolean undoSwap(EnchantingInventory inv)
	{
		ItemStack item =  inv.getItem();
		ItemStack  lapis =inv.getSecondary();

		boolean invalid = (lapis == null || item == null ||  lapis.getType()  != Material.INK_SACK || item.getType() == Material.AIR );
		if(invalid || lapis.getEnchantments().isEmpty())return false;

		item.addUnsafeEnchantments(lapis.getEnchantments());


		for(Enchantment e : item.getEnchantments().keySet())// Remove all Enchantments
			lapis.removeEnchantment(e);
		return true;




	}
	/**
	 *
	 *
	 * Transers enchantments from the item to the lapis
	 * @return returns the successfulness of the swap
	 * @param inv The inventory to do the swapping
	 *
	 */

	private  boolean doSwap(EnchantingInventory inv)
	{
		ItemStack item =  inv.getItem();
		ItemStack  lapis =inv.getSecondary();

		boolean invalid = (lapis == null || item == null ||  lapis.getType()  != Material.INK_SACK || item.getType() == Material.AIR );
		if(invalid || item.getEnchantments().isEmpty())return false;


			lapis.addUnsafeEnchantments(item.getEnchantments());




		for(Enchantment e : lapis.getEnchantments().keySet())// Remove all Enchantments
			item.removeEnchantment(e);
		return true;




	}

	private BukkitTask run(Runnable run){
		return getServer().getScheduler().runTaskLater(this,run,1L);
	}



}
