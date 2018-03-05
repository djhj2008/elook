package com.elook.udp.repository.mysql;

import com.elook.udp.mod.EasyAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EasyAccessRepository extends JpaRepository<EasyAccess,Integer> {
    List<EasyAccess> findTop1ByAccessDeviceIdOrderByAccessTimeDesc(int devid);

}
