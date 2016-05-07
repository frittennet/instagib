package ch.toothwit.instagib.main;
 
import java.util.List; 
 
import org.bukkit.Location; 
import org.bukkit.entity.Player; 

public class GamePlayer { 
	public Player player; 
	public int kills = 0; 
	public int deaths = 0; 
	
	public GamePlayer(Player player){
		this.player = player; 
	} 
	
	public void respawn(){
		List<Location> respawns = Settings.get().getSpawnLocations(); 
		
		int random; 
		do {
			random = (int)(Math.random()*respawns.size()); 
		}
		while(respawns.get(random).distance(this.player.getLocation()) < 2.5f); 
		
		this.player.teleport(respawns.get(random)); 
	}

	public boolean getAllowShooting() {
		return Game.get().getCooldown(this.player); 
	} 
} 
