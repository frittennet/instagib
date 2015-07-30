package ch.toothwit.instagib.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ch.toothwit.lobby.main.LobbyAPI;
import ch.toothwit.lobby.main.LobbyEventHandler;
import net.md_5.bungee.api.ChatColor;

public class Game implements LobbyEventHandler { 
	private static Game instance; 
	private HashMap<String, GamePlayer> gamePlayers = new HashMap<String, GamePlayer>(); 
	private HashMap<String, Integer> cooldowns = new HashMap<String, Integer>(); 
	private GameState gameState; 
	 
	public Game(){ 
		LobbyAPI.subscribe(this); 
		this.reload(); 
		new BukkitRunnable() { 
			public void run() { 
				for(Iterator<Map.Entry<String, Integer>> it = cooldowns.entrySet().iterator(); it.hasNext(); ) { 
					Map.Entry<String, Integer> entry = it.next(); 
					if(entry.getValue().intValue() <= 0) { 
						it.remove(); 
				    } 
					else{
						entry.setValue(entry.getValue()-1); 
					}
				} 
			} 
		}.runTaskTimer(Instagib.get(), 0L, 2L); 
	} 
	
	public static Game get(){
		if(instance == null){
			instance = new Game(); 
		} 
		return instance; 
	} 
	
	private void reload(){
		LobbyAPI.reload(); 
		Bukkit.getScheduler().cancelTask(countdownTask); 
		
		this.gameState = GameState.LOBBY; 
		this.gamePlayers = new HashMap<String, GamePlayer>(); 
	} 
	
	public void setCooldown(Player player){
		cooldowns.put(player.getUniqueId().toString(), 16); 
	}
	
	public boolean getCooldown(Player player){
		return cooldowns.get(player.getUniqueId().toString()) != null; 
	}
	
	int countdownTask; 
	
	@SuppressWarnings("deprecation") 
	public void StartGame(List<Player> players) { 
		ItemStack weapon = new ItemStack(Material.STICK); 
		ItemMeta meta = weapon.getItemMeta(); 
		meta.setDisplayName("Rifle"); 
		weapon.setItemMeta(meta); 
		for(Player player : players){ 
			Inventory inventory = player.getInventory(); 
			this.gamePlayers.put(player.getUniqueId().toString(), new GamePlayer(player)); 
			inventory.clear(); 
			inventory.addItem(weapon); 
		} 
		this.gameState = GameState.RUNNING; 
		
		int n=0; 
		List<Location> locations = Settings.get().getSpawnLocations(); 
		Bukkit.broadcastMessage(ChatColor.GOLD+"Spiel gestartet!"); 
		for(Player player : Bukkit.getOnlinePlayers()){ 
			player.teleport(locations.get(n%locations.size())); 
			n++; 
		} 
		
		countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Instagib.get(), new BukkitRunnable() { 
			private int timeLeft = Settings.get().getGameDuration(); 
			public void run() {
				timeLeft--; 
				if(timeLeft > 60 && timeLeft % 60 == 0){
					Bukkit.broadcastMessage(ChatColor.GOLD+"Noch "+ChatColor.RED+""+timeLeft/60+ChatColor.GOLD+" Minuten."); 
				}
				if(timeLeft == 60){
					Bukkit.broadcastMessage(ChatColor.GOLD+"Noch "+ChatColor.RED+"1"+ChatColor.GOLD+" Minute."); 
				}
				if(timeLeft == 30){
					Bukkit.broadcastMessage(ChatColor.GOLD+"Noch "+ChatColor.RED+"30"+ChatColor.GOLD+" Sekunden.");  
				}
				if(timeLeft <= 5 && timeLeft > 0){
					Bukkit.broadcastMessage(ChatColor.GOLD+"Noch "+ChatColor.RED+timeLeft+ChatColor.GOLD+" Sekunden.");  
				}
				if(timeLeft <= 0){ 
					Game.get().endGame(); 
				}
			} 
		}, 0L, 20L); 
	} 
	
	public GamePlayer getGamePlayer(Player player){ 
		return gamePlayers.get(player.getUniqueId().toString());  
	} 
	
	public void onPlayerKilled(Player shooter, Player victim){
		getGamePlayer(victim).deaths++; 
		getGamePlayer(victim).respawn(); 
		getGamePlayer(shooter).kills++; 
		Bukkit.broadcastMessage(ChatColor.RED+victim.getName()+ChatColor.GREEN+" wurde von "+ChatColor.RED+shooter.getName()+ChatColor.GREEN+" get\u00F6tet."); 
	}
	
	@SuppressWarnings({ "deprecation" })
	public void endGame(){ 
		Bukkit.getScheduler().cancelTask(countdownTask); 
		gameState = GameState.FINISHED; 
		
		List<GamePlayer> ranked = new ArrayList<GamePlayer>(); 
		
		for(Entry<String, GamePlayer> entry : this.gamePlayers.entrySet()) {
			ranked.add(entry.getValue());  
		}
		
		Collections.sort(ranked, new Comparator<GamePlayer>(){
		     public int compare(GamePlayer o1, GamePlayer o2){
		         if(o1.kills == o2.kills)
		             return 0;
		         return o1.kills < o2.kills ? -1 : 1;
		     }
		});
		
		int n=ranked.size(); 
		Bukkit.broadcastMessage(ChatColor.GOLD+"============="+ChatColor.RED+"Platzierung"+ChatColor.GOLD+"============"); 
		for(GamePlayer gamePlayer : ranked){ 
			Bukkit.broadcastMessage(ChatColor.RED+"        "+n+""+ChatColor.GOLD+". "+gamePlayer.player.getName()+" ["+gamePlayer.kills+"/"+gamePlayer.deaths+"]"); 
			n--; 
		} 
		Bukkit.broadcastMessage(ChatColor.GOLD+"===================================="); 
		
		Bukkit.broadcastMessage(ChatColor.GOLD+"R\u00FCckkehr zur Lobby in "+ChatColor.RED+"5"+ChatColor.GOLD+" Sekunden"); 
		Bukkit.getScheduler().runTaskLater(Instagib.get(), new BukkitRunnable() { 
			public void run() {                
				for(Player p : Bukkit.getOnlinePlayers()){ 
					p.getInventory().clear(); 
					p.setResourcePack("http://collab.toothwit.ch/emptyPack.zip"); 
					Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), p); 
				} 
				Game.get().reload(); 
			} 
		}, 5*20L); 
	}
	
	public void setGameState(GameState gameState) { 
		this.gameState = gameState; 
	} 

	public GameState getGameState() { 
		return this.gameState;
	} 
} 
