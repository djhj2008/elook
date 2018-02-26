package com.example.demo.repository.mysql;

import com.example.demo.mod.EasyDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EasyDevRepository extends JpaRepository<EasyDevice,Integer> {
    List<EasyDevice> findByDeviceDeviceId(int devid);
    List<EasyDevice> findDeviceDevStateByDeviceDeviceId(int devid);
}
