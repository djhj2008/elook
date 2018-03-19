package com.elook.udp.repository.mysql;

import com.elook.udp.mod.EasyDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EasyDevRepository extends JpaRepository<EasyDevice,Integer> {
    List<EasyDevice> findByDeviceDeviceId(int devid);
    List<EasyDevice> findDeviceDevStateByDeviceDeviceId(int devid);
    List<EasyDevice> findDeviceRepTypeByDeviceDeviceId(int devid);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set " +
            "ed.device_dev_state=?2 " +
            "where ed.device_device_id = ?1",nativeQuery = true)
    int updateStatusByDevId(int devid,int state);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set " +
            "ed.device_dev_state=?2 " +
            ",ed.device_bettery_lev=?3 " +
            "where ed.device_auto_id = ?1",nativeQuery = true)
    int updateStatusById(int auto_id,int state,int bat_lev);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set" +
            " ed.device_dev_state=?2" +
            " ,ed.device_bettery_lev=?3" +
            " ,ed.device_dev_url_errpic=?4 " +
            "where ed.device_auto_id = ?1",nativeQuery = true)
    int updateErrPicById(int auto_id,int state,int bat_lev,String path);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set " +
            "ed.device_upl_state=?2 " +
            "where ed.device_auto_id=?1",nativeQuery = true)
    int updateUplById(int auto_id,int upl_state);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update easy_device ed set " +
            "ed.device_dev_state=?2 "+
            " ,ed.device_bettery_lev=?3"+
            " ,ed.device_dev_url_pic=?4 "+
            " ,ed.device_tmp_value=?5 "+
            " ,ed.device_led_type=?6 "+
            " ,ed.device_led_level =?7 "+
            "where ed.device_auto_id=?1",nativeQuery = true)
    int updateDevFull(int auto_id,int bat_lev,int state,String path,int tmp_value,int led_type,int led_lev);
}
