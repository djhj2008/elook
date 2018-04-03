package com.elook.udp.elook;

import com.elook.udp.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DataReportBuf extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(DataReportBuf.class);
    private final int BMP_START = TEMP_START+TEMP_LEN;
    private final int BMP_COUNT = 5;
    private final int SMALL_BMP_WIDTH = 12;
    private final int SMALL_BMP_HEIGHT = 18;
    private final int SMALL_BMP_SIZE = SMALL_BMP_WIDTH*SMALL_BMP_HEIGHT;

    public DataReportBuf(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg,int length) {
        String ret=null;
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int temp = parseTemp(msg);

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int id = dev.getDeviceAutoId();
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int upl = dev.getDeviceUplState();
        int led_type = dev.getDeviceLedType();
        int led_lev = dev.getDeviceLedLevel();
        int rep_type = dev.getDeviceRepType();
        int value = 0;
        String path = savePicS(BMP_COUNT,SMALL_BMP_WIDTH,SMALL_BMP_HEIGHT,msg,BMP_START);

        if(path!=null&&!path.isEmpty()) {
            String[] cmd = {".\\bin\\pic_decode_new.exe",path};
            ArrayList<String> result = SmallPicDecode(cmd);
            if(result!=null&&result.size()>0) {
                String str_num = result.get(0);
                value = preParseAccessValue(str_num);
            }
        }

        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String dateString = formatter.format(currentTime);

        saveEasyDev(id,batlev, EasyDeviceInfo.DEVSTATE_CONFIG_PASS);
        saveAccess(devid,value);

        if(upl == 0){
            ret = getResultStr(rep_type,false,delay,delay_sub,led_type,led_lev);
        }else{
            ret = getResultStr(rep_type,false,delay,delay_sub,led_type,led_lev);
        }
        return ret;
    }
}
