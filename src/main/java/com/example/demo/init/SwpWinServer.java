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
public class SwpWinServer extends  WinServerInterface{
    private static final Logger log= LoggerFactory.getLogger(SwpWinServer.class);
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

}

