package com.duke.udp;

import android.content.Context;

import com.duke.udp.interf.UDPReceiveBase;
import com.duke.udp.multicast.UDPMulticastBase;
import com.duke.udp.multicast.UDPMulticastReceive;
import com.duke.udp.util.DExecutor;
import com.duke.udp.util.InnerHandler;
import com.duke.udp.util.UDPListener;


/**
 * @Author: duke
 * @DateTime: 2019-05-12 17:29
 * @Description:
 */
public class UDPReceiveHelper {
    private InnerHandler handler;
    private UDPReceiveBase receiveSocket;
    private volatile boolean isStopReceive;


    /**
     * 停止接收消息
     */
    public void stopReceive() {
        isStopReceive = true;
    }

    /**
     * 设置数据回调
     *
     * @param listener
     */
    public void setUDPListener(UDPListener listener) {
        handler.setListener(listener);
    }

    public UDPReceiveHelper(Context context, int receivePort, String ip) {
        handler = new InnerHandler();
        receiveSocket = new UDPMulticastReceive(context, receivePort, ip);
    }

    public void start() {
        //接收线程
        DExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!isStopReceive) {
                        handler.receiveSuccess(bytesToHex(receiveSocket.receive()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.error(e.getLocalizedMessage());
                } finally {
                    if (receiveSocket instanceof UDPMulticastBase) {
                        ((UDPMulticastBase) receiveSocket).closeAll();
                    }
                }
            }
        });
    }

    public void onDestroy() {
        stopReceive();
        DExecutor.get().shutdownNow();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }


}
