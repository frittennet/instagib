package ch.toothwit.instagib.main;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective; 
import org.bukkit.scoreboard.Score; 
import org.bukkit.scoreboard.Scoreboard;

import ch.toothwit.lobby.main.LobbyAPI;
import ch.toothwit.lobby.main.LobbyEventHandler; 

import net.md_5.bungee.api.ChatColor;

public class Game implements LobbyEventHandler {
	private static Game instance;
	private HashMap<String, GamePlayer> gamePlayers = new HashMap<String, GamePlayer>();
	private HashMap<String, Integer> cooldowns = new HashMap<String, Integer>();
	private GameState gameState;
	private Scoreboard scoreboard; 
	private Objective objective; 
	
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public Game() {
		LobbyAPI.subscribe(this);
		this.reload();
		new BukkitRunnable() {
			public void run() {
				for (Iterator<Map.Entry<String, Integer>> it = cooldowns.entrySet().iterator(); it.hasNext();) {
					Map.Entry<String, Integer> entry = it.next();
					if (entry.getValue().intValue() <= 0) {
						it.remove();
					} else {
						entry.setValue(entry.getValue() - 1);
					}
				}
			}
		}.runTaskTimer(Instagib.get(), 0L, 2L);
	}

	public static Game get() {
		if (instance == null) {
			instance = new Game();
		}
		return instance;
	}

	@SuppressWarnings("deprecation")
	private void reload() {
		LobbyAPI.reload();
		Bukkit.getScheduler().cancelTask(countdownTask);

		this.gameState = GameState.LOBBY;
		this.gamePlayers = new HashMap<String, GamePlayer>();
		this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard(); 
		for(OfflinePlayer player : scoreboard.getPlayers())
		{
			scoreboard.resetScores(player);
		}
		this.scoreboard.clearSlot(DisplaySlot.SIDEBAR); 
	}

	public void setCooldown(Player player) {
		cooldowns.put(player.getUniqueId().toString(), 16);
	}

	public boolean getCooldown(Player player) {
		return cooldowns.get(player.getUniqueId().toString()) != null;
	}

	int countdownTask;

	@SuppressWarnings("deprecation")
	public void StartGame(List<Player> players) {
		ItemStack weapon = new ItemStack(Material.STICK);
		ItemMeta meta = weapon.getItemMeta();
		meta.setDisplayName("Rifle");
		weapon.setItemMeta(meta); 
		
		this.objective = this.scoreboard.getObjective("test"); 
		if(this.objective == null){
			this.objective = this.scoreboard.registerNewObjective("test", "dummy"); 
		}
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR); 
		this.objective.setDisplayName("Kills"); 
		
		for (Player player : players) {
			Inventory inventory = player.getInventory();
			this.gamePlayers.put(player.getUniqueId().toString(), new GamePlayer(player)); 
			
			Score score = this.objective.getScore(player.getDisplayName()); 
			score.setScore(0); 
			
			inventory.clear();
			inventory.addItem(weapon);
		}
		this.gameState = GameState.RUNNING;

		int n = 0; 
		List<Location> locations = Settings.get().getSpawnLocations();
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("gameStarted")));
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(locations.get(n % locations.size()));
			n++;
		}

		countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Instagib.get(), new BukkitRunnable() {
			private int timeLeft = Settings.get().getGameDuration();

			public void run() {
				timeLeft--;
				if (timeLeft > 60 && timeLeft % 60 == 0) {
					Bukkit.broadcastMessage(MessageFormat.format(
							ChatColor.translateAlternateColorCodes('&', Settings.get().getString("minutesLeft")),
							timeLeft / 60));
					
				}
				if (timeLeft == 60) {
					Bukkit.broadcastMessage(MessageFormat.format(
							ChatColor.translateAlternateColorCodes('&', Settings.get().getString("minuteLeft")), 1));
				}
				if (timeLeft == 30) {
					Bukkit.broadcastMessage(MessageFormat.format(
							ChatColor.translateAlternateColorCodes('&', Settings.get().getString("secondsLeft")),
							timeLeft));
				}
				if (timeLeft <= 5 && timeLeft > 0) {
					Bukkit.broadcastMessage(MessageFormat.format(
							ChatColor.translateAlternateColorCodes('&', Settings.get().getString("secondsLeft")),
							timeLeft));
				}
				if (timeLeft <= 0) {
					Game.get().endGame();
				}
			}
		}, 0L, 20L);
	}

	public GamePlayer getGamePlayer(Player player) {
		return gamePlayers.get(player.getUniqueId().toString());
	}

	public void onPlayerKilled(Player shooter, Player victim) {
		getGamePlayer(victim).deaths++;
		getGamePlayer(victim).respawn();
		getGamePlayer(shooter).kills++;
		Bukkit.broadcastMessage(
				MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("killed")),
						victim.getName(), shooter.getName())); 
		Score score = this.objective.getScore(shooter.getDisplayName()); 
		score.setScore(getGamePlayer(shooter).kills); 
	}

	public static float calcKD(int kills, int deaths){ 
		deaths = deaths > 0 ? deaths : 1; 
		return (float)kills/(float)deaths; 
	}
	
	@SuppressWarnings({ "deprecation" })
	public void endGame() {
		Bukkit.getScheduler().cancelTask(countdownTask);
		gameState = GameState.FINISHED;

		List<GamePlayer> ranked = new ArrayList<GamePlayer>();

		for (Entry<String, GamePlayer> entry : this.gamePlayers.entrySet()) {
			ranked.add(entry.getValue());
		}

		Collections.sort(ranked, new Comparator<GamePlayer>() {
			public int compare(GamePlayer o1, GamePlayer o2) {
				if (calcKD(o1.kills, o2.deaths) == calcKD(o2.kills, o2.deaths)) 
					return 0;
				return calcKD(o1.kills, o2.deaths) > calcKD(o2.kills, o2.deaths) ? -1 : 1;
			}
		});

		int n = 0; 
		
		String message = String.format("%3s | %2s | %s" , "#", "KD", "Name").toString()+"\n"; 
		
		for (GamePlayer gamePlayer : ranked) { 
			message += String.format("%2d. | %.1f | %s", 
	        		(n+1), 
	        		calcKD(gamePlayer.kills, gamePlayer.deaths), 
	        		gamePlayer.player.getDisplayName() 
	        ).toString()+"\n"; 
			n++; 
		} 
		
		Bukkit.broadcastMessage(ChatColor.GOLD + message); 
		
		Bukkit.broadcastMessage(MessageFormat
				.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("lobbyMessage")), 5));
		Bukkit.getScheduler().runTaskLater(Instagib.get(), new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.getInventory().clear();
					p.setResourcePack("http://collab.toothwit.ch/emptyPack.zip");
					Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), p);
				}
				Game.get().reload();
			} 
		}, 5 * 20L); 
		
		scoreboard.clearSlot(DisplaySlot.SIDEBAR); 
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public GameState getGameState() {
		return this.gameState;
	}
}
