/*
 * DropHeads - a Bukkit plugin for naturally dropping mob heads
 *
 * Copyright (C) 2017 - 2020 Nathan / EvModder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.evmodder.DropHeads;

import com.github.crashdemons.playerheads.compatibility.Compatibility;
import com.github.crashdemons.playerheads.compatibility.exceptions.*;


import net.evmodder.DropHeads.commands.*;
import net.evmodder.DropHeads.listeners.*;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.Updater;

//TODO:
// * /dropheads reload
// * DeathChest bypass (perhaps as addon?)
// * Cracked iron golem head (repair with ingot?)
// * attempt-place-head-block, attempt-place-overwrite-liquids, facing-direction, place-as: KILLER/VICTIM/SERVER, what to do if blockplaceevent fails
// * overwrite blocks: ['AIR', 'WATER, 'GRASS']
// * middle-click copy with correct item name
// * jeb_ sheep head animated phasing through colors (like the jeb_ sheep)
// * if mob has custom name, use it in head name (configurable)
// * move textures from head-textures.txt to DropHeads/textures/MOB_NAME.txt => "SHEEP|RED: value \n SHEEP|BLUE: value ..."
// * using above, inside /textures/MOB_NAME.txt, set 'drop-rate: x' to modify chance for that sub-type only
// * Multiple possible behead messages, with one picked randomly EG:["$ was beheaded", "$ lost their head", "$ got decapitated"]
// * stray texture skull match mob colors
// * hollow stray skull using Ev resource pack? (custom model data or head tag)
// * for non-living (Vehicles, Hanging), cancel self-drop if head drop is triggered (configurable)
//TEST:
// * Fix HDB crash
// * /gethead player:ShEeP, /gethead mob:SHEEP, /gethead hdb:334
// * /droprate - check (TODO: or edit) per mob (& cmd for spawn modifiers)
// * /gethead mob:PLAYER|ALEX
/*
 * log:
 *   enable: true
 *   filename: 'dropheads-log.txt'
 *   log-mob-behead: true
 *   log-player-behead: true
 *   log-head-command: true
 *   format-mob-behead: '${timestamp},mob decapitated,${victim},${killer},${item}'
 *   format-player-behead: '${timestamp},player decapitated,${victim},${killer},${item}'
 *   format-head-command: '${timestamp},gethead command,${sender},${head}'
 */

public final class DropHeads extends EvPlugin{
	private static DropHeads instance; public static DropHeads getPlugin(){return instance;}
	private HeadAPI api;
	public HeadAPI getAPI(){return api;}
	private boolean LOGFILE_ENABLED;
	private String LOGFILE_NAME;

        
        
        
        

    private void initializePHCompatibility(){
        boolean isUsingRecommendedVersion=true;
        try{
            isUsingRecommendedVersion = Compatibility.init();
        }catch(CompatibilityConflictException | CompatibilityUnavailableException | CompatibilityUnsupportedException | UnknownVersionException e){
            getLogger().warning("[PH-Compat-Lib] error initialzing.");
            throw e;//fatal
        }
        
        if(!isUsingRecommendedVersion){ 
            getLogger().warning("[PH-Compat-Lib] using fallback server support - problems may occur.");
        }
    }
        
        
	@Override public void onEvEnable(){
                initializePHCompatibility();
		if(config.getBoolean("update-plugin", false)){
			new Updater(/*plugin=*/this, /*id=*/274151, getFile(), Updater.UpdateType.DEFAULT, /*announce=*/true);
			//todo: if 'update-textures', trigger overwrite in HeadAPI
		}
		instance = this;
		api = new HeadAPI();
		EntityDeathListener deathListener = new EntityDeathListener();
		if(config.getBoolean("track-mob-spawns", true)){
			getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
		}
		if(config.getBoolean("drop-for-ranged-kills", false)){
			getServer().getPluginManager().registerEvents(new ProjectileFireListener(), this);
		}
		if(config.getBoolean("drop-for-indirect-kills", false)){
			getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
		}
		if(config.getBoolean("refresh-textures", false)){
			getServer().getPluginManager().registerEvents(new ItemDropListener(), this);
		}
		if(config.getBoolean("head-click-listener", true)){
			getServer().getPluginManager().registerEvents(new BlockClickListener(), this);
		}
		if(config.getBoolean("save-custom-lore", true)){
			getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
			getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
		}

		new CommandSpawnHead(this);
		new CommandDropRate(this, deathListener);
		new Commanddebug_all_heads(this);

		LOGFILE_ENABLED = config.getBoolean("log.enable", false);
		if(LOGFILE_ENABLED) LOGFILE_NAME = config.getString("log.filename", "log.txt");
	}

	public boolean writeToLogFile(String line){
		if(!LOGFILE_ENABLED) return false;
		// Write to log
		line = line.replace("\n", "")+"\n";
		getLogger().fine("Writing line to logfile: "+line);
		return FileIO.saveFile(LOGFILE_NAME, line, /*append=*/true);
	}
}