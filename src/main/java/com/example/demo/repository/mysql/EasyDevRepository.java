package com.example.demo.repository.mysql;

import com.example.demo.mod.EasyDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EasyDevRepository extends JpaRepository<EasyDevice,Integer> {
    List<EasyDevice> findByDeviceDeviceId(int devid);
    List<EasyDevice> findDeviceDevStateByDeviceDeviceId(int devid);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set ed.state =?2 ed.device_bettery_lev =?3 where ed.device_auto_id = ?1",nativeQuery = true)
    int updateStatusById(int auto_id,int state,int bat_lev);
}
