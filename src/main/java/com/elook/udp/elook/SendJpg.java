package com.elook.udp.elook;

import com.elook.udp.mod.EasyAccess;
import com.elook.udp.mod.EasyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SendJpg extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(SendJpg.class);
    private final int DEVICE_TYPE_NORMAL = 0;
    private final int DEVICE_TYPE_DIGIT = 1;
    private final int BMP_COUT = NUM_COUNT;
    private final int SMALL_PIC_WIDTH = 12;
    private final int SMALL_PIC_HEIGHT = 18;

    private final int TYPE_START = TEMP_START+TEMP_LEN;
    private final int TYPE_LEN = 1;
    private final int  LED_START = TYPE_START+TYPE_LEN;
    private final int LED_LEN = 1;
    private final int IMG_START = LED_START+LED_LEN;

    public SendJpg(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg,int length) {
        String ret=null;
        int devid = getDevid();
        int sn_c = parseDevId(msg);
        int batlev = parseBatLev(msg);
        int type = parseCharValue(msg[TYPE_START]&0xff);
        int led  = parseCharValue( msg[LED_START]&0xff);
        int temp = parseTemp(msg);
        int res_led;

        log.debug("batlev:"+batlev);
        log.debug("type:"+type);
        log.debug("led:"+led);

        EasyDevice dev = findEasyDev(devid);
        if(dev == null){
            return null;
        }
        int delay = dev.getDeviceUpDelay();
        int delay_sub = dev.getDeviceUpDelaySub();
        int upl_state = dev.getDeviceUplState();
        int id =dev.getDeviceAutoId();
        int rep_type = dev.getDeviceRepType();
        int tmp_value;
        log.debug("upl_state:"+upl_state);
        if(type == 0 ||type == 1||type == 3){
            String filename = saveJpgPic(msg,length,devid);
            File fd = new File(filename);
            String path =fd.getParent();
            if(filename!=null&&!filename.isEmpty()) {
                String[] cmd = {".\\bin\\pic_decode.exe",filename,"tmp.bmp"};
                ArrayList<String> result = SmallPicDecode(cmd);
                if(result!=null&&result.size()>0) {
                    String strconf;
                    int index = result.size();
                    log.debug("result size:"+index);
                    int deviceType = parseDeviceType(result.get(index-1));
                    if(deviceType == DEVICE_TYPE_DIGIT) {
                        byte[] bmpall = HexString2Byte(result.get(index - 3));
                        strconf = result.get(index-4);
                        savePicsPath(BMP_COUT, SMALL_PIC_WIDTH, SMALL_PIC_HEIGHT, bmpall, 0, path);
                        String[] cmd2 = {".\\bin\\pic_decode_new.exe", path};
                        ArrayList<String> result2 = SmallPicDecode(cmd2);
                        if (result2 != null) {
                            String str_num = result2.get(0);
                            log.debug("preParseValue:"+str_num);
                            tmp_value = preParseAccessValue(str_num);
                            log.debug("tmp_value:"+tmp_value);
                        } else {
                            //解析失败
                            String errfilename = saveErrJpgPic(msg,length,devid);
                            saveDevErrPic(id,batlev, EasyDeviceInfo.DEVSTATE_DIG_PARSE_FAIL,errfilename);
                            return getResultStr(rep_type,true,delay,delay_sub);
                        }
                    }else{
                        strconf = result.get(index-3);
                        String value_str = strconf.substring(strconf.length()-4);
                        tmp_value = Integer.valueOf(value_str);
                    }
                    String strled = strconf.substring(0,1);
                    res_led = Integer.valueOf(strled);
                    log.debug("res_led:"+res_led+" led"+led);
                    if(res_led!=led){
                        saveEasyDev(id,batlev,EasyDeviceInfo.DEVSTATE_DEV_LED_CORRECT);
                    }else {
                        int led_type = res_led;
                        int led_lev = Integer.valueOf(strconf.substring(1, 3));
                        saveDevFull(id, batlev, EasyDeviceInfo.DEVSTATE_DEV_CONFIG_MIS, filename, tmp_value, led_type, led_lev);
                    }
                    if(type==3){
                        saveAccess(devid,tmp_value,filename);
                        if(upl_state==3){
                            return getResultStrConfigLog(rep_type,delay,delay_sub,strconf);
                        }
                    }
                    return getResultStrConfig(rep_type,delay,delay_sub,strconf);
                }else{
                    //解析失败
                    String errfilename = saveErrJpgPic(msg,length, devid);
                    saveDevErrPic(id,batlev,EasyDeviceInfo.DEVSTATE_DIG_PARSE_FAIL,errfilename);
                    return getResultStr(rep_type,true,delay,delay_sub);
                }
            }
        }else if(type == 2){
            if(upl_state == 1){
                //upl_state = 0;
                //saveDevUplState(id,upl_state);
            }
            String filename = saveNormalJpgPic(msg,length,devid);
            EasyAccess ea = findTopAccess(devid);
            if(ea !=null) {
                saveAccessUrl(ea, filename);
            }
            return getResultStrLog(rep_type, upl_state, delay, delay_sub);
        }else{
            //Nothing TODO
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

    public String saveJpgPic(byte[] msg,int length,int devid) {
        String path = new File(NORMAL_PATH).getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        log.debug("path:"+path);
        return saveJpgPicPath(msg,length,devid,path);
    }

    public String saveNormalJpgPic(byte[] msg,int length,int devid) {
        String path = new File(NORMAL_PATH).getAbsolutePath();
        path += File.separator+devid+File.separator;
        log.debug("path:"+path);
        return saveJpgPicPath(msg,length,devid,path);
    }

    public String saveErrJpgPic(byte[] msg,int length,int devid) {
        String path = new File(ERROR_PATH).getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        log.debug("path:"+path);
        return saveJpgPicPath(msg,length,devid,path);
    }

    public String saveJpgPicPath(byte[] msg,int length,int devid,String path) {
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
            os.write(msg, IMG_START,length-IMG_START);

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
        log.debug(strType);
        if(strType.startsWith(prefix)) {
            int begin = prefix.length();
            int end = begin+1;
            String str = strType.substring(begin, end);
            ret = Integer.valueOf(str);
        }
        return ret;
    }
}
