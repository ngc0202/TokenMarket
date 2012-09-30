package com.ngc0202.tokenmarket;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SLocation implements PluginSerializable<SLocation> {

    private String world;
    private int x;
    private int y;
    private int z;

    private SLocation() {
    }

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

    public boolean equalsBukkitLocation(Location loc) {
        return loc != null
                && (loc.getWorld() == null ? world == null : world.equals(loc.getWorld().getName()))
                && loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z;
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
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    @Override
    public Factory<SLocation> getSerializationFactory() {
        return MyFactory.instance;
    }

    static class MyFactory implements Factory<SLocation> {
        static final MyFactory instance = new MyFactory();

        @Override
        public void serialize(SLocation o, DataOutput output, int version) throws IOException {
            output.writeUTF(o.world);
            output.writeInt(o.x);
            output.writeInt(o.y);
            output.writeInt(o.z);
        }

        @Override
        public SLocation deserialize(DataInput input, int version, TokenMarket plugin) throws IOException {
            SLocation l = new SLocation();
            l.world = input.readUTF();
            l.x = input.readInt();
            l.y = input.readInt();
            l.z = input.readInt();
            return l;
        }
    }
}
