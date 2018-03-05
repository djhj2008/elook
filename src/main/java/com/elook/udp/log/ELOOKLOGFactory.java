package com.elook.udp.log;

import com.elook.udp.handle.SwpWinHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELOOKLOGFactory{
    private static final Logger log = LoggerFactory.getLogger(ELOOKLOGFactory.class);
    private final static boolean Debug = true;

    private ELOOKLOGFactory() {

    }

    public static Logger getLogger(Class<?> clazz){
        log.debug("Class:"+clazz.getName());
        return LoggerFactory.getLogger(SwpWinHandler.class);
    }
}
