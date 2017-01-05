package me.varmetek.renchant.listener;

import me.varmetek.renchant.Renchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.List;
import java.util.Map;

/**
 * Created by XDMAN500 on 1/4/2017.
 */
public enum EnchantListener implements Listener
{
	INSTANCE;

	ItemFactory itemF;

	EnchantListener()
	{
		itemF = Renchant.getInstance().getServer().getItemFactory();
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

	//	Map<Enchantment,Integer> map = lapis.getEnchantments();



	///	for(Enchantment e : map.keySet()){// Remove all Enchantments Before item is overriden
			//lapis.removeEnchantment(e);
		//}


		doSwap(inv);

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


		Renchant.getInstance().run(() -> ev.getEnchanter().updateInventory());

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

		if(invalid || !lapis.hasItemMeta())return false;
		ItemMeta forLapis = itemF.getItemMeta(lapis.getType());
		ItemMeta forItem = itemF.asMetaFor(lapis.getItemMeta(),item);

		transfer(item,forItem,forLapis,inv.getViewers());

			lapis.setItemMeta(forLapis);

		return true;




	}

	private void transfer(ItemStack item, ItemMeta forItem, ItemMeta forLapis, List<HumanEntity> pl)
	{
		if(item.getItemMeta() instanceof LeatherArmorMeta)
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)forItem;
			meta.setColor(((LeatherArmorMeta) item.getItemMeta()).getColor());
			item.setItemMeta(meta);
		}else{

			if(item.getItemMeta() instanceof EnchantmentStorageMeta)
			{
				Map<Enchantment, Integer> enchants = ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants();
				item.setType(Material.BOOK);
				enchants.forEach( (ench,lvl) -> forLapis.addEnchant(ench,lvl,true) );
				item.setItemMeta(itemF.getItemMeta(item.getType()));
				pl.forEach(p-> ((Player)p).updateInventory());
			}else
			{
				if(item.getItemMeta() instanceof Repairable)
				{

					Repairable meta = (Repairable)forItem;
					meta.setRepairCost( ((Repairable)item.getItemMeta()).getRepairCost());
					item.setItemMeta((ItemMeta)meta);
				}else{
					if(item.getType() == Material.BOOK && forLapis.hasEnchants()){
						item.setType(Material.ENCHANTED_BOOK);
						EnchantmentStorageMeta meta = (EnchantmentStorageMeta)forLapis;
						Map<Enchantment, Integer> enchants = forLapis.getEnchants();
						enchants.forEach((ench,lvl) -> {
							meta.addStoredEnchant(ench,lvl,true);
							meta.removeEnchant(ench);
						});
						item.setItemMeta(meta);
						pl.forEach(p-> ((Player)p).updateInventory());
					}else
					{
						item.setItemMeta(forItem);
					}

				}
			}
		}
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
		if(invalid || !item.hasItemMeta())return false;


		ItemMeta forLapis = item.getItemMeta();
		ItemMeta forItem = itemF.getItemMeta(item.getType());

		transfer(item,forItem,forLapis,inv.getViewers());




		lapis.setItemMeta(itemF.asMetaFor(forLapis,lapis));

		return true;




	}





}
