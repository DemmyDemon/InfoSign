package com.webkonsept.bukkit.infosign;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class InfoSignBlockListener extends BlockListener {
	private InfoSign plugin;

	public InfoSignBlockListener(InfoSign infoSign) {
		plugin = infoSign;
	}

	public void onSignChange(SignChangeEvent event){
		String signTag = event.getLine(0);
		plugin.babble("Sing change!");
		if (signTag.equalsIgnoreCase("[Info]")){
			plugin.babble("OMG it's one of mine!");
			plugin.signFile.add(event.getBlock().getLocation(),(Sign)event.getBlock().getState());
			plugin.signFile.updateSigns();
			plugin.signFile.save();
		}
		else {
			plugin.babble(signTag+"?  Ignored!");
		}
	}
	public void onBlockBreak(BlockBreakEvent event){
		Material blockIs = event.getBlock().getType();
		if (blockIs.equals(Material.SIGN) || blockIs.equals(Material.WALL_SIGN) || blockIs.equals(Material.SIGN_POST)){
			plugin.babble(event.getPlayer().getDisplayName()+" -> Attempt at smasing a sign!");
			Location location = event.getBlock().getLocation();
			if (plugin.signFile.contains(location)){
				plugin.babble("OMG, it's one of mine!");
				if (event.getPlayer().isOp()){
					plugin.babble("It's an OP, so it's cool");
					plugin.signFile.remove(location);
				}
				else {
					plugin.babble("DENIED!");
					event.setCancelled(true);
				}
			}
		}
	}
}
