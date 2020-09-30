package net.evmodder.DropHeads.listeners;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import net.evmodder.DropHeads.DropHeads;

public class ProjectileFireListener implements Listener{
	private DropHeads plugin;
	final boolean allowNonPlayerKills;
	
	public ProjectileFireListener(){
		plugin = DropHeads.getPlugin();
		allowNonPlayerKills = plugin.getConfig().getBoolean("drop-for-nonplayer-kills", false);
	}

	private boolean canShootProjectiles(Material type){
		switch(type.name()){
			// TODO: This list is incomplete, but should include all projectiles that trigger an EntityDamageEvent
			case "BOW":
			case "CROSS_BOW":
			case "TRIDENT":
			case "FIRE_CHARGE":
			case "SNOWBALL":
			case "SPLASH_POTION":
			case "LINGERING_POTION":
			case "FISHING_ROD":
			case "EGG":
				return true;
			default:
				return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onProjectileLaunch(ProjectileLaunchEvent evt){
		// Skip if already has metadata (defers to EntityShootBowEvent)
		if(evt.isCancelled() || evt.getEntity().hasMetadata("ShotUsing") || evt.getEntity().getShooter() instanceof LivingEntity == false
				|| (!allowNonPlayerKills && evt.getEntity() instanceof Player == false)) return;

		LivingEntity shooter = (LivingEntity) evt.getEntity().getShooter();
		ItemStack shotUsingItem = shooter.getEquipment().getItemInMainHand();
		if(shotUsingItem == null || !canShootProjectiles(shotUsingItem.getType())) shotUsingItem = shooter.getEquipment().getItemInOffHand();
		if(shotUsingItem != null) evt.getEntity().setMetadata("ShotUsing", new FixedMetadataValue(plugin, shotUsingItem));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityShootBow(EntityShootBowEvent evt){
		if(evt.isCancelled() || (!allowNonPlayerKills && evt.getEntity() instanceof Player == false)) return;
		// This event is more reliable than ProjectileLaunchEvent, so override any existing metadata
		evt.getProjectile().removeMetadata("ShotUsing", plugin);
		evt.getProjectile().setMetadata("ShotUsing", new FixedMetadataValue(plugin, evt.getBow()));
	}
}