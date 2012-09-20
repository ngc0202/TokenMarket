package com.ngc0202.tokenmarket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ngc0202
 */
public class Data implements Serializable {

    private File startFiles;
    private File remFiles;
    private long time; //In seconds
    private long ttime; //in ticks (20 per sec)
    private int price;

    public Data(String startFileString, String remFileString, String timeString, String priceString) {
        this.startFiles = new File(TokenMarket.plugin.getDataFolder(), startFileString);
        this.remFiles = new File(TokenMarket.plugin.getDataFolder(), remFileString);
        try {
            startFiles.createNewFile();
            remFiles.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        parseTime(timeString);
        ttime = time * 20L;
        parsePrice(priceString);
    }

    private void parseTime(String timeString) {
        char cmod = timeString.charAt(timeString.length() - 1);
        time = Long.parseLong(timeString.substring(0, timeString.length() - 1));
        int imod = 1;
        if (cmod == 'm' || cmod == 'M') {
            imod = 60;
        }
        if (cmod == 'h' || cmod == 'H') {
            imod = 3600;
        }
        if (cmod == 'd' || cmod == 'D') {
            imod = 86400;
        }
        time *= imod;
    }

    private void parsePrice(String priceString) {
        if (priceString.endsWith("T")) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }
        price = Integer.parseInt(priceString);
    }

    public File getStartFile() {
        return startFiles;
    }

    public File getRemoveFile() {
        return remFiles;
    }

    public BufferedReader getStartFileReader() {
        try {
            return new BufferedReader(new FileReader(startFiles));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public BufferedReader getRemoveFileReader() {
        try {
            return new BufferedReader(new FileReader(remFiles));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
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
}
