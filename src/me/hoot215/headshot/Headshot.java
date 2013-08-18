/*
 * Headshot players with arrows if shot from far enough away.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.hoot215.headshot;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Headshot extends JavaPlugin
  {
    private static Headshot instance;
    private final Map<Player, EntityDamageByEntityEvent> headshots =
        new WeakHashMap<Player, EntityDamageByEntityEvent>();
    
    public static Headshot getInstance ()
      {
        return instance;
      }
    
    public boolean hasLastHeadshot (Player player)
      {
        return headshots.containsKey(player);
      }
    
    public EntityDamageByEntityEvent getLastHeadshot (Player player)
      {
        return headshots.get(player);
      }
    
    public void setLastHeadshot (Player player, EntityDamageByEntityEvent event)
      {
        headshots.put(player, event);
      }
    
    @Override
    public void onDisable ()
      {
        instance = null;
        
        this.getLogger().info("Is now disabled");
      }
    
    @Override
    public void onEnable ()
      {
        instance = this;
        
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        
        this.getServer().getPluginManager()
            .registerEvents(new EntityListener(), this);
        this.getServer().getPluginManager()
            .registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager()
            .registerEvents(new ProjectileListener(), this);
        
        this.getLogger().info("Is now enabled");
      }
  }
