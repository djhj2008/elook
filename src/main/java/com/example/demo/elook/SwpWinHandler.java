package com.example.demo.elook;

import java.beans.EventHandler;
import java.util.*;

import com.example.demo.handle.SwpWinListener;
import com.example.demo.init.SingleWinServer;
import com.example.demo.init.StartupEvent;
import com.example.demo.init.SwpWinServer;
import com.example.demo.mod.EasyDevice;
import com.example.demo.repository.mysql.EasyDevRepository;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import static java.lang.Thread.sleep;

public class SwpWinHandler{
    private static final Logger log = LoggerFactory.getLogger(SwpWinHandler.class);
    final static byte FLAG_ACK_VALID  = 'a';
    final static byte FLAG_HURT_VALID = 'b';
    final static byte FLAG_DATA_VALID = 'd';
    final static int HEAD_LEN        = 3 ;
    final static int SN_START        = HEAD_LEN ;
    final static int SN_PRE_LEN      = 4 ;
    final static int CMD_LEN         = 1 ;
    final static int SN_LEN          = SN_PRE_LEN + CMD_LEN ;
    final static int SN_END          = HEAD_LEN+SN_PRE_LEN;
    final static int DATA_LEN        = 2 ;
    final static int DSTART          = HEAD_LEN+SN_LEN+DATA_LEN ;
    final static int CHECK_SUM_LEN   = 1;

    final static int ACK_TIMEOUT_DELAY = 1000;

    SwpHdr hdr;
    HashMap<Integer,SwpWindows> win_map = new HashMap<>();
    HashMap<Long,Thread> thread_map = new HashMap<>();

    SwpWinListener mListener = new SwpWinListener() {
        @Override
        public void listener(int sn, String cmd) {
            log.debug("sn:"+sn+" cmd:"+cmd);
            if(cmd.equals("DONE")) {
                SwpWindows mSwpWindow = win_map.get(sn);
                if (mSwpWindow != null) {
                    win_map.remove(sn);
                    mSwpWindow=null;
                    log.debug("sn:" + sn + " Timeout!");
                }
            }else if(cmd.equals("ACK")){
                Thread timeoutTask =  new Thread(new SwpWinTimeoutTask(sn,5));
                long time_t = System.currentTimeMillis()+ACK_TIMEOUT_DELAY;
                log.debug("ACK timeOut:"+time_t);
                thread_map.put(time_t,timeoutTask);
                //timeoutTask.start();
            }
        }
    };


    public SwpWinHandler(){
        hdr = new SwpHdr();
    }

    public int deliverSWP(ChannelHandlerContext ctx,DatagramPacket packet,byte[] frame){
        int ret=-1;
        hdr.Flags = (short)(frame[0] & 0xFF);
        if(hdr.Flags == FLAG_HURT_VALID){
            log.debug("HURT BEAT!");
            int sn = load_swp_sn(frame);
            SwpWindows mSwpWindow = win_map.get(sn);
            if(mSwpWindow!=null){
                mSwpWindow.resetSwpWin();
            }
        }
        else if(hdr.Flags==FLAG_DATA_VALID) {
            hdr.MaxNum = (short) (frame[1] & 0xFF);
            hdr.AckNum = (short) (frame[2] & 0xFF);
            if(hdr.MaxNum > 1){
                int sn = load_swp_sn(frame);
                int cmd = load_cmd(frame);
                log.debug("cmd:"+cmd+" size:"+win_map.size());

                if(win_map.size()>100){
                    log.debug("WIN MAP FULL!");
                }

                SwpWindows mSwpWindow=win_map.get(sn);
                //log.debug("mSwpWindow:"+mSwpWindow);

                if(mSwpWindow==null) {
                    mSwpWindow = new SwpWindows(sn, hdr.MaxNum, hdr.AckNum,ElookCmdUrl.SENDJPG);
                    win_map.put(sn,mSwpWindow);
                }
                //log.debug("mSwpWindow:"+mSwpWindow);

                //log.debug("state:"+mSwpWindow.getState());

                SwpWinServer mSwpWinServer = (SwpWinServer) StartupEvent.getBean(SwpWinServer.class);
                mSwpWinServer.run(ctx,packet,frame,mSwpWindow,mListener);

                //log.debug("state2:"+mSwpWindow.getState());
            }else{
                int sn = load_swp_sn(frame);
                SwpWindows mSwpWindow=win_map.get(sn);
                log.debug("mSwpWindow:"+mSwpWindow);

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


    private int load_swp_sn(byte[] frame){
        int sn = 0;
        sn = frame[SN_START+3] & 0xff;
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 2] & 0xff) << 8 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 1] & 0xff) << 16 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START] & 0xff) << 24 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        log.debug("sn:"+sn);
        return sn;
    }

    private int load_cmd(byte[] frame){
        int cmd = 0;
        cmd = frame[SN_END] & 0xff;
        log.debug("cmd:"+cmd);
        return cmd;
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
        private int sn;
        private int time;

        public SwpWinTimeoutTask(int sn,int time){
            this.sn=sn;
            this.time=time;
        }

        @Override
        public void run() {

            if (Thread.currentThread().isInterrupted() ) {
                System.out.println("I has interputed");
                return;
            }
            log.debug("SwpWinTimeoutTask!");
        }
    }

    @Scheduled(fixedRate = 1000 )
    public void run() {
        //执行代码
        long time_t = System.currentTimeMillis();
        log.debug("Test time:"+time_t);
    }
}
