package com.elook.udp.handle;

public abstract class SwpWinListener {
    private int sn;
    private int cmd;
    abstract public void listener(int sn,String cmd);
}