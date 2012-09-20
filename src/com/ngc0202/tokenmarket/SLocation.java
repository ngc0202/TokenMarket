package com.ngc0202.tokenmarket;

import java.io.Serializable;
import org.bukkit.*;

class SLocation implements Serializable {

    private String world;
    private int x;
    private int y;
    private int z;

    public SLocation(Location locy) {
        world = locy.getWorld().getName();
        x = locy.getBlockX();
        y = locy.getBlockY();
        z = locy.getBlockZ();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SLocation)) {
            return false;
        }
        SLocation other = (SLocation) obj;
        if (!other.world.equals(world)) {
            return false;
        }
        if (other.x != x) {
            return false;
        }
        if (other.y != y) {
            return false;
        }
        return other.z == z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (world == null ? 0 : world.hashCode());
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return hash;
    }

    @Override
    public String toString(){
        return (x + ", " + y + ", " + z);
    }
}
