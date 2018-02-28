package com.example.demo.elook;

import com.example.demo.init.StartupEvent;
import com.example.demo.mod.EasyAccess;
import com.example.demo.mod.EasyDevice;
import com.example.demo.repository.mysql.EasyAccessRepository;
import com.example.demo.repository.mysql.EasyDevRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataReportBufO extends DeviceUpdController{
    private static final Logger log= LoggerFactory.getLogger(DataReportBufO.class);
    private final int RADIUS_START = BATLEVL_START+BATLEVL_LEN;
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

        int radius = msg[RADIUS_START]&0xff;
        int width = msg[WIDTH_START]&0xff;
        int height = msg[HEIGHT_START]&0Xff;
        short angle = Byte2Short(msg,ANGLE_START);
        int bmp_size = width*height;

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int upl = dev.getDeviceUplState();
        int led_type = dev.getDeviceLedType();
        int led_lev = dev.getDeviceLedLevel();
        int value = 0;

        String path = savePicS(BMP_COUNT,width,height,msg,BMP_START);
        if(path!=null&&!path.isEmpty()) {
            String[] cmd = {".\\bin\\pic_old_decode.exe",path,""+radius,""+width,""+height,""+angle};
            ArrayList<String> result = SmallPicDecode(cmd);
            if(result!=null) {
                String str_num = result.get(0);
                if(!str_num.isEmpty()){
                    value = preParseAccessValue(str_num);
                }
            }
        }
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(currentTime);

        saveEasyDev(devid,batlev,EasyDeviceInfo.DEVSTATE_CONFIG_PASS);
        saveAccess(devid,value);

        if(upl == 0){
            ret = getResultStr(true,delay,delay_sub,led_type,led_lev);
        }else{
            ret = getResultStr(false,delay,delay_sub,led_type,led_lev);;
        }
        return ret;
    }
}
