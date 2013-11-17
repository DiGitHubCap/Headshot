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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import me.hoot215.headshot.metrics.Metrics;
import me.hoot215.headshot.metrics.Metrics.Graph;
import me.hoot215.updater.AutoUpdater;

import org.bukkit.Effect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Headshot extends JavaPlugin
  {
    private static Headshot instance;
    private final Map<Player, Hit> headshots = new WeakHashMap<Player, Hit>();
    private final Map<String, Long> cooldowns =
        new ConcurrentHashMap<String, Long>();
    private final Set<Arrow> arrows = new HashSet<Arrow>();
    private int effectMakerTask = 0;
    
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
    
    public boolean isCoolingDown (Player player)
      {
        if (cooldowns.containsKey(player.getName()))
          {
            return cooldowns.get(player.getName()) > System.currentTimeMillis();
          }
        else
          return false;
      }
    
    public long getCooldown (Player player)
      {
        return cooldowns.get(player.getName());
      }
    
    public void setCooldown (Player player, long cooldown)
      {
        cooldowns.put(player.getName(), cooldown);
      }
    
    public void addArrow (Arrow arrow)
      {
        arrows.add(arrow);
      }
    
    public void removeArrow (Arrow arrow)
      {
        arrows.remove(arrow);
      }
    
    public void updateEffectMaker ()
      {
        if (effectMakerTask == 0 && arrows.size() > 0)
          {
            effectMakerTask =
                this.getServer().getScheduler()
                    .scheduleSyncRepeatingTask(this, new EffectMaker(), 0, 1);
          }
        else if (effectMakerTask > 0 && arrows.size() == 0)
          {
            this.getServer().getScheduler().cancelTask(effectMakerTask);
            effectMakerTask = 0;
          }
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
        
        new CooldownClearer().start();
        
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
        
        if (this.getConfig().getBoolean("auto-update-notify"))
          {
            new AutoUpdater(this).start();
          }
        
        this.getLogger().info("Is now enabled");
      }
    
    private final class CooldownClearer implements Runnable
      {
        public void start ()
          {
            new Thread(this).start();
          }
        
        @Override
        public void run ()
          {
            while (instance != null)
              {
                Iterator<Entry<String, Long>> iter =
                    cooldowns.entrySet().iterator();
                while (iter.hasNext())
                  {
                    Entry<String, Long> e = iter.next();
                    if (e.getValue() <= System.currentTimeMillis())
                      {
                        iter.remove();
                      }
                  }
              }
            try
              {
                Thread.sleep(3600000);
              }
            catch (InterruptedException e)
              {
                e.printStackTrace();
              }
          }
      }
    
    private final class EffectMaker implements Runnable
      {
        @Override
        public void run ()
          {
            Iterator<Arrow> iter = arrows.iterator();
            while (iter.hasNext())
              {
                Arrow arrow = iter.next();
                if (arrow.isOnGround() || arrow.isDead())
                  {
                    iter.remove();
                    continue;
                  }
                arrow.getWorld().playEffect(arrow.getLocation(),
                    Effect.MOBSPAWNER_FLAMES, 0, 256);
              }
            updateEffectMaker();
          }
      }
  }
