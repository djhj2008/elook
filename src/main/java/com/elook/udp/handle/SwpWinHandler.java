package com.elook.udp.handle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.elook.udp.elook.ElookCmdUrl;
import com.elook.udp.init.SingleWinServer;
import com.elook.udp.init.StartupEvent;
import com.elook.udp.init.SwpWinServer;
import com.elook.udp.init.WinServerInterface;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwpWinHandler{
    private static final Logger log = LoggerFactory.getLogger(SwpWinHandler.class);
    final static boolean DEBUG = true;
    final static byte FLAG_HURT_VALID = 'b';
    final static byte FLAG_DATA_VALID = 'd';
    final static int ACK_TIMEOUT_DELAY = 30000;
    final static int FULL_TIMEOUT_DELAY = 30000;
    int Revcount = 0;
    SwpHdr hdr;
    Thread tm;
    Map<Integer,SwpWindows> win_map = new ConcurrentHashMap<>();
    Map<Integer,Long> thread_map = new ConcurrentHashMap<>();

    SwpWinListener mListener = new SwpWinListener() {
        @Override
        public void listener(int sn, String cmd) {
            if(DEBUG) {
                //log.debug("sn:" + sn + " cmd:" + cmd);
            }
            if(cmd.equals("DONE")) {
                SwpWindows mSwpWindow = win_map.get(sn);
                    thread_map.remove(sn);
                if (mSwpWindow != null) {
                    win_map.remove(sn);
                    mSwpWindow=null;
                    if(DEBUG) {
                        Revcount++;
                        log.debug("sn:" + sn + " Done:"+Revcount+" times.");
                    }
                }
            }else if(cmd.equals("ACK")){
                long time_t = System.currentTimeMillis()+ACK_TIMEOUT_DELAY;
                if(DEBUG) {
                    log.debug("Set ACK timeOut:" + time_t);
                }
                thread_map.put(sn, time_t);
                if(DEBUG) {
                    log.debug("Get ACK timeOut:" + thread_map.get(sn));
                }
            }else if(cmd.equals("FULL")){
                long time_t = System.currentTimeMillis()+FULL_TIMEOUT_DELAY;
                if(DEBUG) {
                    log.debug("Set FULL timeOut:" + time_t);
                }
                thread_map.put(sn, time_t);
                if(DEBUG) {
                    log.debug("Get FULL timeOut:" + thread_map.get(sn));
                }
            }
        }
    };

    public SwpWinHandler(){
        hdr = new SwpHdr();
        tm = new Thread(new SwpWinTimeoutTask());
        tm.start();
    }

    public int deliverSWP(ChannelHandlerContext ctx,DatagramPacket packet,byte[] frame){
        int ret=-1;
        hdr.Flags = (short)(frame[0] & 0xFF);
        if(hdr.Flags == FLAG_HURT_VALID){
            if(DEBUG) {
                log.debug("HURT BEAT!");
            }
            int sn = WinServerInterface.load_swp_sn(frame);
            SwpWindows mSwpWindow = win_map.get(sn);
            if(mSwpWindow!=null){
                mSwpWindow.resetSwpWin();
            }
        }
        else if(hdr.Flags==FLAG_DATA_VALID) {
            hdr.MaxNum = (short) (frame[1] & 0xFF);
            hdr.AckNum = (short) (frame[2] & 0xFF);
            if(hdr.MaxNum > 1){
                int sn = WinServerInterface.load_swp_sn(frame);
                int cmd = WinServerInterface.load_cmd(frame);
                if(DEBUG) {
                    log.debug("cmd:" + cmd + " size:" + win_map.size());
                }

                if(win_map.size()>100){
                    if(DEBUG) {
                        log.debug("WIN MAP FULL!");
                    }
                }

                SwpWindows mSwpWindow=win_map.get(sn);

                if(mSwpWindow==null) {
                    mSwpWindow = new SwpWindows(sn, hdr.MaxNum, hdr.AckNum, ElookCmdUrl.SENDJPG);
                    win_map.put(sn,mSwpWindow);
                }

                SwpWinServer mSwpWinServer = (SwpWinServer) StartupEvent.getBean(SwpWinServer.class);
                mSwpWinServer.run(ctx,packet,frame,mSwpWindow,mListener);

                //log.debug("state2:"+mSwpWindow.getState());
            }else{
                int sn = WinServerInterface.load_swp_sn(frame);
                SwpWindows mSwpWindow=win_map.get(sn);
                if(DEBUG) {
                    log.debug("mSwpWindow:" + mSwpWindow);
                }

                if(mSwpWindow!=null) {
                    win_map.remove(sn);
                    mSwpWindow=null;
                }else{

                }
                SingleWinServer mSingleWinServer = (SingleWinServer) StartupEvent.getBean(SingleWinServer.class);
                mSingleWinServer.run(ctx,packet,frame);
            }
        }
        return ret;
    }


    private class SwpHdr{
        short Flags;
        short MaxNum;
        short AckNum;

        public SwpHdr(){
            Flags  = 0;
            MaxNum = 0;
            AckNum = 0;
        }
    }

    public class SwpWinTimeoutTask implements Runnable{
        private boolean isCancel=false;
        public void CancelTask(){
            isCancel = true;
        }

        @Override
        public void run() {
            if (Thread.currentThread().isInterrupted() ) {
                if(DEBUG) {
                    log.debug("I has interputed");
                }
                return;
            }
            while(!isCancel) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long cur_time = System.currentTimeMillis();
                    //synchronized (thread_map) {
                        Iterator iter = thread_map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            int key = (Integer)entry.getKey();
                            if (DEBUG) {
                                //log.debug("Get :" + key);
                            }
                            long time_t = thread_map.get(key);
                            if (cur_time >= time_t) {
                                thread_map.remove(key);
                                SwpWindows mSwpWindow = win_map.remove(key);
                                if(mSwpWindow!=null){
                                    if (DEBUG) {
                                        log.debug("Fail rev:" + mSwpWindow.getNFE());
                                    }
                                    mSwpWindow = null;
                                }
                                if (DEBUG) {
                                    log.debug("remove :" + key + " at:" + time_t + " when:" + cur_time);
                                }
                            }
                        }
                    //}
            }
            if(DEBUG) {
                log.debug("SwpWinTimeoutTask!");
            }
        }
    }
}
