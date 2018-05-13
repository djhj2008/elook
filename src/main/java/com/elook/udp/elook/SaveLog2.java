package com.elook.udp.elook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveLog2 extends DeviceUpdController {
    private static final Logger log= LoggerFactory.getLogger(SaveLog2.class);
    public String Log_PATH = "E:\\amp\\Apache24\\htdocs\\NBIOT\\devlog";
    public SaveLog2(int sn, int cmd) {
        super(sn, cmd);
    }

    @Override
    public String DeviceUpdCtrlHandle(byte[] msg, int length) {
        String filename =null;
        if(getCmd()==ElookCmdUrl.LOGS2) {
            filename = saveLogs2(msg, length, getDevid());
        }else{

        }
        return "OK0";
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

    public String saveLogsPath(byte[] msg,int length,int devid,String path) {
        OutputStream os = null;
        String fileName = null;
        File tempFile = new File(path);
        log.debug("path:"+path+" len:"+length);
        try {
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            fileName=tempFile.getPath() + File.separator+getPicName()+".log";
            log.debug("file:"+fileName);
            os = new FileOutputStream(fileName);
            os.write(msg, 0,length);

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

    public String saveLogs2(byte[] msg,int length,int devid) {
        String path = new File(Log_PATH).getAbsolutePath();
        path += File.separator+devid+File.separator+getPicDir();
        log.debug("path:"+path);
        return saveLogsPath(msg,length,devid,path);
    }
}
