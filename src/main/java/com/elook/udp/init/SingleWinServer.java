package com.elook.udp.init;

import com.elook.udp.elook.*;
import com.elook.udp.handle.SingleWindow;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SingleWinServer extends WinServerInterface{
    private static final Logger log= LoggerFactory.getLogger(SingleWinServer.class);
    public static final short SINGLE_WIN = 1;

    @Async("myTaskAsyncPool")
    public void run(ChannelHandlerContext ctx, DatagramPacket packet, byte[] frame) {
        int sn = load_swp_sn(frame);
        int cmd = load_cmd(frame);
        String ret = null;
        log.debug("SN:"+sn+" CMD:"+cmd);

        requestAck(ctx, packet, SINGLE_WIN, SINGLE_WIN,sn,cmd);

        SingleWindow mSingleWindow = new SingleWindow(sn,cmd);
        int len = load_swp_data_len(frame);
        if(len > 0) {
            if (checkSum(frame, DSTART, len) == true) {
                mSingleWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                log.debug("CheckSum Error.");
                ret = "OKE";
                requestAckString(ctx, packet,sn,cmd,ret);
                return;
            }
        }
        log.debug("len:"+len);
        byte[] msg = mSingleWindow.getSlot();

        if(cmd == ElookCmdUrl.GET_DEVSTATE){
            int state = mSingleWindow.getDevState(sn);
            int rep_type = mSingleWindow.getDevRepType(sn);
            log.debug("state:"+state);
            if(state == 10){
                ret = "OKA"+rep_type;
            }else if(state>=0 && state < 10){
                ret = "OK"+state+rep_type;
            }else{
                ret = "OKE";
            }
        }
        else if(cmd == ElookCmdUrl.BMP_VALUECONF){
            BmpValueConf bvc = new BmpValueConf(sn,cmd);
            ret = bvc.DeviceUpdCtrlHandle(msg,len);
        }
        else if(cmd == ElookCmdUrl.DATA_REPORT){
            DataReport dr = new DataReport(sn,cmd);
            ret = dr.DeviceUpdCtrlHandle(msg,len);
        }
        else if(cmd == ElookCmdUrl.LOGS||cmd == ElookCmdUrl.DATA_REPORT_ERROR_BUF_OLD){
            SaveLog sl = new SaveLog(sn,cmd);
            ret = sl.DeviceUpdCtrlHandle(frame,frame.length);
        }
        if(ret!=null&&!ret.isEmpty()){
            requestAckString(ctx, packet,sn,cmd,ret);
        }
    }

}
