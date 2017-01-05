package me.varmetek.renchant;

import me.varmetek.renchant.listener.EnchantListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


public class Renchant extends JavaPlugin implements Listener
{
	private static Renchant plugin = null;
	public Renchant(){
		plugin = this;
	}

	@Override
	public void onEnable(){

		getLogger().info("Renchant Activated");
		getServer().getPluginManager().registerEvents(EnchantListener.INSTANCE,this);

	}

	@Override
	public void onDisable(){
		plugin = null;
	}


	public static Renchant getInstance(){
		return plugin;
	}
	public  BukkitTask run(Runnable run)
	{
		return getServer().getScheduler().runTaskLater(this,run,1L);
	}

}
