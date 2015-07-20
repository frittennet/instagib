package de.fredo121.instagib.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

public class CustomArrow  {
	public int taskId; 
	public Arrow arrow; 
	public Vector velocity; 
	
	public CustomArrow(Arrow arrow, Vector velocity){
		this.arrow = arrow; 
		this.arrow.setCritical(true);
		this.velocity = velocity; 
		this.taskId = fixArrow(this); 
	} 
	
	private static int fixArrow(final CustomArrow customArrow){
		return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Instagib.get(), new Runnable(){ 
			private int count = 0; 
            public void run() { 
            	if(customArrow.arrow.isOnGround() || count > 40 || !customArrow.arrow.isValid()){ 
                	Bukkit.getScheduler().cancelTask(customArrow.taskId); 
                	customArrow.arrow.remove(); 
                } 
            	count++; 
                customArrow.arrow.setVelocity(customArrow.velocity); 
            } 
        }, 0L, 1L); 
	} 
}
