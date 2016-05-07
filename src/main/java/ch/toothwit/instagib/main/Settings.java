package ch.toothwit.instagib.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import ch.toothwit.instagib.main.Instagib; 

public class Settings {
	private static Settings instance;
	private FileConfiguration config;
	private List<Location> spawnLocations = new ArrayList<Location>(); 
	private int gameDuration; 
	
	public static Settings get() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	public Settings() { 
		Instagib.get().saveDefaultConfig(); 
		this.config = Instagib.get().getConfig(); 
		
		reloadConfig(); 
	}

	public void reloadConfig() {
		Instagib.get().reloadConfig(); 
		config = Instagib.get().getConfig(); 

		this.spawnLocations = getLocationList("game.spawnLocations"); 
		this.gameDuration = config.getInt("game.duration"); 
	}

	public void saveConfig() {
		setLocationList("game.spawnLocations", this.spawnLocations);
		config.set("game.duration", this.gameDuration);
		
		File gameConfig = new File(Instagib.get().getDataFolder() + "/" + "config.yml");
		try {
			config.save(gameConfig);
		} catch (IOException e) {
			Bukkit.getLogger().warning("Could not save config");
		}
	} 

	public void setLocationList(String path, List<Location> locations){
		List<String> locs = new ArrayList<String>();
		for(Location loc : locations){
		    locs.add(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
		}
		config.set(path, locs); 
	}
	
	public List<Location> getLocationList(String path){ 
		List<String> locstrings = config.getStringList(path);
		List<Location> locs = new ArrayList<Location>();
		for(String s : locstrings){ 
			String[] split = s.split(" "); 
		    locs.add(new Location(
		    		Bukkit.getWorld(split[0]), 
		    		Double.parseDouble(split[1]), 
		    		Double.parseDouble(split[2]), 
		    		Double.parseDouble(split[3]), 
		    		split.length > 4 ? Float.parseFloat( split[4]) : 0.0f , 
		    		split.length > 5 ? Float.parseFloat( split[5]) : 0.0f            
		    		));
		}
		return locs; 
	} 
	
	public void test(){
		
	}
	
	public List<Location> getSpawnLocations(){
		return this.spawnLocations; 
	}
	
	public void addSpawnLocation(Location location){
		this.spawnLocations.add(location); 
		saveConfig(); 
	}

	public void clearSpawnLocations(){
		this.spawnLocations.clear(); 
		saveConfig(); 
	}
	
	public int getGameDuration() {
		return gameDuration;
	}

	public void setGameDuration(int gameDuration) {
		this.gameDuration = gameDuration;
		saveConfig(); 
	} 
	
	public String getString(String key){ 
		return Settings.get().config.getString("game."+key); 
	}
}
