package com.example.demo.elook;

import com.example.demo.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class SendJpg extends DeviceUpdController{
    private static final Logger log= LoggerFactory.getLogger(SendJpg.class);
    private final int DEVICE_TYPE_NORMAL = 0;
    private final int DEVICE_TYPE_DIGIT = 1;

    private final int TYPE_START = BATLEVL_START+BATLEVL_LEN;
    private final int TYPE_LEN = 1;
    private final int LED_START = TYPE_START+TYPE_LEN;
    private final int LED_LEN = 1;
    private final int IMG_START = LED_START+LED_LEN;

    public SendJpg(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg) {
        String ret=null;
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int type = msg[TYPE_START]&0xff;
        int led  = msg[LED_START]&0xff;

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int tmp_value = dev.getDeviceTmpValue();

        if(type == 0 ||type == 1){
            String path = saveJpgPic(msg,devid);
            if(path!=null&&!path.isEmpty()) {
                String[] cmd = {".\\bin\\pic_decode.exe",path,"tmp.bmp"};
                ArrayList<String> result = SmallPicDecode(cmd);
                if(result!=null) {
                    int index = result.size();
                    int deviceType = parseDeviceType(result.get(index-1));
                    if(deviceType == DEVICE_TYPE_DIGIT){

                    }

                    //$setdevstate=D('device')->where(array('device_device_id'=>$devid))->save(array('device_dev_state'=>9,));
                    //$index = count($res);
                    //$type = $res[$index-1];
                    //$type = substr($type,6,1);
                }
            }
        }
        return null;
    }

    public String getPicDir(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(currentTime);
        Random rand=new Random();
        int rdm=(int)(Math.random()*(9999-1000+1)+1000);
        dateString=dateString+"_"+rdm;
        return dateString;
    }

    public String getPicName(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(currentTime);
        Random rand=new Random();
        int rdm=(int)(Math.random()*(9999-1000+1)+1000);
        dateString=dateString+"_"+rdm;
        return dateString;
    }

    public String saveJpgPic(byte[] msg,int devid) {
        OutputStream os = null;
        String path = new File("").getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        File tempFile = new File(path);
        log.debug("path:"+path);
        try {
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            String fileName=tempFile.getPath() + File.separator+getPicName()+".jpg";
            log.debug("file:"+fileName);
            os = new FileOutputStream(fileName);
            os.write(msg, IMG_START,msg.length-IMG_START);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 完毕，关闭所有链接
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tempFile.getPath();

    }

    private int parseDeviceType(String strType){
        int ret = 0;
        String prefix = "iType=";
        if(strType.startsWith(prefix)) {
            String str = strType.substring(prefix.length(), 1);
            ret = Integer.valueOf(str);
        }
        return ret;
    }
}
