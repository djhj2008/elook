package com.elook.udp.elook;

import java.util.Arrays;
import java.util.List;

import com.elook.udp.init.StartupEvent;
import com.elook.udp.mod.EasyDevice;
import com.elook.udp.repository.mysql.EasyDevRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaSystemException;

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

    public int getDevState(int sn){
        int state=-1;
        EasyDevRepository easyDevRepository = (EasyDevRepository) StartupEvent.getBean(EasyDevRepository.class);
        //try {
        List<EasyDevice> list = easyDevRepository.findDeviceDevStateByDeviceDeviceId(sn);
        for(int i=0;i<list.size();i++) {
            log.debug(i+":" + list.get(i).toString());
        }
        if(!list.isEmpty()){
            state=list.get(0).getDeviceDevState();
        }
        //}catch (JpaSystemException e){

        //}
        if(state == 0){
            easyDevRepository.updateStatusByDevId(sn,EasyDeviceInfo.DEVSTATE_HAS_CONNECT_SERVER);
        }

        list = easyDevRepository.findDeviceDevStateByDeviceDeviceId(sn);
        for(int i=0;i<list.size();i++) {
            log.debug(i+":" + list.get(i).toString());
        }
        if(!list.isEmpty()){
            state=list.get(0).getDeviceDevState();
        }

        return state;
    }
}
