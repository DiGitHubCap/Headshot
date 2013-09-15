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

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class Hit
  {
    private final EntityDamageByEntityEvent event;
    private final double distance;
    
    public Hit (EntityDamageByEntityEvent event, double distance)
      {
        this.event = event;
        this.distance = distance;
      }
    
    public EntityDamageByEntityEvent getEvent ()
      {
        return event;
      }
    
    public double getDistance ()
      {
        return distance;
      }
  }
