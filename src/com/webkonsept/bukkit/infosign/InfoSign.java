package com.webkonsept.bukkit.infosign;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class InfoSign extends JavaPlugin implements Runnable {
	private final InfoSignBlockListener blockListener = new InfoSignBlockListener(this);
	private final InfoSignPlayerListener playerListener = new InfoSignPlayerListener(this);
	public Logger logger = Logger.getLogger("Minecraft");
	public InfoSignFile signFile;
	private Boolean verbose = false;
	

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		signFile.release();
		signFile = null;
	}

	@Override
	public void onEnable() {
		logger.info("InfoSign enabled");
		try {
			signFile = new InfoSignFile(this,"plugins/InfoSign/signs.txt");
		} catch (FileNotFoundException e) {
			logger.severe("[InfoSign] Failed to read the sign file: "+e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.severe("[InfoSign] Failed to load the sign file: "+e.getLocalizedMessage());
			e.printStackTrace();
		}
		signFile.updateSigns();
		PluginManager pm =getServer().getPluginManager();
		pm.registerEvent(Event.Type.SIGN_CHANGE,blockListener,Priority.Normal,this);
		pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN,playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT,playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK,playerListener, Priority.Normal, this);
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, signFile, 0, 6000);
	}
	public void babble (String message){
		if (verbose){
			logger.info("[InfoSign] "+message);
		}
	}

	@Override
	public void run() {
		if (getServer().getOnlinePlayers().length > 0){
			Iterator<Sign> keys = signFile.iterateSigns();
			while (keys.hasNext()){
				Sign sign = keys.next();
				sign.update();
				babble("Scheduled sign update");
			}
		}
		else {
			babble("Skipping scheduled sign update:  No users connected");
		}
	}
}
