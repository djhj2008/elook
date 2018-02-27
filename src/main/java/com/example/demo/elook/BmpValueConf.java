package com.example.demo.elook;

import com.example.demo.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmpValueConf extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(BmpValueConf.class);
    private int VALUE_START = BATLEVL_START+BATLEVL_LEN;

    public BmpValueConf(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg) {
        String ret="";
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int devid = getDevid();
        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return "OKE";
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int tmp_value = dev.getDeviceTmpValue();
        int value = Byte2Int(msg,VALUE_START);
        int id =dev.getDeviceAutoId();
        log.debug("value:"+value);
        log.debug("tmp_value:"+tmp_value);

        int state = 0;
        if(value == tmp_value){
            state = EasyDeviceInfo.DEVSTATE_CONFIG_PASS;
            saveAccess(devid,value);
            ret = getResultStr(true,delay,delay_sub);
        }else{
            state = EasyDeviceInfo.DEVSTATE_CONFIRM_FAIL;
            ret = getResultStr(false,delay,delay_sub);
        }
        saveEasyDev(id,batlev,state);
        return ret;
    }
}
