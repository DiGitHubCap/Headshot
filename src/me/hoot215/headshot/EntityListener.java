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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;

public class EntityListener implements Listener
  {
    private final Headshot plugin = Headshot.getInstance();
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity (EntityDamageByEntityEvent event)
      {
        if ( ! (event.getDamager() instanceof Arrow)
            || ! (event.getEntity() instanceof LivingEntity))
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
        double min = plugin.getConfig().getDouble("min-distance");
        if (distance < min)
          return;
        double damage = event.getDamage();
        damage +=
            plugin.getConfig().getDouble("extra-damage-per-block")
                * (distance - min);
        event.setDamage(damage);
        if (entity instanceof Player)
          {
            Player player = (Player) entity;
            player.setLastDamageCause(event);
            plugin.setLastHeadshot(player, new Hit(event, distance));
            if (damage >= plugin.getConfig().getDouble("headshot-damage"))
              {
                String shooterMessage =
                    String.format(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig()
                            .getString("strings.headshot-shooter")), player
                        .getName());
                shooter.sendMessage(shooterMessage);
                String playerMessage =
                    String.format(ChatColor
                        .translateAlternateColorCodes('&', plugin.getConfig()
                            .getString("strings.headshot-player")), shooter
                        .getName());
                player.sendMessage(playerMessage);
              }
          }
      }
  }
