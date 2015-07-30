package ch.toothwit.instagib.events;

import org.bukkit.event.Listener;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import ch.toothwit.instagib.main.Game;

public class ServerEventListener implements Listener {
	@EventHandler
	public void onServerListPing(ServerListPingEvent event) { 
		switch (Game.get().getGameState()) {
			case RUNNING:
			case PREPARATION:
			case FINISHED:
				event.setMotd(ChatColor.AQUA + "[Laufend]");
				break;
			case LOBBY:
				event.setMotd(ChatColor.GREEN + "[Lobby]");
				break;
			case STOPPED:
				event.setMotd(ChatColor.DARK_RED + "[Gestoppt]");
				break;
		}
	} 
	
	@EventHandler 
	public void onWeatherChangeEvent(WeatherChangeEvent event){ 
		if(event.toWeatherState()){ 
			event.setCancelled(true);
	        event.getWorld().setStorm(false);
		} 
	} 
}