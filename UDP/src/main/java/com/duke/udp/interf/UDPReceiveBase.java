package com.duke.udp.interf;

/**
 * @Author: duke
 * @DateTime: 2019-05-12 18:18
 * @Description:
 */
public interface UDPReceiveBase {
    public abstract byte[] receive();

    public abstract byte[] receive(int byteArrayLength);
}
