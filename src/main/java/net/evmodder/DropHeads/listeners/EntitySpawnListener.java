package net.evmodder.DropHeads.listeners;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import net.evmodder.DropHeads.DropHeads;
import net.evmodder.EvLib.FileIO;

public class EntitySpawnListener implements Listener{
	private Map<SpawnReason, Float> spawnModifiers = new HashMap<SpawnReason, Float>();
	private DropHeads plugin;
	
	public EntitySpawnListener(){
		plugin = DropHeads.getPlugin();

		//load spawn cause modifiers
		InputStream defaultModifiers = plugin.getClass().getResourceAsStream("/spawn-cause-modifiers.txt");
		String modifiers = FileIO.loadFile("spawn-cause-modifiers.txt", defaultModifiers);
		for(String line : modifiers.split("\n")){
			line = line.replace(" ", "").replace("\t", "").toUpperCase();
			int i = line.indexOf(":");
			if(i != -1){
				try{
					SpawnReason reason = SpawnReason.valueOf(line.substring(0, i));
					Float modifier = Float.parseFloat(line.substring(i+1));
					if(Math.abs(1F - modifier) > 0.0001) spawnModifiers.put(reason, modifier);
				}
				catch(IllegalArgumentException ex){
					plugin.getLogger().warning("Unknown SpawnReason: '"+line+"' in config file!");
				}
			}
		}
		if(spawnModifiers.isEmpty()){
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entitySpawnEvent(CreatureSpawnEvent evt){
		if(!evt.isCancelled() && evt.getSpawnReason() != null){
			Float modifier = spawnModifiers.get(evt.getSpawnReason());
			if(modifier != null){
				evt.getEntity().setMetadata("SpawnReason", new FixedMetadataValue(plugin, modifier));
				evt.getEntity().addScoreboardTag("SpawnReasonModifier:"+modifier);
			}
		}
	}
}