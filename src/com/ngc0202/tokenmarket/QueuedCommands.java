package com.ngc0202.tokenmarket;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author ngc0202
 */
public class QueuedCommands implements PluginSerializable<QueuedCommands> {

    private final String player;
    private final Collection<String> commands;

    public QueuedCommands(String player, Collection<String> commands) {
        this.player = player;
        this.commands = commands;
    }

    public String getPlayer() {
        return player;
    }

    public Collection<String> getCommands() {
        return Collections.unmodifiableCollection(commands);
    }

    @Override
    public Factory<QueuedCommands> getSerializationFactory() {
        return MyFactory.instance;
    }

    static class MyFactory implements Factory<QueuedCommands> {
        static final MyFactory instance = new MyFactory();

        @Override
        public void serialize(QueuedCommands obj, DataOutput output, int version) throws IOException {
            output.writeUTF(obj.player);
            output.writeInt(obj.commands.size());
            for (String s : obj.commands) {
                output.writeUTF(s);
            }
        }

        @Override
        public QueuedCommands deserialize(DataInput input, int version, TokenMarket plugin) throws IOException {
            String p = input.readUTF();
            int size = input.readInt();
            ArrayList<String> commands = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                commands.add(input.readUTF());
            }
            return new QueuedCommands(p, commands);
        }
    }
}
