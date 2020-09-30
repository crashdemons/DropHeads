/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.evmodder.DropHeads.integration;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author crashdemons (crashenator at gmail.com)
 */
public class HeadDBSupport{
    private HeadDatabaseAPI hdbAPI = null;
    private Plugin pl = null;
    public HeadDBSupport(Plugin parentPlugin){
        pl = parentPlugin;
    }
    
    
    public HeadDatabaseAPI getAPI(){//either check this for null or use isReady to prevent runtime errors.
        return hdbAPI;
    }
    
    public boolean isInstalled(){
        boolean hdbInstalled = true;
        try{Class.forName("me.arcaniax.hdb.api.DatabaseLoadEvent");}
        catch(ClassNotFoundException ex){hdbInstalled = false;}
        return hdbInstalled;
    }
    
    public boolean isReady(){//wrap all Head-DB code in either isReady or isInstalled. this will prevent runtime exceptions
        return hdbAPI!=null;
    }
    
    public void registerAPI(){
        pl.getServer().getPluginManager().registerEvents(new Listener(){
                @EventHandler public void onDatabaseLoad(DatabaseLoadEvent e){
                        HandlerList.unregisterAll(this);
                        hdbAPI = new HeadDatabaseAPI();
                        /*MAX_HDB_ID = JunkUtils.binarySearch(
                                id -> {
                                        try{return api.isHead(""+id);}
                                        catch(NullPointerException nullpointer){return false;}
                                },
                                0, Integer.MAX_VALUE
                        );*/
                }
        }, pl);
    }
    
    
}
