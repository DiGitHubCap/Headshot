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

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener
  {
    private final Headshot plugin = Headshot.getInstance();
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath (PlayerDeathEvent event)
      {
        Player player = event.getEntity();
        if ( !plugin.hasLastHeadshot(player))
          return;
        Hit lastHeadshot = plugin.getLastHeadshot(event.getEntity());
        if (lastHeadshot.getEvent() == event.getEntity().getLastDamageCause())
          {
            String deathMessage =
                String.format(ChatColor.translateAlternateColorCodes(
                    '&',
                    plugin.getConfig().getString(
                        "strings.headshot-death-message")), player.getName(),
                    ((Player) ((Arrow) lastHeadshot.getEvent().getDamager())
                        .getShooter()).getName(), String
                        .valueOf((int) lastHeadshot.getDistance()));
            if ( !deathMessage.isEmpty())
              {
                event.setDeathMessage(deathMessage);
              }
            else
              {
                event.setDeathMessage(null);
              }
          }
      }
  }
