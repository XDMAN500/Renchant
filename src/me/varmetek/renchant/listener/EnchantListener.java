package me.varmetek.renchant.listener;

import me.varmetek.renchant.Renchant;
import org.bukkit.Bukkit;
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

import java.util.List;
import java.util.Map;

/**
 * Created by XDMAN500 on 1/4/2017.
 */
public class EnchantListener implements Listener
{

	private static final String permission ="renchant.use";
	private ItemFactory itemF;
	private Renchant plugin;

	public EnchantListener(Renchant plugin)
	{
		this.plugin = plugin;
		itemF = Bukkit.getItemFactory();
	}

	@EventHandler
	public void updateEnchant(InventoryClickEvent ev)
	{
		Player player = (Player)ev.getWhoClicked();
		if(!player.hasPermission( permission))return;
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
		if(!ev.getPlayer().hasPermission( permission))return;
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

		if(!ev.getEnchanter().hasPermission( permission))return;
		EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
		ItemStack lapis = inv.getSecondary();
		boolean invalid = (lapis == null || lapis.getType()  != Material.INK_SACK || hasItemMeta(lapis));
		if(invalid)return;


		doSwap(inv);

	}


	@EventHandler
	public void enchant(PrepareItemEnchantEvent ev){


		if(!ev.getEnchanter().hasPermission(permission))return;
		EnchantingInventory inv = (EnchantingInventory)ev.getInventory();
		ItemStack item =  inv.getItem();
		ItemStack  lapis =inv.getSecondary();


		boolean invalid = (lapis == null || item == null ||
				                   lapis.getType()  != Material.INK_SACK || item.getType() == Material.AIR );
		if(invalid ||  !hasItemMeta(item))return;


		doSwap(inv);


		Renchant.run(() -> ev.getEnchanter().updateInventory());

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

		if(invalid || !hasItemMeta(lapis))return false;
		ItemMeta forLapis = itemF.getItemMeta(lapis.getType());
		ItemMeta forItem = itemF.asMetaFor(lapis.getItemMeta(),item);

		transfer(item,forItem,forLapis,inv.getViewers());

			lapis.setItemMeta(forLapis);

		return true;




	}
	/**
	 * Transfers non-specialized Item meta
	 * */
	private void transfer(ItemStack item, ItemMeta forItem, ItemMeta forLapis, List<HumanEntity> viewers)
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
				item.setItemMeta(itemF.getItemMeta(item.getType()));
				enchants.forEach( (ench,lvl) -> forLapis.addEnchant(ench,lvl,true) );


				viewers.forEach(p-> ((Player)p).updateInventory());
			}else
			{

				if(item.getType() == Material.BOOK ){

					item.setType(Material.ENCHANTED_BOOK);
					EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemF.asMetaFor(forItem,Material.ENCHANTED_BOOK);
					Map<Enchantment, Integer> enchants = forLapis.getEnchants();
					enchants.forEach((ench,lvl) -> meta.addStoredEnchant(ench,lvl,false));
					item.setItemMeta(meta);

				}else{
					item.setItemMeta(forItem);


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
		if(invalid || !hasItemMeta(item))return false;


		ItemMeta forLapis = item.getItemMeta();
		ItemMeta forItem = itemF.getItemMeta(item.getType());

		transfer(item,forItem,forLapis,inv.getViewers());




		lapis.setItemMeta(itemF.asMetaFor(forLapis,lapis));

		return true;




	}
	/*
	*  A method that checks if the item has any non-specialized item-meta
	*   if item meta is present then more checks are done on the meta.
	*   if the meta is specialized under enchantment storage then the method will return true
	*   if not then the item meta will be converted into non-specialized item meta to be compared to the default
	*   non specialized item meta generated by an Item factory. If the converted meta is similar to the default meta
	 *   then the method return false for not having meta;
	*
	*
	* */
	private boolean hasItemMeta(ItemStack item){
		if(item.hasItemMeta()){

			return (item.getItemMeta() instanceof EnchantmentStorageMeta) ||
					       !itemF.asMetaFor(item.getItemMeta(),Material.STONE).equals(itemF.getItemMeta(Material.STONE));
		}else{
			return false;
		}
	}





}
