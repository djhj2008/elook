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
    private final int BMP_COUT = NUM_COUNT;
    private final int SMALL_PIC_WIDTH = 12;
    private final int SMALL_PIC_HEIGHT = 18;

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
            String filename = saveJpgPic(msg,devid);
            int path_index = filename.lastIndexOf(File.separator);
            String path =filename.substring(0,path_index);
            if(filename!=null&&!filename.isEmpty()) {
                String[] cmd = {".\\bin\\pic_decode.exe",filename,"tmp.bmp"};
                ArrayList<String> result = SmallPicDecode(cmd);
                int value = 0;
                if(result!=null) {
                    int index = result.size();
                    int deviceType = parseDeviceType(result.get(index-1));
                    if(deviceType == DEVICE_TYPE_DIGIT){
                        byte[] bmpall = HexString2Byte(result.get(index-3));
                        savePicsPath(BMP_COUT,SMALL_PIC_WIDTH,SMALL_PIC_HEIGHT,bmpall,0,path);
                    }
                    String[] cmd2 = {".\\bin\\pic_decode_new.exe",path};
                    ArrayList<String> result2 = SmallPicDecode(cmd);
                    if(result2!=null) {
                        String str_num = result.get(0);
                        value = preParseAccessValue(str_num);
                    }
                    else{
                        String errfilename = saveErrJpgPic(msg,devid);

                    }
                }else{
                    //解析失败
                }
            }
        }
        return null;
    }

    public String getPicName(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = formatter.format(currentTime);
        //Random rand=new Random();
        int rdm=(int)(Math.random()*(9999-1000+1)+1000);
        dateString=dateString+"_"+rdm;
        return dateString;
    }

    public String saveJpgPic(byte[] msg,int devid) {
        String path = new File("normalup").getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        log.debug("path:"+path);
        return saveJpgPicPath(msg,devid,path);
    }

    public String saveErrJpgPic(byte[] msg,int devid) {
        String path = new File("errorup").getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        log.debug("path:"+path);
        return saveJpgPicPath(msg,devid,path);
    }

    public String saveJpgPicPath(byte[] msg,int devid,String path) {
        OutputStream os = null;
        String fileName = null;
        File tempFile = new File(path);
        log.debug("path:"+path);
        try {
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            fileName=tempFile.getPath() + File.separator+getPicName()+".jpg";
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

        return fileName;

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
