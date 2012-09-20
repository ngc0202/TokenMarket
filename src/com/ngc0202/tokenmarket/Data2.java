package com.ngc0202.tokenmarket;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author ngc0202
 */
public class Data2 implements Serializable {

    private ArrayList<String> cmds;
    private long end;
    private String playerName;
    private int taskID;

    public Data2(ArrayList<String> cmds, String playerName, long end) {
        this.cmds = cmds;
        this.playerName = playerName;
        this.end = end;
    }

    public Data2(ArrayList<String> cmds, String playerName, long end, int taskID) {
        this.cmds = cmds;
        this.playerName = playerName;
        this.end = end;
        this.taskID = taskID;
    }

    public ArrayList<String> getCommands() {
        return cmds;
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
}
