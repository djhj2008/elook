package com.elook.udp.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 接受UDP消息，并保存至redis的list链表中
 * Created by wj on 2017/8/30.
 *
 */

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger log= LoggerFactory.getLogger(UdpServerHandler.class);

    SwpWinHandler mSwHandler = new SwpWinHandler();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

        String receiveMsg = packet.content().toString(CharsetUtil.UTF_8);

        //log.info("Received UDP Msg:" + receiveMsg);

        int len = packet.content().readableBytes();

        byte[] buf = new byte[len];

        packet.content().getBytes(0,buf);

//        for(int i=0;i<len;i++){
//            log.debug(i+":0x" + Integer.toHexString(buf[i]));
//        }

        if(buf.length<3){
            log.debug("recv:"+buf.length+"drop:"+receiveMsg);
        }
        mSwHandler.deliverSWP(ctx, packet, buf);

//        UdpRecord udpRecord = new UdpRecord();
//
//        //判断接受到的UDP消息是否正确（未实现）
//        if (StringUtils.isNotEmpty(receiveMsg) ){
//
//            //计算接收到的UDP消息的数量
//            count++;
//            udpRecord.setId(1);
//            udpRecord.setTime(getTime()); //获取UdpRepository对象，将接收UDP消息的日志保存至mysql中
//            udpRecord.setUdpMsg(receiveMsg);
//            UdpRepository udpRepository = (UdpRepository) StartupEvent.getBean(UdpRepository.class);
//            udpRepository.save(udpRecord);
//            List <UdpRecord> list = udpRepository.findAll();
//
//            for(int i=0;i<list.size();i++) {
//                log.debug(i+":" + list.get(i).toString());
//            }
//            //获取RedirRepository对象
//            //RedisRepository redisRepository = (RedisRepository) StartupEvent.getBean(RedisRepository.class);
//            //将获取到的UDP消息保存至redis的list列表中
//            //redisRepository.lpush("udp:msg", receiveMsg);
//            //redisRepository.setKey("UDPMsgNumber", String.valueOf(count));
//
//
//            //在这里可以返回一个UDP消息给对方，告知已接收到UDP消息，但考虑到这是UDP消息，此处可以注释掉
//            ctx.write(new DatagramPacket(
//                    Unpooled.copiedBuffer("QOTM: " + "Got UDP Message!" , CharsetUtil.UTF_8), packet.sender()));
//
//        }else{
//            log.error("Received Error UDP Messsage:" + receiveMsg);
//        }
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }

    public Timestamp getTime(){
        Date date = new Date();
        Timestamp time = new Timestamp(date.getTime());
        return time;
    }

}