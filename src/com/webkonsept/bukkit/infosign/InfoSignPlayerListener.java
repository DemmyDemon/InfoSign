package com.webkonsept.bukkit.infosign;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class InfoSignPlayerListener extends PlayerListener {
	private final InfoSign plugin;
	
	InfoSignPlayerListener (InfoSign instance){
		plugin = instance;
	}
	
	@Override
	public void onPlayerQuit (PlayerQuitEvent event){
		plugin.signFile.updateSigns();
	}
	public void onPlayerJoin (PlayerJoinEvent event){
		plugin.signFile.updateSigns();
	}
	public void onPlayerKick (PlayerKickEvent event){
		plugin.signFile.updateSigns();
	}
	public void onPlayerInteract (PlayerInteractEvent event){
		if (event.getClickedBlock() != null){
			if (plugin.signFile.contains(event.getClickedBlock().getLocation())){
				event.getPlayer().sendMessage(ChatColor.GRAY+"Updating InfoSign...");
				plugin.signFile.updateSigns();
			}
		}
	}

}
