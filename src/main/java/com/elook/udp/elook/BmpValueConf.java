package com.elook.udp.elook;

import com.elook.udp.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmpValueConf extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(BmpValueConf.class);
    private int VALUE_START = HEAD_END;

    public BmpValueConf(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg,int length) {
        String ret="";
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int temp = parseTemp(msg);
        int signal = parseSignal(msg);

        log.debug("batlev:"+batlev);

        int devid = getDevid();
        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return "OKE";
        }
        for(int i=0;i<16;i++){
            log.debug("msg["+i+"]"+msg[i]);
        }


        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int tmp_value = dev.getDeviceTmpValue();
        int rep_type = dev.getDeviceRepType();

        int id =dev.getDeviceAutoId();
        int value = Byte2Int(msg,VALUE_START);
        log.debug("value:"+value);
        log.debug("tmp_value:"+tmp_value);

        int state = 0;
        if(value == tmp_value){
            state = EasyDeviceInfo.DEVSTATE_CONFIG_PASS;
            saveAccess(devid,value);
            ret = getResultStr(rep_type,true,delay,delay_sub);
        }else{
            state = EasyDeviceInfo.DEVSTATE_CONFIRM_FAIL;
            ret = getResultStr(rep_type,false,delay,delay_sub);
        }
        saveEasyDev(id,batlev,state,temp,signal);
        return ret;
    }
}
