package com.example.demo.elook;

import java.util.Arrays;
import java.util.List;

import com.example.demo.init.StartupEvent;
import com.example.demo.mod.EasyDevice;
import com.example.demo.repository.mysql.EasyDevRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleWindow {
    private static final Logger log = LoggerFactory.getLogger(SingleWindow.class);
    final static short SWP_WIN_FLAG_NORMAL = 0;
    final static short SWP_WIN_FLAG_DONE = 1;
    final static short REV_WIN_SIZE = 512;

    private byte mFlag;
    private int mSn;
    private int mCmd;
    private byte[] slot = new byte[REV_WIN_SIZE];
    private int slot_len;

    public SingleWindow(int sn,int cmd){
        mFlag   = SWP_WIN_FLAG_NORMAL;
        mSn     = sn;
        mCmd = cmd;
    }

    public int getCmd(){
        return mCmd;
    }

    public boolean RevDone(){
        boolean ret = false;
        if(mFlag == SWP_WIN_FLAG_DONE){
            ret = true;
        }
        return ret;
    }

    public byte[] getSlot(){
        return slot;
    }

    public boolean saveSlotMsg(byte[] frame,int offset,int len){
        boolean ret = false;
        Arrays.fill(slot,(byte)0);
        slot_len=len;
        System.arraycopy(frame,offset,slot,0,len);
        mFlag = SWP_WIN_FLAG_DONE;
        return ret;
    }

    public boolean checkSum(byte[] frame,int offset,int len){
        boolean ret = false;
        int sum=0;
        int sum2 = frame[offset+len]&0xff;
        log.debug("sum2:"+sum2);
        for(int i=0 ;i<len;i++){
            sum+=frame[i]&0xff;
        }
        sum=sum&0xff;
        log.debug("sum:"+sum);
        if(sum==sum2){
            ret = true;
        }
        return ret;
    }

    public int getDevState(int sn){
        int state=0;
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        List<EasyDevice> list = easyDevRepository.findByDeviceDeviceId(sn);
        for(int i=0;i<list.size();i++) {
            log.debug(i+":" + list.get(i).toString());
        }
        if(list.isEmpty()){

        }else{
            state=list.get(0).getDeviceDevState();
        }
        return state;
    }
}
