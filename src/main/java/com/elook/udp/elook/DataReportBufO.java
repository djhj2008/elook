package com.elook.udp.elook;

import com.elook.udp.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataReportBufO extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(DataReportBufO.class);
    private final int RADIUS_START = HEAD_END;
    private final int RADIUS_LEN  = 1;
    private final int WIDTH_START = RADIUS_START+RADIUS_LEN;
    private final int WIDTH_LEN   = 1;
    private final int HEIGHT_START = WIDTH_START+WIDTH_LEN;
    private final int HEIGHT_LEN  = 1;
    private final int ANGLE_START = HEIGHT_START+HEIGHT_LEN;
    private final int ANGLE_LEN   = 2;
    private final int BMP_START = ANGLE_START+ANGLE_LEN;
    private final int BMP_COUNT = NUM_COUNT;

    public DataReportBufO(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg,int length) {
        String ret="";
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int temp = parseTemp(msg);
        int signal = parseSignal(msg);

        int radius = msg[RADIUS_START]&0xff;
        int width = msg[WIDTH_START]&0xff;
        int height = msg[HEIGHT_START]&0Xff;
        short angle = Byte2Short(msg,ANGLE_START);
        int bmp_size = width*height;

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int id =dev.getDeviceAutoId();
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int upl = dev.getDeviceUplState();
        int led_type = dev.getDeviceLedType();
        int led_lev = dev.getDeviceLedLevel();
        int rep_type = dev.getDeviceRepType();
        int value = 0;

        String path = savePicS(BMP_COUNT,width,height,msg,BMP_START);
        if(path!=null&&!path.isEmpty()) {
            String[] cmd = {".\\bin\\pic_old_decode.exe",path,""+radius,""+width,""+height,""+angle};
            ArrayList<String> result = SmallPicDecode(cmd);
            if(result!=null&&result.size()>0) {
                String str_num = result.get(2);
                if(!str_num.isEmpty()){
                    value = preParseAccessValueOld(str_num);
                }
            }
        }
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(currentTime);

        saveEasyDev(id,batlev, EasyDeviceInfo.DEVSTATE_CONFIG_PASS,temp,signal);
        saveAccess(devid,value);

        ret = getResultStrLog(rep_type,upl,delay,delay_sub,led_type,led_lev);

        return ret;
    }
}
