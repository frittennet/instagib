package de.fredo121.instagib.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import de.fredo121.instagib.events.PlayerEventListener;
import de.fredo121.instagib.events.ServerEventListener;
import de.fredo121.lobby.main.LobbyAPI;
import net.md_5.bungee.api.ChatColor; 

public class Instagib extends JavaPlugin {
	private static Instagib instance;
	
	
	@Override
	public void onEnable() { 
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), this); 
		Bukkit.getPluginManager().registerEvents(new ServerEventListener(), this); 
		
		getLogger().info("Lobby was enabled");
	}

	
	
	@Override
	public void onDisable() {
		getLogger().info("Lobby was disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
		if (command.getName().equalsIgnoreCase("insta") || command.getName().equalsIgnoreCase("instagib")) { 
			String cmd = args[0]; 
			Player player = (Player)sender; 
			if(sender.hasPermission("instagib.user")){
				if(cmd.equalsIgnoreCase("leave")){
					Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), (Player)sender);
				}
			}
			if(sender.hasPermission("instagib.admin")){
				if(cmd.equalsIgnoreCase("addSpawn")){
					Settings.get().addSpawnLocation(player.getLocation()); 
					player.sendMessage(ChatColor.GOLD+"Spawn hinzugefügt."); 
				}
				else if(cmd.equalsIgnoreCase("stop")){
					Game.get().setGameState(GameState.STOPPED); 
					player.sendMessage(ChatColor.GOLD+"Spiel gestoppt."); 
				}
				else if(cmd.equalsIgnoreCase("setDuration")){
					Settings.get().setGameDuration(Integer.parseInt(args[1])); 
					player.sendMessage(ChatColor.GOLD+"Spieldauer auf"+ChatColor.RED+args[1]+" gesetzt."); 
				}
				else{
					player.sendMessage("Unbekannter Befehl."); 
				}
			} 
			
			return true; 
		}
		return false; 
	}

	public static Instagib get() {
		return instance;
	}

	public Instagib() {
		instance = this;
	}
}
