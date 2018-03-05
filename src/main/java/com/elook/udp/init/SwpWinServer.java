package com.elook.udp.init;

import com.elook.udp.elook.*;
import com.elook.udp.handle.SwpWinListener;
import com.elook.udp.handle.SwpWindows;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Component
public class SwpWinServer extends WinServerInterface {
    private static final Logger log= LoggerFactory.getLogger(SwpWinServer.class);
    final static boolean DEBUG = true;
    short MaxNum;
    short AckNum;
    SwpWinListener mListener;

    @Async("myTaskAsyncPool")
    public void run(ChannelHandlerContext ctx, DatagramPacket packet, byte[] frame, SwpWindows mSwpWindow, SwpWinListener mListener) {
        synchronized (this)
        {
            this.mListener=mListener;
            MaxNum = (short) (frame[1] & 0xFF);
            AckNum = (short) (frame[2] & 0xFF);
            int sn = load_swp_sn(frame);
            int cmd = load_cmd(frame);

            if(DEBUG) {
                log.debug("get ACK:" + AckNum);
            }
            log.debug("sn:"+sn+" ack:"+AckNum+" nfe:"+MaxNum);
            mListener.listener(sn,"ACK");

            mSwpWindow.reSwpWindows(sn, MaxNum, AckNum,cmd);

            int len = load_swp_data_len(frame);

            if(DEBUG) {
                log.debug("data len:" + len);
            }

            if (len == 0||len > 512-DSTART) {
                return;
            }

            if (mSwpWindow.swpInWindow() == false) {
                if(DEBUG) {
                    log.debug("Drop ACK:" + AckNum + " NFE:" + mSwpWindow.getNFE());
                }
                short save_ack = 0;
                if (mSwpWindow.getNFE() > 0){
                    save_ack = (short)(mSwpWindow.getNFE()-1);
                }else{
                    save_ack = 0xff;
                }
                if(mSwpWindow.swpWinDrop()) {
                    if(DEBUG) {
                        log.debug("Send ACK:" + save_ack);
                    }
                    requestAck(ctx, packet, mSwpWindow.getSeqNum(), save_ack,sn,cmd);
                }
                return;
            }

            if (mSwpWindow.checkSum(frame, DSTART, len) == true) {
                mSwpWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                //if(DEBUG) {
                    log.debug("checkSum error!");
                //}
            }
            //requestAckString(ctx,packet,"test:"+AckNum);
            if (mSwpWindow.swpInWindowFull() == true) {
                if(DEBUG) {
                    log.debug("WindowFull.");
                }
                short nfe = mSwpWindow.fillMsg();
                short save_ack = 0;
                if (nfe > 0){
                    save_ack = (short)(nfe-1);
                }else{
                    save_ack = 0xff;
                }
                requestAck(ctx, packet, mSwpWindow.getSeqNum(), save_ack,sn,cmd);
                if(DEBUG) {
                    log.debug("nfe:" + nfe);
                }
                if (mSwpWindow.RevDone()) {
                    byte[] msg = mSwpWindow.getMsg();
                    int length = mSwpWindow.getMsg_cursor();
                    String ret = null;
                    //if(DEBUG) {
                        log.debug("sn:"+sn+" RevDone length:" + length);
                    //}
                    if (cmd == ElookCmdUrl.SENDJPG) {
                        SendJpg sendjpg = new SendJpg(sn,cmd);
                        ret = sendjpg.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF_BUF){
                        BmpValueConfBuf bvcf = new BmpValueConfBuf(sn,cmd);
                        ret = bvcf.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF_BUF_OLD){
                        BmpValueConfBufO bvcfo = new BmpValueConfBufO(sn,cmd);
                        ret = bvcfo.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.BMP_VALUECONF){
                        BmpValueConf bvc = new BmpValueConf(sn,cmd);
                        ret = bvc.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT_BUF){
                        DataReportBuf drb = new DataReportBuf(sn,cmd);
                        ret = drb.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT_BUF_OLD){
                        DataReportBufO drbo = new DataReportBufO(sn,cmd);
                        ret = drbo.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.DATA_REPORT){
                        DataReport dr = new DataReport(sn,cmd);
                        ret = dr.DeviceUpdCtrlHandle(msg,length);

                    }
                    else if(cmd == ElookCmdUrl.LOGS||cmd == ElookCmdUrl.DATA_REPORT_ERROR_BUF_OLD){
                        SaveLog sl = new SaveLog(sn,cmd);
                        ret = sl.DeviceUpdCtrlHandle(msg,length);
                    }
                    if(ret!=null&&!ret.isEmpty()){
                        requestAckString(ctx, packet,sn,cmd,ret);
                    }
                    mListener.listener(sn,"DONE");
                    return;
                }
                mListener.listener(sn,"FULL");
                return;
            }

        }
    }

}

