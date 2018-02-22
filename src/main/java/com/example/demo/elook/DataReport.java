package com.example.demo.elook;

import com.example.demo.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReport extends DeviceUpdController{
    private static final Logger log= LoggerFactory.getLogger(DataReport.class);
    private int COUNT_START = BATLEVL_START+BATLEVL_LEN;
    private int COUNT_LEN = 1;
    private int VALUE_START = COUNT_START+COUNT_LEN;
    private int VALUE_LEN = 4;
    private int TIME_LEN = 12;
    private int STRUCT_LEN = TIME_LEN+VALUE_LEN;

    public DataReport(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg) {
        String ret="";
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int upl = dev.getDeviceUplState();
        int led_type = dev.getDeviceLedType();
        int led_lev = dev.getDeviceLedLevel();

        int count = msg[COUNT_START];
        for(int i=0;i<count;i++){
            int time;
            if(i==count-1){
                time = (int)(System.currentTimeMillis()/1000);
            }else{
                byte[] str_buf = new byte[TIME_LEN];
                System.arraycopy(msg,VALUE_START,str_buf,0,TIME_LEN);
                String str = str_buf.toString();
                time = formatDate(str,"yyyyMMddHHmm");
            }
            int value = Byte2Int(msg,VALUE_START+count*(STRUCT_LEN)+TIME_LEN);
            saveAccess(devid,value,time);
        }

        saveEasyDev(devid,batlev,EasyDeviceInfo.DEVSTATE_CONFIG_PASS);

        if(upl == 0){
            ret = getResultStr(true,delay,delay_sub,led_type,led_lev);
        }else{
            ret = getResultStr(false,delay,delay_sub,led_type,led_lev);
        }
        return ret;
    }
}
