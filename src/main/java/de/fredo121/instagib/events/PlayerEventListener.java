package de.fredo121.instagib.events;


import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import de.fredo121.instagib.main.Game;
import de.fredo121.instagib.main.GamePlayer;
import de.fredo121.instagib.main.GameState;
import de.fredo121.instagib.main.Target;
import de.fredo121.instagib.main.Util;
import de.fredo121.lobby.main.LobbyAPI;


@SuppressWarnings("deprecation")
public class PlayerEventListener implements Listener { 
	private static int maxDistance = 1000; 
	private static int interval = 1; 
	 
	@EventHandler 
	public void onPlayerMoveEvent(PlayerMoveEvent event){ 
		GameState gameState = Game.get().getGameState(); 
		if(gameState == GameState.RUNNING || gameState == GameState.FINISHED){ 
			event.getPlayer().setWalkSpeed(0.45f); 
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, 2, false, false)); 
		} 
	} 
	
	@EventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){           
		event.setCancelled(true);
	} 
	
	@EventHandler 
	public void onPlayerDropItemEvent(PlayerDropItemEvent event)
	{
		if(Game.get().getGameState() != GameState.STOPPED){ 
			event.setCancelled(true); 
		}
	} 
	
	@EventHandler 
	public void onInventoryClickEvent(InventoryClickEvent event)
	{
		if(Game.get().getGameState() != GameState.STOPPED){ 
			event.setCancelled(true); 
		}
	} 
	
	@EventHandler 
	public void onInventoryDragEvent(InventoryDragEvent event)
	{
		if(Game.get().getGameState() != GameState.STOPPED){ 
			event.setCancelled(true); 
		}
	} 
	
	@EventHandler 
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event)
	{
		if(Game.get().getGameState() != GameState.STOPPED){ 
			event.setCancelled(true); 
		} 
	} 
	
	
	@EventHandler 
	public void onPlayerJoinEvent(PlayerJoinEvent event){
		if(Game.get().getGameState() != GameState.LOBBY && Game.get().getGameState() != GameState.STOPPED){ 
			Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), event.getPlayer());
		} 
		event.setJoinMessage(""); 
	}
	
	@EventHandler 
	public void onPlayerUse(PlayerInteractEvent event){ 
		if(Game.get().getGameState() == GameState.RUNNING){ 
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){ 
				Player player = event.getPlayer(); 
				GamePlayer gamePlayer = Game.get().getGamePlayer(player); 
				if(player.getItemInHand().getType() == Material.STICK){ 
					if(!Game.get().getCooldown(player)){ 
						
						Vector velocity = player.getVelocity(); 
						velocity.add((new Vector()).subtract(player.getLocation().getDirection().multiply(0.7f))); 
						player.setVelocity(velocity); 
						
						Player target = Target.getTargetPlayer(player); 
						Location headLocation = player.getLocation().add(0d, 1.5d, 0d); 
						Vector vec = player.getLocation().getDirection(); 
						World playerWorld = player.getLocation().getWorld(); 
						
						double blockDistance = maxDistance; 
						double targetDistance = maxDistance; 
						
						Block b = player.getTargetBlock((HashSet<Byte>) null, 1000); 
						if(b != null){
							blockDistance = b.getLocation().distance(headLocation); 
						}
						
						if(target != null){
							targetDistance = target.getLocation().distance(headLocation); 
						}
						
						if(targetDistance < blockDistance){
							Game.get().onPlayerKilled(player, target); 
							for(int n=0;n<10;n++){
								target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152); 
							} 
							traceShot((int)targetDistance, vec, headLocation); 
						} 
						else{
							traceShot((int)blockDistance, vec, headLocation); 
						}
						
						for(Player p : Bukkit.getOnlinePlayers()){
							p.playSound(headLocation, "instagib.weapons.rifle", 1f, 1f); 
						}
						
						Game.get().setCooldown(player); 
					} 
				} 
			} 
		} 
		if(Game.get().getGameState() != GameState.STOPPED){
			event.setCancelled(true); 
		}
	} 
	
	private void traceShot(int maxDistance, Vector vec, Location headLocation){
		for (float i = 0; i < maxDistance / interval; i++) { 
			World playerWorld = headLocation.getWorld(); 
			Location currentParticle = new Location(playerWorld, 
					headLocation.getX()+(vec.getX() * i), 
					headLocation.getY()+(vec.getY() * i), 
					headLocation.getZ()+(vec.getZ() * i)); 
            playerWorld.playEffect(currentParticle, Effect.INSTANT_SPELL, 31); 
		} 
	}
	
	
	
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event){ 
		if(event.getCause() == EntityDamageEvent.DamageCause.VOID){ 
			Entity entity = event.getEntity(); 
			if(entity != null){ 
				Game.get().getGamePlayer((Player)event.getEntity()).respawn(); 
			}
		} 
		event.setCancelled(true); 
	} 
} 
