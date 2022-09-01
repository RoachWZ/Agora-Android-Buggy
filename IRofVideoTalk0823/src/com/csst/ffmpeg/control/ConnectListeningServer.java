package com.csst.ffmpeg.control;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.csst.videotalk.FfmpegActivity;
import com.csst.videotalk.VideoTalkActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


public class ConnectListeningServer extends Thread {
	String TAG="ConnectListeningServer";
	ServerSocket serverSocket = null;//new ServerSocket(4038);
	Socket s = null;//so.accept();    
	private final int serverPort=5432;
	private Context mContext;
	private Handler mHandler = null;
	
	public ConnectListeningServer(Handler mHandler){
		this.mHandler=mHandler;
	}
	
	public ConnectListeningServer(Context mContex){
		this.mContext=mContex;
	}
	  
	public void run(){
		try {
			System.out.println("�����пͻ�������...... " );
			serverSocket = new ServerSocket(serverPort);
			while (true){
     			s = serverSocket.accept();    //��һֱ����ͣ������ģ�sӦ�ñ�����ά�ֻỰ
     			Log.e(TAG,"�������ӵ�IP is :"+s.getInetAddress().getHostAddress());
     			 
 				Intent intent = new Intent();
 				intent.setClass(mContext, VideoTalkActivity.class);
 				intent.putExtra("CONNECTIP",s.getInetAddress().getHostAddress());
 				intent.putExtra("CONNECTTYPE", "BECALLER");
// 				intent.
 				mContext.startActivity(intent);	
 				
            }  

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
