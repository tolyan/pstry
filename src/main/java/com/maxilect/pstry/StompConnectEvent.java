/**
 * Copyright © Anatoly Rybalchenko, 2016
 * a.rybalchenko@gmail.com
 */
package com.maxilect.pstry;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;

public class StompConnectEvent implements ApplicationListener<SessionConnectEvent> {

    final static Logger logger = Logger.getLogger(StompConnectEvent.class);


    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

//        String  company = sha.getNativeHeader("company").get(0);
        logger.debug("Connect event");
    }
}