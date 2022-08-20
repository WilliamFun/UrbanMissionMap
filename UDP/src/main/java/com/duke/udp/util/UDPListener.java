package com.duke.udp.util;

/**
 * @Author: duke
 * @DateTime: 2019-05-12 17:50
 * @Description:
 */
public abstract class UDPListener {
    public abstract void onError(String error);

    public abstract void onReceive(String content) throws IllegalAccessException;

    public abstract void onSend(String content);
}
