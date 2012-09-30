package com.ngc0202.tokenmarket;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ngc0202
 */
public class PlayerCommandTimer implements PluginSerializable<PlayerCommandTimer> {

    public static final long TICKS_PER_SECOND = 20L;
    private Collection<String> commands;
    private long end;
    private String playerName;
    private int taskID = -1;

    public PlayerCommandTimer(Collection<String> commands, String playerName, long end) {
        this.commands = commands;
        this.playerName = playerName;
        this.end = end;
    }

    public PlayerCommandTimer(Collection<String> commands, String playerName, long end, int taskID) {
        this.commands = commands;
        this.playerName = playerName;
        this.end = end;
        this.taskID = taskID;
    }

    public Collection<String> getCommands() {
        return commands;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getEnd() {
        return end;
    }

    public int getID() {
        return taskID;
    }

    public long setEnd(long end) {
        this.end = end;
        return this.end;
    }

    public void setID(int ID) {
        taskID = ID;
    }

    public long getRemainingTicks() {
        return (TICKS_PER_SECOND * (end - System.currentTimeMillis())) / 1000;
    }

    @Override
    public Factory<PlayerCommandTimer> getSerializationFactory() {
        return MyFactory.instance;
    }

    static class MyFactory implements Factory<PlayerCommandTimer> {
        static final MyFactory instance = new MyFactory();

        @Override
        public void serialize(PlayerCommandTimer o, DataOutput output, int version) throws IOException {
            output.writeLong(o.end);
            output.writeUTF(o.playerName);
            output.writeInt(o.commands.size());
            for (String s : o.commands) {
                output.writeUTF(s);
            }
        }

        @Override
        public PlayerCommandTimer deserialize(DataInput input, int version, TokenMarket plugin) throws IOException {
            long end = input.readLong();
            String playerName = input.readUTF();
            int size = input.readInt();
            Collection<String> cmds = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                cmds.add(input.readUTF());
            }
            return new PlayerCommandTimer(cmds, playerName, end);
        }
    }
}
