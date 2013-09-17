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

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import me.hoot215.headshot.metrics.Metrics;
import me.hoot215.headshot.metrics.Metrics.Graph;
import me.hoot215.updater.AutoUpdater;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Headshot extends JavaPlugin
  {
    private static Headshot instance;
    private final Map<Player, Hit> headshots = new WeakHashMap<Player, Hit>();
    
    public static Headshot getInstance ()
      {
        return instance;
      }
    
    public boolean hasLastHeadshot (Player player)
      {
        return headshots.containsKey(player);
      }
    
    public Hit getLastHeadshot (Player player)
      {
        return headshots.get(player);
      }
    
    public void setLastHeadshot (Player player, Hit hit)
      {
        headshots.put(player, hit);
      }
    
    public void applyEffect (LivingEntity entity, String effect)
      {
        String[] args = effect.split(":");
        int probability = 100;
        try
          {
            int i = Integer.parseInt(args[0]);
            if (i >= 1 && i <= 100)
              {
                probability = i;
              }
            else
              {
                this.getLogger().warning(
                    "Probability cannot be less than 1 or greater than 100");
              }
          }
        catch (NumberFormatException e)
          {
            this.getLogger().warning(e + " is not a valid integer");
          }
        if (new Random().nextInt(100) + 1 > probability)
          return;
        String type = args[1];
        if (type.equals("PotionEffect"))
          {
            PotionEffectType potionEffectType =
                PotionEffectType.getByName(args[2]);
            if (potionEffectType == null)
              {
                this.getLogger().severe(
                    args[2] + " is not a valid PotionEffectType");
                return;
              }
            int duration = 20;
            try
              {
                int i = Integer.parseInt(args[3]);
                if (i >= 1)
                  {
                    duration = i;
                  }
                else
                  {
                    this.getLogger().warning("Duration cannot be less than 1");
                  }
              }
            catch (NumberFormatException e)
              {
                this.getLogger().warning(args[3] + " is not a valid integer");
              }
            int amplifier = 0;
            try
              {
                int i = Integer.parseInt(args[4]);
                if (i >= 0)
                  {
                    amplifier = i;
                  }
                else
                  {
                    this.getLogger().warning("Amplifier cannot be less than 0");
                  }
              }
            catch (NumberFormatException e)
              {
                this.getLogger().warning(args[4] + " is not a valid integer");
              }
            PotionEffect potionEffect =
                potionEffectType.createEffect(duration, amplifier);
            entity.addPotionEffect(potionEffect);
          }
        else
          {
            this.getLogger().severe(type + " is not a valid effect type");
          }
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
        
        try
          {
            Metrics metrics = new Metrics(this);
            Graph graph = metrics.createGraph("Enabled Headshot Modes");
            graph.addPlotter(new Metrics.Plotter("Both")
              {
                
                @Override
                public int getValue ()
                  {
                    return getConfig().getBoolean("distance.enabled")
                        && getConfig().getBoolean("hitboxes.enabled") ? 1 : 0;
                  }
              });
            graph.addPlotter(new Metrics.Plotter("Distance")
              {
                
                @Override
                public int getValue ()
                  {
                    return getConfig().getBoolean("distance.enabled")
                        && !getConfig().getBoolean("hitboxes.enabled") ? 1 : 0;
                  }
              });
            graph.addPlotter(new Metrics.Plotter("Hitboxes")
              {
                
                @Override
                public int getValue ()
                  {
                    return !getConfig().getBoolean("distance.enabled")
                        && getConfig().getBoolean("hitboxes.enabled") ? 1 : 0;
                  }
              });
            metrics.start();
          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
        
        new AutoUpdater(this).start();
        
        this.getLogger().info("Is now enabled");
      }
  }
