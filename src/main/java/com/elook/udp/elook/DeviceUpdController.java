package com.elook.udp.elook;

import com.elook.udp.init.StartupEvent;
import com.elook.udp.mod.EasyAccess;
import com.elook.udp.mod.EasyDevice;
import com.elook.udp.repository.mysql.EasyAccessRepository;
import com.elook.udp.repository.mysql.EasyDevRepository;
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
    private String NPIC_STR = "OK2";
    private String NLOG_STR = "OK3";
    private String PASS2_STR = "OK0";
    public String NORMAL_PATH = "E:\\amp\\Apache24\\htdocs\\NBIOT\\normalup";
    public String NORMAL_DIR = "normalup";
    public String ERROR_PATH = "E:\\amp\\Apache24\\htdocs\\NBIOT\\errorup";
    public String ERROR_DIR = "errorup";

    private final int DATA_START  = 0;
    private final int SN_LEN      = 9;
    public final int BATLEVL_START = DATA_START+SN_LEN;
    public final int BATLEVL_LEN = 1;
    public final int TEMP_START = BATLEVL_START+BATLEVL_LEN;
    public final int TEMP_LEN = 1;
    public final int SIGNAL_START = TEMP_START+TEMP_LEN;
    public final int SIGNAL_LEN = 1;
    public final int HEAD_END = SIGNAL_START+SIGNAL_LEN;
    public final int NUM_COUNT = 5;


    public DeviceUpdController(int sn,int cmd){
        this.devid = sn;
        this.cmd = cmd;
    }

    abstract public String DeviceUpdCtrlHandle(byte[] msg,int length);

    public int getDevid() {
        return devid;
    }

    public int getCmd() {
        return cmd;
    }

    public short Byte2Short(byte[] buf,int offset) {
        short num;
        num=(short)(buf[offset]<<8|buf[offset+1]);
        return num;
    }

    public int Byte2Int(byte[] buf, int offset) {
        int num;
        num=buf[offset]<<24|buf[offset+1]<<16|buf[offset+2]<<8|buf[offset+3];
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
        String path = new File(NORMAL_PATH).getAbsolutePath();
        path += File.separator + devid + File.separator + getPicDir();
        log.debug("path:" + path);
        return savePicsPath(count,width,height,bmpall,offset,path);
    }

    public String savePicsPath(int count, int width, int height, byte[] bmpall, int offset, String path) {
        File tempFile = new File(path);
        //log.debug("path:" + path);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        for(int i=0;i<count;i++) {
            OutputStream os = null;
            try {
                String fileName = i + ".bmp";
                File fd = new File(tempFile.getPath() + File.separator + fileName);
                //log.debug("file:" + fd.getPath());
                log.debug("file:" + fd.getName());
                //log.debug("file:" + fd.getParent());
                os = new FileOutputStream(fd);
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

    public void saveEasyDev(int id,int batlev,int state,int temp,int signal){
        log.debug("saveEasyDev signal:"+signal);
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        easyDevRepository.updateStatusById(id,state,batlev,temp,signal);
    }

    public void saveDevUplState(int id,int state,int temp,int signal){
        log.debug("saveDevUplState signal:"+signal);
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        easyDevRepository.updateUplById(id,state,temp,signal);
    }

    public void saveDevErrPic(int id,int batlev,int state,String path,int temp,int signal){
        log.debug("saveDevErrPic signal:"+signal);
        int index = path.lastIndexOf(ERROR_DIR);
        path = path.substring(index,path.length());
        path = path.replace("\\","/");
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        easyDevRepository.updateErrPicById(id,state,batlev,path,temp,signal);
    }

    public void saveDevFull(int id,int batlev,int state,String path,int tmp_value,int led_type,int led_lev,int temp,int signal){
        log.debug("saveDevFull signal:"+signal);
        int index = path.lastIndexOf(NORMAL_DIR);
        path = path.substring(index,path.length());
        path = path.replace("\\","/");
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        easyDevRepository.updateDevFull(id,state,batlev,path,tmp_value,led_type,led_lev,temp,signal);
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

    public void saveAccess(int devid,int value,String path){
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        EasyAccess easyAccess = new EasyAccess();
        int index = path.lastIndexOf(NORMAL_DIR);
        path = path.substring(index,path.length());
        path = path.replace("\\","/");
        easyAccess.setAccessDeviceId(devid);
        easyAccess.setAccessValue(value);
        int time = (int)(System.currentTimeMillis()/1000);
        easyAccess.setAccessTime(time);
        easyAccess.setAccessNewUrl(path);
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

    public void saveAccessUrl(EasyAccess easyAccess,String path){
        int index = path.lastIndexOf(NORMAL_DIR);
        path = path.substring(index,path.length());
        path = path.replace("\\","/");
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        //EasyAccess easyAccess = new EasyAccess();
        //easyAccess.setAccessAutoId(id);
        int time = (int)(System.currentTimeMillis()/1000);
        easyAccess.setAccessTime(time);
        easyAccess.setAccessNewUrl(path);
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
                    byte[] tmp = line.getBytes();
                    if(tmp.length>0){
                        log.debug(line);
                        res.add(line);
                    }
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
        log.debug(buf);
        log.debug("len:"+buf.length());
        if(buf.length()%2!=0){
            return null;
        }else{
            ret = new byte[buf.length()/2];
        }

        for(int i=0;i<buf.length()/2;i++){
            int begin = i*2;
            int end = begin+2;
            String substr= buf.substring(begin,end);
            int temp = Integer.decode("0x"+substr);
            ret[i] = (byte)(temp&0xff);
            //ret[i]=Byte.decode("0x"+substr);
        }
        return ret;
    }

    public EasyAccess findTopAccess(int sn){
        EasyAccess ea = null;
        EasyAccessRepository easyAccessRepository = (EasyAccessRepository) StartupEvent.getBean(EasyAccessRepository.class);
        List<EasyAccess> list = easyAccessRepository.findTop1ByAccessDeviceIdOrderByAccessTimeDesc(devid);
        if(list.isEmpty()){

        }else{
            ea =list.get(0);
        }
        return ea;
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
        String value_str = String.format("%05d",pre_value);
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
                sub_str[i] = str_num.substring(i,i+1);
                if(sub_str[i].equals("a")){
                    sub_str[i]=pre_value_str.substring(i,i+1);
                }
                value_str+=sub_str[i];
            }
            value = Integer.valueOf(value_str);
        }
        return value;
    }

    public int preParseAccessValueOld(String str_num){
        int ret = 0;
        String prefix = "digital after : ";
        log.debug(str_num);
        if(str_num.startsWith(prefix)) {
            int begin = prefix.length();
            int end = str_num.length();
            String str = str_num.substring(begin, end);
            ret = Integer.valueOf(str.trim());
        }
        return ret;
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
//        for(int i=0;i<SN_LEN;i++){
//            log.debug(""+sn_buf[i]);
//        }
        String sn_str =new String(sn_buf);
        log.debug(sn_str);
        int sn_c = Integer.valueOf(sn_str);
        return sn_c;
    }

    public int parseBatLev(byte[] msg){
        return parseCharValue(msg[BATLEVL_START]&0xff);
    }

    public int parseTemp(byte[] msg){
        log.debug("temp:"+msg[TEMP_START]);
        return msg[TEMP_START];
    }

    public int parseSignal(byte[] msg){
        log.debug("signal:"+msg[SIGNAL_START]);
        return msg[SIGNAL_START];
    }

    public int parseCharValue(int num){
        int ret = 0;
        char tmp = (char) num;
        String str = String.valueOf(tmp);
        ret = Integer.valueOf(str);
        return ret;
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

    public String getNormalResultStr(int rep_type,int delay,int delay_sub){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%02d", delay) + String.format("%04d", delay_sub);
        return "OK0"+rep_type+dateString;
    }

    public String getResultStrLog(int rep_type,int upl_state,int delay,int delay_sub){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        String delay_str =  String.format("%02d", delay) + String.format("%04d", delay_sub);
        log.debug(delay_str);
        if(upl_state==0) {
            ret = PASS2_STR+rep_type+dateString+delay_str;
        }else if(upl_state==3) {
            ret = NLOG_STR+rep_type+dateString+delay_str;
        }else {
            ret = PASS2_STR+rep_type+dateString+delay_str;
        }
        return ret;
    }

    public String getResultStr(int rep_type,boolean state,int delay,int delay_sub){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        String delay_str =  String.format("%02d", delay) + String.format("%04d", delay_sub);
        log.debug(delay_str);
        if(state) {
            ret = PASS_STR+rep_type+dateString+delay_str;
        }else{
            ret = FAIL_STR+rep_type+dateString+delay_str;
        }
        return ret;
    }

    public String getResultStr(int rep_type,boolean state,int delay,int delay_sub,int led_type,int led_lev){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%02d", delay) + String.format("%04d", delay_sub);
        if(state) {
            ret = PASS_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }else{
            ret = FAIL_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }
        return ret;
    }

    public String getResultStrLog(int rep_type,int upl_state,int delay,int delay_sub,int led_type,int led_lev){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%02d", delay) + String.format("%04d", delay_sub);
        if(upl_state==0) {
            ret = PASS_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }else if(upl_state==1||upl_state==3) {
            ret = NPIC_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }else if(upl_state==2) {
            ret = NLOG_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }else {
            ret = PASS_STR+rep_type+dateString+led_type+String.format("%02d", led_lev);
        }
        return ret;
    }

    public String getResultStrConfig(int rep_type,int delay,int delay_sub,String conf){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%02d", delay) + String.format("%04d", delay_sub);
        return PASS2_STR+rep_type+dateString+conf;
    }

    public String getResultStrConfigLog(int rep_type,int delay,int delay_sub,String conf){
        String ret = null;
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        dateString += String.format("%02d", delay) + String.format("%04d", delay_sub);
        return NLOG_STR+rep_type+dateString+conf;
    }
}
