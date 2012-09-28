package com.ngc0202.tokenmarket;

import java.util.ArrayList;

/**
 *
 * @author ngc0202
 */
public class Data3 {

    private final String ply;
    private final ArrayList<String> cmds;

    public Data3(String ply, ArrayList<String> cmds) {
        this.ply = ply;
        this.cmds = cmds;
    }

    public String getPly() {
        return ply;
    }

    public ArrayList<String> getCmds() {
        return cmds;
    }
}
