package com.example.demo.mod;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * Created by dj on 2018/1/19.
 *
 * 用来记录接收的UDP消息的日志
 */
@Entity
@Table
@DynamicUpdate
public class EasyDevice {
    @Column(name = "device_auto_id")
    private int deviceAutoId;

    @Column(name = "device_device_id")
    private int deviceDeviceId;

    @Column(name = "device_dev_state")
    private int deviceDevState;

    @Column(name = "device_dev_state_sub")
    private int deviceDevStateSub;

    @Column(name = "device_dev_config")
    private String deviceDevConfig;

    @Column(name = "device_upl_state")
    private int deviceUplState;

    @Column(name = "device_up_delay")
    private int deviceUpDelay;

    @Column(name = "device_up_delay_sub")
    private int deviceUpDelaySub;

    @Column(name = "device_led_type")
    private int deviceLedType;

    @Column(name = "device_led_level")
    private int deviceLedLevel;

    @Column(name = "device_tmp_value")
    private int deviceTmpValue;

    @Column(name = "device_bettery_lev")
    private int deviceBetteryLev;

    @Column(name = "device_dev_url_pic")
    private String deviceDevUrlPic;

    @Column(name = "device_dev_url_errpic")
    private String deviceDevUrlErrpic;

    @Id
    @GeneratedValue
    public int getDeviceAutoId() {
        return deviceAutoId;
    }

    public int getDeviceDeviceId() {
        return deviceDeviceId;
    }

    public int getDeviceDevState() {
        return deviceDevState;
    }

    public int getDeviceDevStateSub() {
        return deviceDevStateSub;
    }

    public String getDeviceDevConfig() {
        return deviceDevConfig;
    }

    public int getDeviceUplState() {
        return deviceUplState;
    }

    public int getDeviceUpDelay() {
        return deviceUpDelay;
    }

    public int getDeviceUpDelaySub() {
        return deviceUpDelaySub;
    }

    public int getDeviceLedType() {
        return deviceLedType;
    }

    public int getDeviceLedLevel() {
        return deviceLedLevel;
    }

    public int getDeviceTmpValue() {
        return deviceTmpValue;
    }

    public int getDeviceBetteryLev() {
        return deviceBetteryLev;
    }

    public String getDeviceDevUrlPic() {
        return deviceDevUrlPic;
    }

    public String getDeviceDevUrlErrpic() {
        return deviceDevUrlErrpic;
    }

    public void setDeviceAutoId(int deviceAutoId) {
        this.deviceAutoId = deviceAutoId;
    }

    public void setDeviceDeviceId(int deviceDeviceId) {
        this.deviceDeviceId = deviceDeviceId;
    }

    public void setDeviceDevState(int deviceDevState) {
        this.deviceDevState = deviceDevState;
    }

    public void setDeviceDevStateSub(int deviceDevStateSub) {
        this.deviceDevStateSub = deviceDevStateSub;
    }

    public void setDeviceDevConfig(String deviceDevConfig) {
        this.deviceDevConfig = deviceDevConfig;
    }

    public void setDeviceUplState(int deviceUplState) {
        this.deviceUplState = deviceUplState;
    }

    public void setDeviceUpDelay(int deviceUpDelay) {
        this.deviceUpDelay = deviceUpDelay;
    }

    public void setDeviceUpDelaySub(int deviceUpDelaySub) {
        this.deviceUpDelaySub = deviceUpDelaySub;
    }

    public void setDeviceLedType(int deviceLedType) {
        this.deviceLedType = deviceLedType;
    }

    public void setDeviceLedLevel(int deviceLedLevel) {
        this.deviceLedLevel = deviceLedLevel;
    }

    public void setDeviceTmpValue(int deviceTmpValue) {
        this.deviceTmpValue = deviceTmpValue;
    }

    public void setDeviceBetteryLev(int deviceBetteryLev) {
        this.deviceBetteryLev = deviceBetteryLev;
    }

    public void setDeviceDevUrlPic(String deviceDevUrlPic) {
        this.deviceDevUrlPic = deviceDevUrlPic;
    }

    public void setDeviceDevUrlErrpic(String deviceDevUrlErrpic) {
        this.deviceDevUrlErrpic = deviceDevUrlErrpic;
    }

    public String toString()
    {
        return "id:"+deviceAutoId+" devid:"+deviceDeviceId+" state:"+deviceDevState+" tmp_value:"+deviceTmpValue;
    }
}
