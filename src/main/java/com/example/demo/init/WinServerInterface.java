package com.example.demo.init;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WinServerInterface {
    private static final Logger log= LoggerFactory.getLogger(WinServerInterface.class);
    final static int HEAD_LEN        = 3 ;
    final static int SN_START        = HEAD_LEN ;
    final static int SN_LEN          = 4;
    final static int SN_END          = HEAD_LEN+SN_LEN;
    final static int CMD_START       = SN_END;
    final static int CMD_LEN         = 1 ;
    final static int CMD_END         = SN_END+CMD_LEN;
    final static int DATA_LEN_START  = CMD_END ;
    final static int DATA_LEN        = 2 ;
    final static int DSTART          = CMD_END + DATA_LEN;
    final static int CHECK_SUM_LEN   = 1;

    public boolean checkSum(byte[] frame,int offset,int len){
        boolean ret = false;
        int sum=0;
        int sum2 = frame[offset+len]&0xff;
        log.debug("sum2:"+Integer.toHexString(sum2));
        for(int i=0 ;i<len;i++){
            sum+=frame[offset+i]&0xff;
        }
        sum=sum&0xff;
        log.debug("sum:"+Integer.toHexString(sum));
        if(sum==sum2){
            ret = true;
        }
        return ret;
    }

    public int load_swp_sn(byte[] frame){
        int sn = 0;
        sn = frame[SN_START + 3 ] & 0xff;
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

    public int load_cmd(byte[] frame){
        int cmd = 0;
        cmd = frame[SN_END] & 0xff;
        log.debug("cmd:"+cmd);
        return cmd;
    }


    public short load_swp_data_len(byte[] frame){
        short len = 0;
        len = (short)(frame[DATA_LEN_START+1] & 0xff);
        len = (short)(len | (short)(frame[DATA_LEN_START] << 8));
        return len;
    }

    public void requestAck(ChannelHandlerContext ctx,DatagramPacket packet,short max ,short nfe,int sn,int cmd){
        byte[] ack = new byte[CMD_END];
        ack[0] = 'a';
        ack[1] = (byte)(max&0xff);
        ack[2] = (byte)(nfe&0xff);
        System.arraycopy(intToByte(sn),0,ack,SN_START,SN_LEN);
        ack[CMD_START] = (byte)(cmd&0xff);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ack,0,CMD_END), packet.sender()));
    }

    public void requestAckString(ChannelHandlerContext ctx, DatagramPacket packet, int sn, int cmd, String str){
        int len = str.getBytes().length;
        byte[] ack = new byte[DSTART+len+CHECK_SUM_LEN];
        ack[0] = 'd';
        ack[1] = 0x01;
        ack[2] = 0x01;
        System.arraycopy(intToByte(sn),0,ack,SN_START,SN_LEN);
        ack[CMD_START] = (byte)(cmd&0xff);
        byte[] len_buf;
        len_buf = shortToByte((short)len);
        System.arraycopy(len_buf,0,ack,DATA_LEN_START,DATA_LEN);
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

    public byte Str2CheckSum(String str){
        int sum=0;
        byte[] buf=str.getBytes();
        for(int i=0;i< buf.length;i++){
            sum+=buf[i];
        }
        return (byte)(sum&0xff);
    }
}
