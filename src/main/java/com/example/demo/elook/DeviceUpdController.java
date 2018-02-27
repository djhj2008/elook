package com.example.demo.elook;

import com.example.demo.init.StartupEvent;
import com.example.demo.mod.EasyAccess;
import com.example.demo.mod.EasyDevice;
import com.example.demo.repository.mysql.EasyAccessRepository;
import com.example.demo.repository.mysql.EasyDevRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaSystemException;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public abstract class DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(DeviceUpdController.class);
    private int devid;
    private int cmd;
    private String PASS_STR = "OK1";
    private String FAIL_STR = "OK2";
    private String PASS2_STR = "OK0";

    private final int DATA_START  = 0;
    private final int SN_LEN      = 9;
    public final int BATLEVL_START = DATA_START+SN_LEN;
    public final int BATLEVL_LEN = 1;
    public final int NUM_COUNT = 5;


    public DeviceUpdController(int sn,int cmd){
        this.devid = sn;
        this.cmd = cmd;
    }

    abstract public String DeviceUpdCtrlHandle(byte[] msg);

    public int getDevid() {
        return devid;
    }

    public int getCmd() {
        return cmd;
    }

    public short Byte2Short(byte[] buf,int offset) {
        short num;
        num=(short)(buf[offset]<<8&buf[offset+1]);
        return num;
    }

    public int Byte2Int(byte[] buf, int offset) {
        int num;
        num=buf[offset]<<24&buf[offset+1]<<16&buf[offset+2]<<8&buf[offset+3];
        return num;
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

    public String savePicS(int count, int width, int height, byte[] bmpall, int offset) {
        String path = new File("normalup").getAbsolutePath();
        path += File.separator + devid + File.separator + getPicDir();
        log.debug("path:" + path);
        return savePicsPath(count,width,height,bmpall,offset,path);
    }

    public String savePicsPath(int count, int width, int height, byte[] bmpall, int offset, String path) {
        File tempFile = new File(path);
        log.debug("path:" + path);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        for(int i=0;i<count;i++) {
            OutputStream os = null;
            try {
                String fileName = i + ".bmp";
                log.debug("file:" + fileName);
                os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
                BMPWriter bmpwrier = new BMPWriter(width,height);
                bmpwrier.savebmpTop(os);
                bmpwrier.savebmpInfo(os);
                bmpwrier.savebmpDate(os,bmpall,offset+i*width*height,width*height);
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 完毕，关闭所有链接
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile.getPath();
    }

    public void saveEasyDev(int id,int batlev,int state){
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        EasyDevice devstate = new EasyDevice();
        devstate.setDeviceAutoId(id);
        devstate.setDeviceDevStateSub(0);
        devstate.setDeviceBetteryLev(batlev);
        devstate.setDeviceDevState(state);
        easyDevRepository.updateStatusById(id,state,batlev);
    }

    public void saveDevUplState(int devid,int state){
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        EasyDevice devstate = new EasyDevice();
        devstate.setDeviceDeviceId(devid);
        devstate.setDeviceUplState(state);
        easyDevRepository.save(devstate);
    }

    public void saveDevErrPic(int devid,int batlev,int state,String path){
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        EasyDevice devstate = new EasyDevice();
        devstate.setDeviceDeviceId(devid);
        devstate.setDeviceDevStateSub(0);
        devstate.setDeviceBetteryLev(batlev);
        devstate.setDeviceDevState(state);
        devstate.setDeviceDevUrlErrpic(path);
        easyDevRepository.save(devstate);
    }

    public void saveDevFull(int devid,int batlev,int state,String path,int tmp_value,int led_type,int led_lev){
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        EasyDevice devstate = new EasyDevice();
        devstate.setDeviceDeviceId(devid);
        devstate.setDeviceDevStateSub(0);
        devstate.setDeviceBetteryLev(batlev);
        devstate.setDeviceDevState(state);
        devstate.setDeviceDevUrlPic(path);
        devstate.setDeviceTmpValue(tmp_value);
        devstate.setDeviceLedType(led_type);
        devstate.setDeviceLedLevel(led_lev);
        easyDevRepository.save(devstate);
    }

    public void saveAccess(int devid,int value){
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        EasyAccess easyAccess = new EasyAccess();
        easyAccess.setAccessDeviceId(devid);
        easyAccess.setAccessValue(value);
        int time = (int)(System.currentTimeMillis()/1000);
        easyAccess.setAccessTime(time);
        easyAccessRepository.save(easyAccess);
    }

    public void saveAccess(int devid,int value,int time){
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        EasyAccess easyAccess = new EasyAccess();
        easyAccess.setAccessDeviceId(devid);
        easyAccess.setAccessValue(value);
        easyAccess.setAccessTime(time);
        easyAccessRepository.save(easyAccess);
    }

    public void saveAccessUrl(int id,String url){
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        EasyAccess easyAccess = new EasyAccess();
        easyAccess.setAccessAutoId(id);
        int time = (int)(System.currentTimeMillis()/1000);
        easyAccess.setAccessTime(time);
        easyAccess.setAccessNewUrl(url);
        easyAccessRepository.save(easyAccess);
    }

    public ArrayList<String> SmallPicDecode(String[] cmd){
        ArrayList<String> res = new ArrayList<String>();
        BufferedReader br = null;
        try{
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int exitcode = p.waitFor();
            if(exitcode == 0) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    res.add(line);
                }
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    public byte[] HexString2Byte(String buf){
        byte[] ret=null;
        if(buf.length()%2!=0){
            return null;
        }else{
            ret = new byte[buf.length()/2];
        }

        for(int i=0;i<buf.length()/2;i++){
            String substr= buf.substring(i*2,2);
            ret[i]=Byte.decode("0x"+substr);
        }
        return ret;
    }

    public int findTopAccessId(int sn){
        int id = 0;
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        List<EasyAccess> list = easyAccessRepository.findTop1ByAccessDeviceIdOrderByAccessTimeDesc(devid);
        if(list.isEmpty()){

        }else{
            id =list.get(0).getAccessAutoId();
        }
        return id;
    }

    public int findTopAccessInt(int sn){
        int pre_value = 0;
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        List<EasyAccess> list = easyAccessRepository.findTop1ByAccessDeviceIdOrderByAccessTimeDesc(devid);
        if(list.isEmpty()){

        }else{
            pre_value =list.get(0).getAccessValue();
        }
        return pre_value;
    }

    public String findTopAccessStr(int sn){
        int pre_value = 0;
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        List<EasyAccess> list = easyAccessRepository.findTop1ByAccessDeviceIdOrderByAccessTimeDesc(devid);
        if(list.isEmpty()){

        }else{
            pre_value =list.get(0).getAccessValue();
        }
        String value_str = String.format("%5d",pre_value);
        return value_str;
    }

    public int preParseAccessValue(String str_num){
        int value = 0;
        String pre_value_str=null;
        if(!str_num.isEmpty()){
            if(str_num.contains("a")){
                pre_value_str = findTopAccessStr(devid);
            }
            str_num=str_num.replace(",","");
            String[] sub_str = new String[NUM_COUNT];
            String value_str = "";
            for(int i=0;i<NUM_COUNT;i++){
                sub_str[i] = str_num.substring(i,1);
                if(sub_str[i].equals("a")){
                    sub_str[i]=pre_value_str.substring(i,1);
                }
                value_str+=sub_str[i];
            }
            value = Integer.valueOf(value_str);
        }
        return value;
    }



    public EasyDevice findEasyDev(int sn){
        EasyDevice dev = null;
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        try {
            List<EasyDevice> list = easyDevRepository.findByDeviceDeviceId(sn);
            for (int i = 0; i < list.size(); i++) {
                log.debug(i + ":" + list.get(i).toString());
            }
            if (list.isEmpty()) {

            } else {
                dev = list.get(0);
            }
        }catch (JpaSystemException e){

        }
        return dev;
    }

    public int parseDevId(byte[] msg){
        byte[] sn_buf = new byte[SN_LEN];
        System.arraycopy(msg,DATA_START,sn_buf,0,SN_LEN);
        for(int i=0;i<SN_LEN;i++){
            log.debug(""+sn_buf[i]);
        }
        String sn_str =new String(sn_buf);
        log.debug(sn_str);
        int sn_c = Integer.valueOf(sn_str);
        return sn_c;
    }

    public int parseBatLev(byte[] msg){
        return msg[BATLEVL_START];
    }

    public static int formatDate(String dateStr, String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        Date result=null;
        int time=0;
        try {
            result = sdf.parse(dateStr);
            time = (int)(result.getTime()/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public String getNormalResultStr(int delay,int delay_sub){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%2d", delay) + String.format("%4d", delay_sub);
        return "OK"+dateString;
    }

    public String getResultStr(boolean state,int delay,int delay_sub){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%2d", delay) + String.format("%4d", delay_sub);
        if(state) {
            ret = PASS_STR+dateString;
        }else{
            ret = FAIL_STR+dateString;
        }
        return ret;
    }

    public String getResultStr(boolean state,int delay,int delay_sub,int led_type,int led_lev){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%2d", delay) + String.format("%4d", delay_sub);
        if(state) {
            ret = PASS_STR+dateString+led_type+String.format("%2d", led_lev);
        }else{
            ret = FAIL_STR+dateString+led_type+String.format("%2d", led_lev);
        }
        return ret;
    }

    public String getResultStrConfig(int delay,int delay_sub,String conf){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%2d", delay) + String.format("%4d", delay_sub);
        return PASS2_STR+dateString+conf;
    }
}
