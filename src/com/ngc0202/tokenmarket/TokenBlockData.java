package com.ngc0202.tokenmarket;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ngc0202
 */
public class TokenBlockData implements PluginSerializable<TokenBlockData> {

    public static final long TICKS_PER_SECOND = 20L;
    private File startFiles;
    private File remFiles;
    private long time; //In seconds
    private long ttime; //in ticks (20 per sec)
    private int price;

    public TokenBlockData(File dataFolder, String startFileString, String remFileString, String timeString, String priceString) throws NumberFormatException {
        this.startFiles = new File(dataFolder, startFileString);
        this.remFiles = new File(dataFolder, remFileString);
        try {
            startFiles.createNewFile();
            remFiles.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        parseTime(timeString);
        ttime = time * TICKS_PER_SECOND;
        parsePrice(priceString);
    }

    public TokenBlockData(File startFiles, File remFiles, long time, long ttime, int price) {
        this.startFiles = startFiles;
        this.remFiles = remFiles;
        this.time = time;
        this.ttime = ttime;
        this.price = price;
    }

    private void parseTime(String timeString) {
        char cmod = Character.toLowerCase(timeString.charAt(timeString.length() - 1));
        time = Long.parseLong(timeString.substring(0, timeString.length() - 1));
        int imod;
        switch (cmod) {
            default:
                imod = 1;
                break;
            case 'm':
                imod = 60;
                break;
            case 'h':
                imod = 3600;
                break;
            case 'd':
                imod = 86400;
                break;
        }
        time *= imod;
    }

    private void parsePrice(String priceString) throws NumberFormatException {
        if (priceString.endsWith("T")) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }
        price = Integer.parseInt(priceString);
    }

    public BufferedReader getStartFileReader() {
        try {
            return new BufferedReader(new FileReader(startFiles));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TokenBlockData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public BufferedReader getRemoveFileReader() {
        try {
            return new BufferedReader(new FileReader(remFiles));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TokenBlockData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public long getTime() {
        return time;
    }

    public int getPrice() {
        return price;
    }

    public long getTickTime() {
        return ttime;
    }

    @Override
    public Factory<TokenBlockData> getSerializationFactory() {
        return MyFactory.instance;
    }

    static class MyFactory implements Factory<TokenBlockData> {
        static final MyFactory instance = new MyFactory();

        @Override
        public void serialize(TokenBlockData o, DataOutput output, int version) throws IOException {
            output.writeUTF(o.startFiles.getPath());
            output.writeUTF(o.remFiles.getPath());
            output.writeLong(o.time);
            output.writeLong(o.ttime);
            output.writeInt(o.price);
        }

        @Override
        public TokenBlockData deserialize(DataInput input, int version, TokenMarket plugin) throws IOException {
            File startFiles = new File(input.readUTF());
            File remFiles = new File(input.readUTF());
            long time = input.readLong();
            long ttime = input.readLong();
            int price = input.readInt();
            return new TokenBlockData(startFiles, remFiles, time, ttime, price);
        }
    }
}
