package com.elook.udp.elook;

import com.elook.udp.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class BmpValueConfBuf extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(BmpValueConfBuf.class);
    private final int BMP_START = BATLEVL_START+BATLEVL_LEN;
    private final int BMP_COUNT = NUM_COUNT;
    private final int SMALL_BMP_WIDTH = 12;
    private final int SMALL_BMP_HEIGHT = 18;
    private final int SMALL_BMP_SIZE = SMALL_BMP_WIDTH*SMALL_BMP_HEIGHT;

    public BmpValueConfBuf(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg,int length) {
        String ret=null;
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int tmp_value = dev.getDeviceTmpValue();
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

        int state;
        if(value == tmp_value){
            state = EasyDeviceInfo.DEVSTATE_CONFIG_PASS;
            saveAccess(devid,value);
            ret = getResultStr(true,delay,delay_sub);
        }else{
            state = EasyDeviceInfo.DEVSTATE_CONFIRM_FAIL;
            ret = getResultStr(false,delay,delay_sub);
        }
        saveEasyDev(devid,batlev,state);
        return ret;
    }

}
