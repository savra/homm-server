package com.hvdbs.hommserver;

import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
public class ServerSocketHandler {
    public String handleMessage(byte[] message, MessageHeaders messageHeaders) {
        String string = new String(message);
        System.out.println(string);
        return string.toUpperCase();
    }
}
