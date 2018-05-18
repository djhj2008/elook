package com.elook.udp.elook;

import com.elook.udp.mod.EasyDevice;

public class SaveBmp extends DeviceUpdController {
    private final int WIDTH_START = HEAD_END;
    private final int WIDTH_LEN   = 2;
    private final int HEIGHT_START = WIDTH_START+WIDTH_LEN;
    private final int HEIGHT_LEN  = 2;
    private final int BMP_START  = HEIGHT_START+HEIGHT_LEN;

    public SaveBmp(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg, int length) {
        String ret="";
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int temp = parseTemp(msg);
        int devid = getDevid();
        int signal = parseSignal(msg);

        int width = Byte2Short(msg,WIDTH_START);
        int height = Byte2Short(msg,HEIGHT_START);

        int bmp_size = width*height;

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int tmp_value = dev.getDeviceTmpValue();
        int rep_type = dev.getDeviceRepType();

        String path = savePicS(1,width,height,msg,BMP_START);

        ret = getResultStr(rep_type,true,delay,delay_sub);
        return ret;
    }
}
