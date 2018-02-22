package com.example.demo.elook;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class SwpWinTask implements Runnable{
    private static final Logger log= LoggerFactory.getLogger(SwpWinTask.class);
    final static int FLAG_ACK_VALID  = 'a';
    final static int FLAG_HURT_VALID = 'b';
    final static int FLAG_DATA_VALID = 'd';
    final static int HEAD_LEN        = 3 ;
    final static int SN_START        = HEAD_LEN ;
    final static int SN_LEN          = 4 ;
    final static int SN_END          = HEAD_LEN+SN_LEN;
    final static int DATA_LEN        = 2 ;
    final static int DSTART          = HEAD_LEN+SN_LEN+DATA_LEN ;
    final static int CHECK_SUM_LEN   = 1;

    short MaxNum;
    short AckNum;

    ChannelHandlerContext ctx;
    DatagramPacket packet;
    byte[] frame;
    SwpWindows mSwpWindow;


    public SwpWinTask(ChannelHandlerContext ctx,DatagramPacket packet,byte[] frame,HashMap<Integer,SwpWindows> win_map){
        this.ctx=ctx;
        this.packet=packet;
        this.frame = frame;
        this.mSwpWindow=mSwpWindow;
    }

    @Override
    public void run() {
        synchronized(this) {
            MaxNum = (short) (frame[1] & 0xFF);
            AckNum = (short) (frame[2] & 0xFF);
            int sn = load_swp_sn(frame);
            log.debug("AckNum:" + AckNum);

            mSwpWindow.reSwpWindows(sn, MaxNum, AckNum, ElookCmdUrl.SENDJPG);

            int len = load_swp_data_len(frame);
            if (len == 0) {
                return;
            }
            if (mSwpWindow.swpInWindow() == false) {
                return;
            }
            if (mSwpWindow.checkSum(frame, DSTART, len) == true) {
                mSwpWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                log.debug("checkSum error!");
            }

            if (mSwpWindow.swpInWindowFull() == true) {
                log.debug("WindowFull.");
                short nfe = mSwpWindow.fillMsg();
                log.debug("nfe:" + nfe);
                if (mSwpWindow.RevDone()) {
                    if (mSwpWindow.getCmd() == ElookCmdUrl.SENDJPG) {
                        mSwpWindow.savePic();
                    }
                }
                requestAck(ctx, packet, mSwpWindow.getSeqNum(), nfe);
            }
        }
    }


    private void requestAck(ChannelHandlerContext ctx,DatagramPacket packet,short max ,short nfe){
        byte[] ack = new byte[3];
        ack[0] = 'a';
        ack[1] = (byte)(max&0xff);
        ack[2] = (byte)(nfe&0xff);
        ctx.write(new DatagramPacket(Unpooled.copiedBuffer(ack,0,3), packet.sender()));
    }

    private void requestAckString(ChannelHandlerContext ctx,DatagramPacket packet,String str){
        int len = str.getBytes().length;
        byte[] ack = new byte[DSTART+len+CHECK_SUM_LEN];
        ack[0] = 'a';
        ack[1] = 0x01;
        ack[2] = 0x01;
        ack[SN_START] = 0x01;
        ack[SN_START+1] = 0x01;;
        ack[SN_START+2] = 0x01;;
        ack[SN_START+3] = 0x01;;
        ack[SN_END] = 0x01;;
        ack[SN_END+1] = (byte)(len&0xff);
        System.arraycopy(str.getBytes(),0,ack,DSTART,len);
        ctx.write(new DatagramPacket(Unpooled.copiedBuffer(ack,0,DSTART+len+CHECK_SUM_LEN), packet.sender()));
    }

    private int load_swp_sn(byte[] frame){
        int sn = 0;
        sn = frame[SN_START] & 0xff;
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 1] & 0xff) << 8 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 2] & 0xff) << 16 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 3] & 0xff) << 24 & 0xffffffff);
        //log.debug("sn:"+Integer.toHexString(sn));
        log.debug("sn:"+sn);
        return sn;
    }

    private short load_swp_data_len(byte[] frame){
        short len = 0;
        len = (short)(frame[SN_END] & 0xff);
        len = (short)(len | (short)(frame[SN_END + 1] << 8));
        return len;
    }

    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8; // 向右移8位
            //log.debug("buf:"+Integer.toHexString(b[i]));
        }
        return b;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8; // 向右移8位
            //log.debug("buf:"+Integer.toHexString(b[i]));
        }
        return b;
    }
}
