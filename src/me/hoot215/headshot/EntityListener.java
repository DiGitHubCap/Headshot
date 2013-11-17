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

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

public class EntityListener implements Listener
  {
    private final Headshot plugin = Headshot.getInstance();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityShootBow (EntityShootBowEvent event)
      {
        if ( ! (event.getEntity() instanceof Player))
          return;
        if (plugin.getConfig().getBoolean("general.particle-trail")
            && event.getProjectile() instanceof Arrow)
          {
            plugin.addArrow((Arrow) event.getProjectile());
            plugin.updateEffectMaker();
          }
        Player player = (Player) event.getEntity();
        if ( !player.hasPermission("headshot.bypass.reload-time"))
          {
            long cooldown =
                plugin.getConfig().getLong("general.reload-time", 0) * 50;
            if (cooldown == 0)
              return;
            plugin.setCooldown(player, System.currentTimeMillis() + cooldown);
          }
      }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity (EntityDamageByEntityEvent event)
      {
        if ( ! (event.getDamager() instanceof Arrow)
            || ! (event.getEntity() instanceof HumanEntity
                || event.getEntity() instanceof NPC
                || event.getEntity() instanceof Snowman
                || event.getEntity() instanceof Creeper
                || event.getEntity() instanceof Skeleton
                || event.getEntity() instanceof Witch || event.getEntity() instanceof Zombie))
          return;
        Arrow arrow = (Arrow) event.getDamager();
        LivingEntity entity = (LivingEntity) event.getEntity();
        if ( ! (arrow.getShooter() instanceof Player))
          return;
        Player shooter = (Player) arrow.getShooter();
        List<MetadataValue> values = arrow.getMetadata("spawn-location");
        if (values.isEmpty())
          return;
        Location origin = (Location) values.get(0).value();
        Location loc = arrow.getLocation();
        double distance = origin.distance(loc);
        boolean announce = false;
        boolean messaged = false;
        distance : if (plugin.getConfig().getBoolean("distance.enabled"))
          {
            double min = plugin.getConfig().getDouble("min-distance");
            if (distance < min)
              break distance;
            double damage = event.getDamage();
            damage +=
                plugin.getConfig().getDouble("extra-damage-per-block")
                    * (distance - min);
            event.setDamage(damage);
            if (entity instanceof Player)
              {
                Player player = (Player) entity;
                if (damage >= plugin.getConfig().getDouble("headshot-damage"))
                  {
                    announce = true;
                    String shooterMessage =
                        String.format(ChatColor.translateAlternateColorCodes(
                            '&',
                            plugin.getConfig().getString(
                                "strings.headshot-shooter")), player.getName());
                    if ( !shooterMessage.isEmpty())
                      {
                        shooter.sendMessage(shooterMessage);
                      }
                    String playerMessage =
                        String.format(ChatColor.translateAlternateColorCodes(
                            '&',
                            plugin.getConfig().getString(
                                "strings.headshot-player")), shooter.getName());
                    if ( !playerMessage.isEmpty())
                      {
                        player.sendMessage(playerMessage);
                      }
                    messaged = true;
                  }
              }
          }
        boolean damage = plugin.getConfig().getBoolean("hitboxes.enabled");
        Location relative = loc.clone().subtract(entity.getLocation());
        // Head
        double value = event.getDamage();
        if (relative.getY() >= plugin.getConfig().getDouble(
            "hitboxes.head.above"))
          {
            if (damage)
              {
                announce = true;
                value =
                    value
                        * plugin.getConfig().getDouble(
                            "hitboxes.head.multiplier");
                for (String s : plugin.getConfig().getStringList(
                    "hitboxes.head.effects"))
                  {
                    plugin.applyEffect(entity, s);
                  }
                if ( !messaged && entity instanceof Player)
                  {
                    Player player = (Player) entity;
                    String shooterMessage =
                        String.format(ChatColor.translateAlternateColorCodes(
                            '&',
                            plugin.getConfig().getString(
                                "strings.headshot-shooter")), player.getName());
                    if ( !shooterMessage.isEmpty())
                      {
                        shooter.sendMessage(shooterMessage);
                      }
                    String playerMessage =
                        String.format(ChatColor.translateAlternateColorCodes(
                            '&',
                            plugin.getConfig().getString(
                                "strings.headshot-player")), shooter.getName());
                    if ( !playerMessage.isEmpty())
                      {
                        player.sendMessage(playerMessage);
                      }
                  }
              }
            if (entity instanceof Player)
              {
                ItemStack item = ((Player) entity).getInventory().getHelmet();
                if (item != null)
                  {
                    value =
                        value
                            / plugin.getConfig().getDouble(
                                "armour-damage-divisor." + item, 1);
                  }
              }
          }
        // Feet
        else if (relative.getY() <= plugin.getConfig().getDouble(
            "hitboxes.feet.below"))
          {
            if (damage)
              {
                value =
                    value
                        * plugin.getConfig().getDouble(
                            "hitboxes.feet.multiplier");
                for (String s : plugin.getConfig().getStringList(
                    "hitboxes.feet.effects"))
                  {
                    plugin.applyEffect(entity, s);
                  }
              }
            if (entity instanceof Player)
              {
                ItemStack item = ((Player) entity).getInventory().getBoots();
                if (item != null)
                  {
                    value =
                        value
                            / plugin.getConfig().getDouble(
                                "armour-damage-divisor." + item, 1);
                  }
              }
          }
        // Legs
        else if (relative.getY() <= plugin.getConfig().getDouble(
            "hitboxes.legs.below"))
          {
            if (damage)
              {
                value =
                    value
                        * plugin.getConfig().getDouble(
                            "hitboxes.legs.multiplier");
                for (String s : plugin.getConfig().getStringList(
                    "hitboxes.legs.effects"))
                  {
                    plugin.applyEffect(entity, s);
                  }
              }
            if (entity instanceof Player)
              {
                ItemStack item = ((Player) entity).getInventory().getLeggings();
                if (item != null)
                  {
                    value =
                        value
                            / plugin.getConfig().getDouble(
                                "armour-damage-divisor." + item, 1);
                  }
              }
          }
        // Torso
        else
          {
            if (damage)
              {
                value =
                    value
                        * plugin.getConfig().getDouble(
                            "hitboxes.torso.multiplier");
                for (String s : plugin.getConfig().getStringList(
                    "hitboxes.torso.effects"))
                  {
                    plugin.applyEffect(entity, s);
                  }
              }
            if (entity instanceof Player)
              {
                ItemStack item =
                    ((Player) entity).getInventory().getChestplate();
                if (item != null)
                  {
                    value =
                        value
                            / plugin.getConfig().getDouble(
                                "armour-damage-divisor." + item, 1);
                  }
              }
          }
        if (value != event.getDamage())
          {
            event.setDamage(value);
          }
        if (entity instanceof Player)
          {
            Player player = (Player) entity;
            player.setLastDamageCause(event);
            if (announce)
              {
                plugin.setLastHeadshot(player, new Hit(event, distance));
              }
          }
      }
  }
