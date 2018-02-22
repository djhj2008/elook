package com.example.demo.init;

import com.example.demo.elook.ElookCmdUrl;
import com.example.demo.elook.SingleWindow;
import com.example.demo.elook.SwpWindows;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public class SingleWinServer {
    private static final Logger log= LoggerFactory.getLogger(SingleWinServer.class);
    final static int FLAG_ACK_VALID  = 'a';
    final static int FLAG_HURT_VALID = 'b';
    final static int FLAG_DATA_VALID = 'd';
    final static int HEAD_LEN        = 3 ;
    final static int SN_START        = HEAD_LEN ;
    final static int SN_LEN          = 4 ;
    final static int SN_END          = HEAD_LEN+SN_LEN;
    final static int CMD_LEN         = 1 ;
    final static int CMD_END         = SN_END+CMD_LEN;
    final static int DATA_LEN        = 2 ;
    final static int DSTART          = CMD_END+DATA_LEN ;
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
                log.debug("checkSum error!");
            }
        }
        if(mSingleWindow.getCmd()==ElookCmdUrl.GET_DEVSTATE){
            int state = mSingleWindow.getDevState(sn);
            log.debug("state:"+state);
            requestAckString(ctx,packet,"OK"+state);
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
}
