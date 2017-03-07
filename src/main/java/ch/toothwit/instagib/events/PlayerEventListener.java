package ch.toothwit.instagib.events;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import ch.toothwit.instagib.main.Game;
import ch.toothwit.instagib.main.GamePlayer;
import ch.toothwit.instagib.main.GameState;
import ch.toothwit.instagib.main.Instagib;
import ch.toothwit.instagib.main.Target;
import ch.toothwit.instagib.main.Util;
import ch.toothwit.lobby.main.LobbyAPI;


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
	public void onPlayerQuitEvent(PlayerQuitEvent event) { 
        Player p = event.getPlayer(); 
	    p.getInventory().clear(); 
        p.setResourcePack("http://collab.toothwit.ch/emptyPack.zip"); 
        // Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), p); 
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
	
	private static Set<Material> transparentMaterial = new HashSet<Material>(
	    Arrays.asList(
	        Material.WATER, 
	        Material.STATIONARY_WATER, 
	        Material.AIR, 
	        Material.LAVA, 
	        Material.STATIONARY_LAVA, 
	        Material.VINE, 
	        Material.REDSTONE, 
	        Material.BARRIER, 
	        Material.TRIPWIRE, 
	        Material.STONE_BUTTON, 
	        Material.WOOD_BUTTON, 
	        Material.TRIPWIRE_HOOK, 
	        Material.LONG_GRASS, 
	        Material.LADDER, 
	        Material.SUGAR_CANE_BLOCK, 
	        Material.YELLOW_FLOWER, 
	        Material.RED_ROSE, 
	        Material.RED_MUSHROOM, 
	        Material.BROWN_MUSHROOM, 
	        Material.DOUBLE_PLANT, 
	        Material.SAPLING, 
	        Material.TORCH, 
	        Material.REDSTONE_TORCH_OFF, 
	        Material.REDSTONE_TORCH_ON, 
	        Material.CROPS, 
	        Material.LEVER 
	    )
	); 
	
	@EventHandler 
	public void onPlayerUse(PlayerInteractEvent event){ 
		if(Game.get().getGameState() == GameState.RUNNING){ 
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){ 
				Player player = event.getPlayer(); 
				if(player.getItemInHand().getType() == Material.STICK){ 
					if(!Game.get().getCooldown(player)){ 
						
						Vector velocity = player.getVelocity(); 
						velocity.add((new Vector()).subtract(player.getLocation().getDirection().multiply(0.7f))); 
						player.setVelocity(velocity); 
						
						Player target = Target.getTargetPlayer(player); 
						Location headLocation = player.getLocation().add(0d, 1.5d, 0d); 
						Vector vec = player.getLocation().getDirection(); 
						
						double blockDistance = maxDistance; 
						double targetDistance = maxDistance; 
						
						Block b = player.getTargetBlock(transparentMaterial, 1000); 
						if(b != null){
							blockDistance = b.getLocation().distance(headLocation); 
						}
						
						if(target != null){
							targetDistance = target.getLocation().distance(headLocation); 
						}
						
						if(targetDistance < blockDistance){ 
							for(int n=0;n<10;n++){
								target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152); 
							} 
							Game.get().onPlayerKilled(player, target); 
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
		if(Game.get().getGameState() == GameState.RUNNING && event.getCause() == EntityDamageEvent.DamageCause.VOID){ 
			Entity entity = event.getEntity(); 
			if(entity != null){ 
				GamePlayer gamePlayer = Game.get().getGamePlayer((Player)event.getEntity()); 
				gamePlayer.deaths++; 
				gamePlayer.respawn(); 
			}
		} 
		event.setCancelled(true); 
	} 
} 
