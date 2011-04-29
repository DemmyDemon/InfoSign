package com.webkonsept.bukkit.infosign;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class InfoSignFile implements Runnable {
	protected String fileName;
	private Logger log = Logger.getLogger("Minecraft");
	private File fileHandle;
	private HashMap<Location,Sign> signs;
	private InfoSign plugin;
	private Long updated = System.currentTimeMillis();
	
	InfoSignFile (InfoSign instance, String filename) throws IOException,FileNotFoundException {
		fileName = filename;
		fileHandle = new File(fileName);
		plugin = instance;
		signs = load();
		
	}
	
	private HashMap<Location,Sign> load() throws IOException {
		HashMap<Location,Sign> signList =  new HashMap<Location,Sign>();
		if (!fileHandle.exists()){
			if (fileHandle.canWrite()){
				fileHandle.createNewFile();
			}
			else {
				log.severe("Could not write new settings file "+fileName);
			}
		}
		else {
			BufferedReader reader = new BufferedReader(new FileReader(fileHandle));
			while (true){
				String line = reader.readLine();
				if (line == null || line.isEmpty()){
					break;
				}
				else {
					plugin.babble("Read: "+line);
					String[] coords = line.split(",",4);
					World world = plugin.getServer().getWorld(coords[0]);
					Location location = null;
					Double X;
					Double Y;
					Double Z;
					if (world == null){
						plugin.babble(coords[0]+" failed to resolve to a valid World!");
					}
					else {
						X = Double.parseDouble(coords[1]);
						Y = Double.parseDouble(coords[2]);
						Z = Double.parseDouble(coords[3]);
						location = new Location(world,X,Y,Z);
					}
					if (location != null){
						Block block = location.getBlock();
						if (block == null){
							plugin.babble("Urr, getBlock() on that loation gave me a Null blck!");
						}
						else if (block.getType().equals(Material.SIGN) || block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST)){
							Sign sign = (Sign) block.getState();
							signList.put(location, sign);
						}
						else {
							plugin.babble("Urr, getBlock() on that location gives me type "+block.getType().toString());
						}
					}
					else {
						plugin.babble("Converting the loaded line into a valid location failed!");
					}
				}
			}
			reader.close();
		}
		return signList;
	}
	public void add(Location location, Sign sign){
		plugin.babble("Adding a sign?!");
		if (!signs.containsKey(location)){
			plugin.babble("It's a fresh one!");
			signs.put(location, sign);
			save();
		}
	}
	public void remove(Location location){
		plugin.babble("Removing a sign?!");
		if (signs.containsKey(location)){
			signs.remove(location);
			plugin.babble("Done!");
		}
		else {
			plugin.babble("Urrr, none of my signs are there.  Ignoring.");
		}
	}
	public void save() {
		plugin.babble("Attempting to save!");
		BufferedWriter writer = null;
		if (!fileHandle.exists()){
			if (fileHandle.canWrite()){
				try {
					fileHandle.createNewFile();
				}catch (IOException e){
					log.severe("Failed to create new file "+fileName+": "+e.getLocalizedMessage());
					return;
				}
			}
		}
		try {
			writer = new BufferedWriter(new FileWriter(fileHandle));
		} catch (IOException e){
			log.severe("Failed to create BufferedWriter for "+fileName+": "+e.getLocalizedMessage());
			return;
		}
		Iterator<Location> keys = signs.keySet().iterator();
		while (keys.hasNext()){
			Location location = (Location) keys.next();
			String X = ((Double)location.getX()).toString();
			String Y = ((Double)location.getY()).toString();
			String Z = ((Double)location.getZ()).toString();
			String world = location.getWorld().getName();
			String line = world+","+X+","+Y+","+Z;
			try {
				writer.write(line);
				plugin.babble(line);
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				log.severe("Exception while trying to save to "+fileName+": "+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			log.severe("OH COME ON JAVA!  CAN'T YOU EVEN CLOSE A FILE?!?!?!");
			e.printStackTrace();
		}
		plugin.babble("Let's hope that worked!");
	}
	public Iterator<Sign> iterateSigns() {
		Iterator<Sign> signsIterator = signs.values().iterator();
		return signsIterator;
	}
	public Iterator<Location> iterateLocations() {
		Iterator<Location> locationIterator = signs.keySet().iterator();
		return locationIterator;
	}
	public void updateSigns() {
		Long now = System.currentTimeMillis();
		if (now < updated + 20000){ // 20 seconds since last update?
			plugin.babble("Less than 20 seconds since last update.  REFUSING.");
			return;
		}
		updated = now;
		plugin.babble("Sign update time!");
		Iterator<Location> signsIterator = signs.keySet().iterator();
		String longVersion = plugin.getServer().getVersion();
		String shortVersion = longVersion.split("-")[5].split(" ")[0];
		
		double memUsed = ( Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory() ) / 1048576;
		double memMax = Runtime.getRuntime().maxMemory() / 1048576;
		double percentageUsed = ( 100 / memMax) * memUsed;
		
		String maxPlayers = ((Integer)plugin.getServer().getMaxPlayers()).toString();
		Integer playersOn = plugin.getServer().getOnlinePlayers().length;
		
		if (playersOn > 0){
			String[] lines = new String[4];
			lines[0] = plugin.getServer().getName();
			lines[1] = shortVersion;
			lines[2] = playersOn+"/"+maxPlayers+" slots used";
			lines[3] = (int)percentageUsed+"% RAM used";
	
			while (signsIterator.hasNext()){
				Location signAt = signsIterator.next();
				Material blockIs = signAt.getBlock().getType();
				if (blockIs.equals(Material.SIGN)|| blockIs.equals(Material.WALL_SIGN) || blockIs.equals(Material.SIGN_POST)){
					Sign sign = (Sign)signAt.getBlock().getState();
					sign.setLine(0, lines[0]);
					sign.setLine(1, lines[1]);
					sign.setLine(2, lines[2]);
					sign.setLine(3, lines[3]);
					plugin.babble("Plop: "+sign.getLine(0)+"/"+sign.getLine(1)+"/"+sign.getLine(2)+"/"+sign.getLine(3));
						sign.update(true);
				}
				else {
					plugin.babble("Wat?!  Not a sign!  This block type is "+blockIs.toString()+"!");
				}
			}
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin, 100);
			plugin.babble("They should all be up to date now.");
		}
		else {
			plugin.babble("No users on, so I'm skipping sign updates.  I mean:  Who would read them?!");
		}
	}

	public boolean contains(Location location) {
		plugin.babble("Is this one of mine?");
		if (signs.containsKey(location)){
			plugin.babble("Yep");
			return true;
		}
		else {
			plugin.babble("Nope");
			return false;
		}
	}

	@Override
	public void run() {
		updateSigns();
		
	}

	public void release() {
		save();
		fileName = null;
		log = null;
		fileHandle = null;
		signs = null;
		plugin = null;
	}

}