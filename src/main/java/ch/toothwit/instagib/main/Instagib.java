package ch.toothwit.instagib.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ch.toothwit.instagib.events.PlayerEventListener;
import ch.toothwit.instagib.events.ServerEventListener;
import ch.toothwit.lobby.main.LobbyAPI;
import net.md_5.bungee.api.ChatColor; 

public class Instagib extends JavaPlugin {
	private static Instagib instance;
	
	
	@Override
	public void onEnable() { 
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), this); 
		Bukkit.getPluginManager().registerEvents(new ServerEventListener(), this); 
		
		LobbyAPI.test(); 
		
		getLogger().info("Lobby was enabled");
	}

	
	
	@Override
	public void onDisable() {
		getLogger().info("Lobby was disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
		if ((command.getName().equalsIgnoreCase("insta") || command.getName().equalsIgnoreCase("instagib"))) { 
			if(args.length > 0){
				String cmd = args[0]; 
				Player player = (Player)sender; 
				boolean unknownUserCommand = false; 
				boolean unknownAdminCommand = false; 
				if(sender.hasPermission("instagib.user")){
					if(cmd.equalsIgnoreCase("leave")){
						Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), (Player)sender);
					} 
					else {
						unknownUserCommand = true; 
					}
				}
				if(sender.hasPermission("instagib.admin")){
					if(cmd.equalsIgnoreCase("addSpawn")){
						Settings.get().addSpawnLocation(player.getLocation()); 
						player.sendMessage(ChatColor.GOLD+"Spawn hinzugef\u00FCgt."); 
					}
					else if(cmd.equalsIgnoreCase("stop")){
						Game.get().setGameState(GameState.STOPPED);  
						player.sendMessage(ChatColor.GOLD+"Spiel gestoppt."); 
					}
					else if(cmd.equalsIgnoreCase("setDuration")){
						Settings.get().setGameDuration(Integer.parseInt(args[1])); 
						player.sendMessage(ChatColor.GOLD+"Spieldauer auf "+ChatColor.RED+""+args[1]+ChatColor.GOLD+" Sekunden gesetzt."); 
					}
					else if(cmd.equalsIgnoreCase("clearSpawns")){ 
						 Settings.get().clearSpawnLocations(); 
						 player.sendMessage(ChatColor.GOLD+"Spawns geloescht."); 
					} 
					else {
						unknownAdminCommand = true; 
					}
					
					if((unknownUserCommand && !(sender.hasPermission("instagib.admin"))) || (unknownUserCommand && unknownAdminCommand)){
						printHelp(sender); 
					}
				} 
				
			}
			else { 
				 printHelp(sender); 
			} 
			return true;  
		} 
		return false; 
	}

	private void printHelp(CommandSender player){
		if(player.hasPermission("instagib.admin")){
			player.sendMessage("Unbekannter Befehl. \n /instagib [addSpawn, stop, setDuration, clearSpawns]"); 
		}
		else { 
			player.sendMessage("Unbekannter Befehl. \n Befehle : '/instagib leave' "); 
		}
	}
	
	public static Instagib get() {
		return instance;
	}

	public Instagib() {
		instance = this;
	}
}
