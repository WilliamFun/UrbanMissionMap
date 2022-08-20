package com.duke.udp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.duke.udp.interf.UDPSendBase;
import com.duke.udp.multicast.UDPMulticastBase;
import com.duke.udp.multicast.UDPMulticastSend;
import com.duke.udp.util.DExecutor;
import com.duke.udp.util.InnerHandler;
import com.duke.udp.util.UDPListener;
import com.duke.udp.util.UDPUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UDPSendHelper {
    public byte[] bytes;
    public ArrayList<String> multibytes=new ArrayList<String>();
    private InnerHandler handler;
    private UDPSendBase sendSocket;
    private volatile boolean isStopSend;
    private volatile long sendGap = 300;//80ms发送间隔

    /**
     * 设置发送线程的时间间隔，避免发送速度过快
     *
     * @param sendGap
     */
    private void setSendGap(long sendGap) {
        this.sendGap = sendGap;
    }

    /**
     * 停止发送消息
     */
    public void stopSend() {
        isStopSend = true;
    }

    /**
     * 设置数据回调
     *
     * @param listener
     */
    public void setUDPListener(UDPListener listener) {
        handler.setListener(listener);
    }

    public UDPSendHelper(Context context, int sendPort, String ip) {
        handler = new InnerHandler();
        sendSocket = new UDPMulticastSend(context, sendPort, ip);
    }

    public void start() {
        // 发送线程
        DExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String text = null;
                    for(int j=0;j<multibytes.size();j++){
                        text = multibytes.get(j);
                        bytes=hexToByteArray(text);
                        for (int i=0;i<3;i++) {//发送三次有效帧
                            //text = bytesToHex(bytes);
                            sendSocket.send(bytes);
                            handler.sendSuccess(text);
                            if (sendGap > 0) {
                                Thread.sleep(sendGap);
                            }//发送间隔80ms
                        }
                        byte[] byte0=new byte[25];//发送一次空指令
                        byte0[0]=(byte) 0xee;
                        byte0[1]=(byte) 0x16;
                        byte0[2]=(byte) 0xa5;
                        byte0[3]=(byte) 0x15;
                        byte0[4]=(byte) 0xaa;
                        byte0[24]=CheckSum(byte0,25);//校验和
                        //bytes=byte0;
                        //text = bytesToHex(byte0);
                        sendSocket.send(byte0);
                        handler.sendSuccess(bytesToHex(byte0));
                        if (sendGap > 0) {
                            Thread.sleep(sendGap);
                        }//发送间隔80ms
                        System.out.println(Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.error(e.getLocalizedMessage());
                } finally {
                    if (sendSocket instanceof UDPMulticastBase) {
                        ((UDPMulticastBase) sendSocket).closeAll();
                    }
                }
            }

        });
    }

    public void onDestroy() {
        stopSend();
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

    public static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=hexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }

    public static byte hexToByte(String inHex){
        return (byte)Integer.parseInt(inHex,16);
    }

    public byte CheckSum (byte[] bytes,int length){
        int i;
        byte sum=0;
        for(i=2;i<length-1;i++){
            sum+=bytes[i];
        }
        return sum;
    }



}
