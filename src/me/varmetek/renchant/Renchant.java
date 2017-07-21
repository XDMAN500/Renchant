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
		getServer().getPluginManager().registerEvents(new EnchantListener(this),this);

	}

	@Override
	public void onDisable(){
		plugin = null;
	}


	public static Renchant getInstance(){
		return plugin;
	}
	public  static BukkitTask run(Runnable run)
	{
		return plugin.getServer().getScheduler().runTaskLater(plugin,run,1L);
	}

}
