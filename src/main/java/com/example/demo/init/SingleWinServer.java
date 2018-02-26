package com.example.demo.init;

import com.example.demo.elook.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public class SingleWinServer {
    private static final Logger log= LoggerFactory.getLogger(SingleWinServer.class);
    final static int HEAD_LEN        = 3 ;
    final static int SN_START        = HEAD_LEN ;
    final static int SN_LEN          = 4;
    final static int SN_END          = HEAD_LEN+SN_LEN;
    final static int CMD_START       = SN_END;
    final static int CMD_LEN         = 1 ;
    final static int CMD_END         = SN_END+CMD_LEN;
    final static int DATA_LEN        = 2 ;
    final static int DSTART          = HEAD_LEN+SN_LEN+DATA_LEN ;
    final static int CHECK_SUM_LEN   = 1;

    short MaxNum;
    short AckNum;

    @Async("myTaskAsyncPool")
    public void run(ChannelHandlerContext ctx, DatagramPacket packet, byte[] frame) {
        int sn = load_swp_sn(frame);
        SingleWindow mSingleWindow = new SingleWindow(sn,ElookCmdUrl.GET_DEVSTATE);
        int len = load_swp_data_len(frame);
        if(len > 0) {
            if (mSingleWindow.checkSum(frame, DSTART, len) == true) {
                mSingleWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                log.debug("No Data.");
            }
        }
        int cmd = mSingleWindow.getCmd();
        byte[] msg = mSingleWindow.getSlot();
        String ret = null;
        if(cmd ==ElookCmdUrl.GET_DEVSTATE){
            int state = mSingleWindow.getDevState(sn);
            log.debug("state:"+state);
            if(state == 10){
                ret = "OKA";
            }else if(state < 10){
                ret = "OK"+state;
            }else{
                ret = "OKE";
            }
        }
        else if(cmd == ElookCmdUrl.BMP_VALUECONF){
            BmpValueConf bvc = new BmpValueConf(sn,cmd);
            ret = bvc.DeviceUpdCtrlHandle(msg);

        }
        else if(cmd == ElookCmdUrl.DATA_REPORT){
            DataReport dr = new DataReport(sn,cmd);
            ret = dr.DeviceUpdCtrlHandle(msg);

        }
        if(ret!=null&&!ret.isEmpty()){
            requestAckString(ctx, packet,sn,cmd,ret);
        }
    }

    private int load_swp_sn(byte[] frame){
        int sn = 0;
        sn = frame[SN_START] & 0xff;
        log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 1] & 0xff) << 8 & 0xffffffff);
        log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 2] & 0xff) << 16 & 0xffffffff);
        log.debug("sn:"+Integer.toHexString(sn));
        sn = sn | ((frame[SN_START + 3] & 0xff) << 24 & 0xffffffff);
        log.debug("sn:"+Integer.toHexString(sn));
        log.debug("sn:"+sn);
        return sn;
    }

    private short load_swp_data_len(byte[] frame){
        short len = 0;
        len = (short)(frame[SN_END] & 0xff);
        len = (short)(len | (short)(frame[SN_END + 1] << 8));
        return len;
    }

    private void requestAckString(ChannelHandlerContext ctx,DatagramPacket packet,int sn,int cmd,String str){
        int len = str.getBytes().length;
        byte[] ack = new byte[DSTART+len+CHECK_SUM_LEN];
        ack[0] = 'a';
        ack[1] = 0x01;
        ack[2] = 0x01;
        System.arraycopy(intToByte(sn),0,ack,SN_START,SN_LEN);
        ack[CMD_START] = (byte)(cmd&0xff);
        byte[] len_buf;
        len_buf = shortToByte((short)len);
        System.arraycopy(len_buf,0,ack,CMD_END,DATA_LEN);
        System.arraycopy(str.getBytes(),0,ack,DSTART,len);
        ack[DSTART+len] = Str2CheckSum(str);//sum;
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ack,0,DSTART+len+CHECK_SUM_LEN), packet.sender()));
        len_buf = null;
        ack = null;
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

    private byte Str2CheckSum(String str){
        int sum=0;
        byte[] buf=str.getBytes();
        for(int i=0;i<=buf.length;i++){
            sum+=buf[i];
        }
        return (byte)(sum&0xff);
    }
}
