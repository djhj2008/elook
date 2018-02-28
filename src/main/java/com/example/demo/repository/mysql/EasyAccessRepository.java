package com.example.demo.repository.mysql;

import com.example.demo.mod.EasyAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EasyAccessRepository extends JpaRepository<EasyAccess,Integer> {
    List<EasyAccess> findTop1ByAccessDeviceIdOrderByAccessTimeDesc(int devid);

}
