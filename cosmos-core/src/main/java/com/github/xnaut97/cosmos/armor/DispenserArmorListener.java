package com.github.xnaut97.cosmos.armor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

import java.util.Comparator;

/**
 * @author Arnah
 * @since Feb 08, 2019
 */
public class DispenserArmorListener implements Listener{
	
	
	@EventHandler
	public void dispenseArmorEvent(BlockDispenseEvent event){
		ArmorType type = ArmorType.matchType(event.getItem());
		Location location = event.getBlock().getLocation();
		if(type != null){
			Location frontLocation = location.clone().add(event.getVelocity());
			LivingEntity nearestEnemy = frontLocation.getWorld()
					.getNearbyEntities(frontLocation, 1, 1, 1).stream()
					.filter(LivingEntity.class::isInstance)
					.map(LivingEntity.class::cast)
					.min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(frontLocation)))
					.orElse(null);
			if(nearestEnemy instanceof Player){
				Player player = (Player) nearestEnemy;
				ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player,
						ArmorEquipEvent.EquipMethod.DISPENSER,
						type,
						null,
						event.getItem());
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if(armorEquipEvent.isCancelled()){
					event.setCancelled(true);
				}
			}
		}
	}
}