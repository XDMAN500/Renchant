package me.varmetek.renchant;

import me.varmetek.renchant.listener.EnchantListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Renchant extends JavaPlugin implements Listener
{


	@Override
	public void onEnable(){

		getLogger().info("Renchant Activated");
		getServer().getPluginManager().registerEvents(new EnchantListener(this),this);

	}

	@Override
	public void onDisable(){
	}


}
