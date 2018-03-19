package com.elook.udp.init;

import com.elook.udp.handle.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * server服务器
 * Created by wj on 2017/8/30.
 */
@Component
public class UdpServer {

    private static final Logger log= LoggerFactory.getLogger(UdpServer.class);

//    private static final int PORT = Integer.parseInt(System.getProperty("port", "7686"));

    @Async("myTaskAsyncPool")
    public void run(int udpReceivePort) {

        EventLoopGroup group = new NioEventLoopGroup();
        log.info("Server start!  Udp Receive msg Port:" + udpReceivePort +" time:"+System.currentTimeMillis());
        //String str = "91878c8a817f7e807a76757f8b877d75716d81786a736e787f8e694c42376b717c716b757f634f4c5548345682736b737e444f677e7b2c2e487b6f746e5e676d727d5c3d2e78676a757c7672727482745e6954577279727879757a5bbd4b61545f58717569765691555567543e37636c5a6c4e5050655159332559665466512636574e54381b465a50575c1d1c5053573f0e2a474d475c1a12515c5b4507183c504352150f515b5641280a1331304e160c4c524d41280a133130150532464f4cc8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c86c7078757d857c86838f766875868e797c82829398907e52737c5d83716d728091ab956a84753a361c18223a4ca08e897a642f283f556038326c8a93918151457e908a7a5d33907d85835a658a85778b7a19875f7a5d5667828a957e87348f6b6f5d3c648882868667326a7764273f7d7c7f765e28627d596e23367a7f7d79641a4479646e2928727a72786d1327767a5e3b215d716c6c6d1619738750561b49707870691814667c4b6b1128535f594a182b6a69435a59001b221f191854855ec8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c870868376531c235b8d76a77d748368140400040621759c8e7a7e1e0c398779550814808b7780011f727a808f450c837b68823735857c778d7a0b5c6d647d6b2483968c7c8c0833826d7d6a0387778d7c961b258a657d4f00848393848212066f48763b0873837e847e200469703d3d006e797e7f822a05616c4d0350786c6b7a2f055674605a0c19616e746e20086287546621003a5959340119697148724e22000500001757856e356d715b210f14385f7e8d86295c72737b6970807e6f6875c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c86e928e550e070000072c8e916b806b02005e91691c0261915d793b03667d8e898106198b548057036e828e8a83180f904780720072858c89833400813f757a0069848686845100844567760054828286886a007e565d730438817e878a77005d6556720d197c79838675013f6b5071110375717d7e6c001f4769270f4e7478834c031d854366690311596a440c025379466273270709060006368681515a707a602c1d335e918a865a4c607a757580716a7c70815d426a7d6e75857264606375c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8577e8b8d8a8d8d8e9c95939a617c8087877b6a5f998e959464687482692b06095c648287705c8262071c49580f316878775a6f41104c6d7c3c0d246b7862641f14737d856c35194d7c626514117a777a7f660f29825062200d6e787d7b800f167d485b330758797a827f1b18775651420646796e80772a1978644948094584707575330f7348441d27686b797a360654623f45282d5d5c757734145c704239181339456879290650744c47311912036029121d596a4f5662656a6404102e4f64c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8";
        //byte[] mmp = HexString2Byte(str);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpServerHandler());

            b.bind(udpReceivePort).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public byte[] HexString2Byte(String buf){
        byte[] ret=null;
        log.debug(buf);
        log.debug("len:"+buf.length());
        if(buf.length()%2!=0){
            return null;
        }else{
            ret = new byte[buf.length()/2];
        }

        for(int i=0;i<buf.length()/2;i++){
            int begin = i*2;
            int end = begin+2;
            String substr= buf.substring(begin,end);
            int temp = Integer.decode("0x"+substr);
            ret[i] = (byte)(temp&0xff);
            //ret[i]=Byte.decode("0x"+substr);
        }
        return ret;
    }
}
