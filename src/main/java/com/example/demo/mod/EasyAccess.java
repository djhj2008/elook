package com.example.demo.mod;

import javax.persistence.*;

@Entity
@Table
public class EasyAccess {
    @Column(name = "access_autoid")
    private int accessAutoid;

    @Column(name = "access_device_id")
    private int accessDeviceId;

    @Column(name = "access_value")
    private int accessValue;

    @Column(name = "access_time")
    private int accessTime;

    @Column(name = "access_new_url")
    private String accessNewUrl;


    @Id
    @GeneratedValue
    public int getAccessAutoid() {
        return accessAutoid;
    }

    public void setAccessAutoid(int accessAutoid) {
        this.accessAutoid = accessAutoid;
    }

    public int getAccessDeviceId() {
        return accessDeviceId;
    }

    public void setAccessDeviceId(int accessDeviceId) {
        this.accessDeviceId = accessDeviceId;
    }

    public int getAccessValue() {
        return accessValue;
    }

    public void setAccessValue(int accessValue) {
        this.accessValue = accessValue;
    }

    public int getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(int accessTime) {
        this.accessTime = accessTime;
    }

    public String getAccessNewUrl() {
        return accessNewUrl;
    }

    public void setAccessNewUrl(String accessNewUrl) {
        this.accessNewUrl = accessNewUrl;
    }
}
