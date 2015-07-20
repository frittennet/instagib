package de.fredo121.instagib.main;

import java.util.HashMap; 
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Inspired by BlingGravity (http://sethbling.com/downloads/bukkit-plugins/blinggravity/)
 * 
 * @author Bastian Oppermann
 *
 */
public class GravityModifier {
	
	private static final HashMap<UUID, Double> MODIFY = new HashMap<UUID, Double>();
    private static final HashMap<UUID, Vector> OLD_VELOCITIES = new HashMap<UUID, Vector>();
	
	static {
		Bukkit.getScheduler().runTaskTimer(Instagib.get(), new Runnable() {			
			public void run() {
				onTick();
			}
		}, 1, 1);
	}	
	
	private GravityModifier() { }	// singleton
	
	/**
	 * Sets the gravity for the player with the given uuid.
	 * 
	 * @param player The uuid of the player. Won't work for animals.
	 * @param gravity The gravity. A number less than 1 will decrease gravity.
	 */
	public static void modifyGravity(UUID player, double gravity) {
		MODIFY.put(player, gravity);
	}
	
	/**
	 * Resets the gravity for the player with the given uuid.
	 * 
	 * @param player The uuid of the player.
	 */
	public static void resetGravity(UUID player) {
		MODIFY.remove(player);
		OLD_VELOCITIES.remove(player);
	}
	
	private static void onTick() {
		for (Entry<UUID, Double> entry : MODIFY.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			updateVelocities(entry.getValue(), player);
		}
	}
	
	@SuppressWarnings("deprecation")	// cause isOnGround() is deprecated
	private static void updateVelocities(double gravity, Player player) {
    	UUID uuid = player.getUniqueId();
    	Vector newVector = player.getVelocity().clone();
    	if (OLD_VELOCITIES.containsKey(uuid) && !player.isInsideVehicle()) {
    		if (!player.isOnGround()) {
        		Vector oldVector = OLD_VELOCITIES.get(uuid);
    			Vector oldVectorClone = oldVector.clone();
    			oldVectorClone.subtract(newVector);
    			double oldVectorCloneY = oldVectorClone.getY();
    			if (oldVectorCloneY > 0.0D && (newVector.getY() < -0.01D || newVector.getY() > 0.01D)) {
    				newVector.setY(oldVector.getY() - oldVectorCloneY * gravity);
    				boolean newXChanged = newVector.getX() < -0.001D || newVector.getX() > 0.001D;
    				boolean oldXChanged = oldVector.getX() < -0.001D || oldVector.getX() > 0.001D;
    				if (newXChanged && oldXChanged) {
    					newVector.setX(oldVector.getX());
    				}
    				boolean newZChanged = newVector.getZ() < -0.001D || newVector.getZ() > 0.001D;
    				boolean oldZChanged = oldVector.getZ() < -0.001D || oldVector.getZ() > 0.001D;
    				if (newZChanged && oldZChanged) {
    					newVector.setZ(oldVector.getZ());
    				}
    				player.setVelocity(newVector.clone());
    			}
    		}
    	}
    	OLD_VELOCITIES.put(uuid, newVector.clone());
    }

}