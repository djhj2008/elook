package com.elook.udp.elook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class SwpWindows {
    private static final Logger log = LoggerFactory.getLogger(SwpWindows.class);
    final static boolean DEBUG = true;
    final static short SWP_WIN_FLAG_NORMAL = 0;
    final static short SWP_WIN_FLAG_RECEIVE = 1;
    final static short SWP_WIN_FLAG_DONE = 2;
    final static short SWP_WIN_FLAG_FULL = 3;

    final static short REV_WIN_MAX = 5;
    final static short REV_WIN_SIZE = 512;
    final static short REV_MSG_WIN_MAX = 64;

    private byte mFlag;
    private int mSn;
    private int mCmd;
    private short mSeqNum;
    private short mAckNum;
    private short Nfe;
    private byte[][] slot = new byte[REV_WIN_MAX][REV_WIN_SIZE];
    private boolean[] slot_flag = new boolean[REV_WIN_MAX];
    private int[] slot_len = new int[REV_WIN_MAX];
    private byte[] msg = new byte[REV_MSG_WIN_MAX*REV_WIN_SIZE];
    private int   msg_cursor;
    private int dcount = 0;

    public SwpWindows(int sn,short MaxNum ,short AckNum,int cmd){
        mFlag   = SWP_WIN_FLAG_NORMAL;
        mSn     = sn;
        mSeqNum = MaxNum;
        mAckNum = AckNum;
        msg_cursor = 0;
        mCmd = cmd;
    }

    public void reSwpWindows(int sn,short MaxNum ,short AckNum,int cmd){
        mSn     = sn;
        mSeqNum = MaxNum;
        mAckNum = AckNum;
        mCmd = cmd;
    }

    public boolean swpInWindow(){
        boolean ret = false;
        int pos = mAckNum - Nfe;
        int maxpos = Nfe+REV_WIN_MAX;
        if(pos == 0||(pos>0)&&pos<maxpos){
            ret = true;
        }
        return ret;
    }

    public boolean swpWinDrop(){
        boolean ret = true;
        dcount++;
        if(dcount >= REV_WIN_MAX){
            ret = true;
            dcount = 0;
        }
        return ret;
    }

    public short getSeqNum(){
        return mSeqNum;
    }

    public int getCmd(){
        return mCmd;
    }

    public byte[] getMsg(){
        return msg;
    }

    public int getMsg_cursor(){
        return msg_cursor;
    }

    public int getState(){
        return mFlag;
    }

    public short getNFE(){
        return Nfe;
    }

    public boolean RevDone(){
        boolean ret = false;
        if(mFlag == SWP_WIN_FLAG_DONE){
            ret = true;
        }
        return ret;
    }


    public short fillMsg(){
        //boolean send_flag=true;
        for(int i=0;i<REV_WIN_MAX;i++){
            int index = Nfe%REV_WIN_MAX;
            if(DEBUG) {
                log.debug("Nfe:" + Nfe + " index:" + index);
            }
            if(slot_flag[index]==false){
                if(Nfe==0){
                    //send_flag=false;
                }
                break;
                //nopossible
            }else{
                System.arraycopy(slot[index], 0, msg, msg_cursor, slot_len[index]);
                msg_cursor += slot_len[index];
            }
            Nfe++;
            //log.debug("msg_cursor:"+msg_cursor);
        }
        mFlag = SWP_WIN_FLAG_FULL;

        if(Nfe==mSeqNum){
            mFlag = SWP_WIN_FLAG_DONE;
        }

        for(int i=0;i<REV_WIN_MAX;i++){
            Arrays.fill(slot[i],(byte)0);
            slot_flag[i]=false;
            slot_len[i]=0;
        }
        return Nfe;
    }

    public void resetSwpWin(){
        if(DEBUG) {
            log.debug("Reset Win.");
        }
        Nfe = 0;
        msg_cursor = 0;
        dcount = 0;
        for(int i=0;i<REV_WIN_MAX;i++){
            Arrays.fill(slot[i],(byte)0);
            slot_flag[i]=false;
            slot_len[i]=0;
        }
    }


    public boolean swpInWindowFull(){
        boolean ret=true;
        int start = Nfe;
        int sum = REV_WIN_MAX;
        if (mSeqNum - start < REV_WIN_MAX){
            sum = mSeqNum - start;
        }
        for (int i = start; i < start+sum; i++){
            int index = i%REV_WIN_MAX;
            if(DEBUG) {
                log.debug("slot" + index + ":" + slot_flag[index]);
            }
            if (slot_flag[index] == false){
                ret = false;
                break;
            }
        }
        return ret;
    }


    public boolean saveSlotMsg(byte[] frame,int offset,int len){
        boolean ret = false;
        int index = mAckNum%REV_WIN_MAX;
        Arrays.fill(slot[index],(byte)0);
        slot_len[index]=len;
        System.arraycopy(frame,offset,slot[index],0,len);
        slot_flag[index]=true;
        mFlag = SWP_WIN_FLAG_RECEIVE;
        if(DEBUG) {
            log.debug("ack:" + mAckNum + " index:" + index);
        }
        return ret;
    }

    public boolean checkSum(byte[] frame,int offset,int len){
        boolean ret = false;
        int sum=0;
        int sum2 = frame[offset+len]&0xff;
        //log.debug("sum2:"+sum2);
        for(int i=0 ;i<len;i++){
            sum+=frame[offset+i]&0xff;
        }
        sum=sum&0xff;
        //log.debug("sum:"+sum);
        if(sum==sum2){
            ret = true;
        }
        return ret;
    }
}
