package com.example.demo.init;

import com.example.demo.elook.*;
import com.example.demo.handle.SwpWinListener;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static java.lang.Thread.sleep;

@Component
public class SwpWinServer{
    private static final Logger log= LoggerFactory.getLogger(SwpWinServer.class);
    final static int FLAG_ACK_VALID  = 'a';
    final static int FLAG_HURT_VALID = 'b';
    final static int FLAG_DATA_VALID = 'd';
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
    int sn;
    short MaxNum;
    short AckNum;
    SwpWinListener mListener;

    @Async("myTaskAsyncPool")
    public void run(ChannelHandlerContext ctx, DatagramPacket packet, byte[] frame,SwpWindows mSwpWindow,SwpWinListener mListener) {
        synchronized (this)
        {
            this.mListener=mListener;
            MaxNum = (short) (frame[1] & 0xFF);
            AckNum = (short) (frame[2] & 0xFF);
            int sn = load_swp_sn(frame);
            this.sn=sn;

            //SwpWindows mSwpWindow=win_map.get(sn);

            mSwpWindow.reSwpWindows(sn, MaxNum, AckNum, ElookCmdUrl.SENDJPG);

            int len = load_swp_data_len(frame);

            log.debug("data len:"+len);

            if (len == 0||len > 512-DSTART) {
                return;
            }

            if (mSwpWindow.swpInWindow() == false) {
                log.debug("Drop ACK:"+AckNum+" NFE:"+mSwpWindow.getNFE());
                short save_ack = 0;
                if (mSwpWindow.getNFE() > 0){
                    save_ack = (short)(mSwpWindow.getNFE()-1);
                }else{
                    save_ack = 0xff;
                }
                if(mSwpWindow.swpWinDrop()) {
                    log.debug("Send ACK:" + save_ack);
                    requestAck(ctx, packet, mSwpWindow.getSeqNum(), save_ack);
                }
                return;
            }

            if (mSwpWindow.checkSum(frame, DSTART, len) == true) {
                mSwpWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                log.debug("checkSum error!");
            }
            //requestAckString(ctx,packet,"test:"+AckNum);
            if (mSwpWindow.swpInWindowFull() == true) {
                log.debug("WindowFull.");
                short nfe = mSwpWindow.fillMsg();
                short save_ack = 0;
                if (nfe > 0){
                    save_ack = (short)(nfe-1);
                }else{
                    save_ack = 0xff;
                }
                requestAck(ctx, packet, mSwpWindow.getSeqNum(), save_ack);
                log.debug("nfe:" + nfe);
                if (mSwpWindow.RevDone()) {
                    int cmd = mSwpWindow.getCmd();
                    byte[] msg = mSwpWindow.getMsg();
                    String ret = null;
                    if (cmd == ElookCmdUrl.SENDJPG) {
                        SendJpg sendjpg = new SendJpg(sn,cmd);
                        ret = sendjpg.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF_BUF){
                        BmpValueConfBuf bvcf = new BmpValueConfBuf(sn,cmd);
                        ret = bvcf.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF_BUF_OLD){
                        BmpValueConfBufO bvcfo = new BmpValueConfBufO(sn,cmd);
                        ret = bvcfo.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF){
                        BmpValueConf bvc = new BmpValueConf(sn,cmd);
                        ret = bvc.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT_BUF){
                        DataReportBuf drb = new DataReportBuf(sn,cmd);
                        ret = drb.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT_BUF_OLD){
                        DataReportBufO drbo = new DataReportBufO(sn,cmd);
                        ret = drbo.DeviceUpdCtrlHandle(msg);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT){
                        DataReport dr = new DataReport(sn,cmd);
                        ret = dr.DeviceUpdCtrlHandle(msg);

                    }
                    if(ret!=null&&!ret.isEmpty()){
                        requestAckString(ctx, packet,sn,cmd,ret);
                    }
                }
                mListener.listener(sn,"FULL");
                return;
            }
            mListener.listener(sn,"ACK");
        }
    }


    private void requestAck(ChannelHandlerContext ctx,DatagramPacket packet,short max ,short nfe){
        byte[] ack = new byte[3];
        ack[0] = 'a';
        ack[1] = (byte)(max&0xff);
        ack[2] = (byte)(nfe&0xff);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ack,0,3), packet.sender()));
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

    private byte Str2CheckSum(String str){
        int sum=0;
        byte[] buf=str.getBytes();
        for(int i=0;i<=buf.length;i++){
            sum+=buf[i];
        }
        return (byte)(sum&0xff);
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

