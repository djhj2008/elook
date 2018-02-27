package com.example.demo.init;

import com.example.demo.elook.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SingleWinServer extends WinServerInterface{
    private static final Logger log= LoggerFactory.getLogger(SingleWinServer.class);

    @Async("myTaskAsyncPool")
    public void run(ChannelHandlerContext ctx, DatagramPacket packet, byte[] frame) {
        int sn = load_swp_sn(frame);
        SingleWindow mSingleWindow = new SingleWindow(sn,ElookCmdUrl.GET_DEVSTATE);
        int len = load_swp_data_len(frame);
        if(len > 0) {
            if (checkSum(frame, DSTART, len) == true) {
                mSingleWindow.saveSlotMsg(frame, DSTART, len);
            } else {
                log.debug("CheckSum Error.");
            }
        }
        int cmd = load_cmd(frame);
        byte[] msg = mSingleWindow.getSlot();
        String ret = null;
        log.debug("CMD:"+cmd);
        if(cmd ==ElookCmdUrl.GET_DEVSTATE){
            int state = mSingleWindow.getDevState(sn);
            log.debug("state:"+state);
            if(state == 10){
                ret = "OKA";
            }else if(state>=0 && state < 10){
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

}
