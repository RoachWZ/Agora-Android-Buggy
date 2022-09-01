package com.onLineDetect.service;

import java.net.Socket;

import com.csst.videotalk.FfmpegActivity;
import com.csst.videotalk.VideoTalkActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * �������񣬼���Ƿ����û�����
 * 
 * @author androidtv.power@gmail.com
 * 
 */
public class OnLineDetectService extends Service {
	//
//	private Handler mHandler;
	
	final String TAG="OnLineDetectService";
	
    private NetRateBinder netRateBinder=new NetRateBinder();
    
    
    private final int DetectTimeRate=3000;
    
    private String localIpHead="";
	private Socket clientSocket = null;
	private static final int PORT=5432;   //�ͱ��еļ����߳�ͨ�ŵĶ˿�

    
    /**
     * 
     * 
     */
    public void detecting(){
    	    int i=1;
            while(i<255){
    			   String destIp=(localIpHead+i).trim();

          			try {
     				    Log.e(TAG,"======================׼������"+destIp);

     				    clientSocket = new Socket(destIp,PORT);
						clientSocket.setSoTimeout(20);

     		    	  } catch (Exception e) {
     				    Log.e(TAG,i+"û������"+e);
//     				    testTimes++;

     			     }
           			Log.e("222","########################################");
         			if(clientSocket!=null){
         	   			Log.e("222","����ip��"+clientSocket.getInetAddress().getHostAddress());
         	   			i++;
         			}
               }
    	
    } 
    
    
    
//	/**
//	 * �����߳������Եػ�ȡ�Ƿ����û�����
//	 */
//	private Runnable mRunnable = new Runnable() {
//		// ÿ3���ӻ�ȡһ�����ݣ���ƽ�����Լ��ٶ�ȡϵͳ�ļ�������������Դ����
//		@Override
//		public void run() {
////			Log.e(TAG,"������߷����������е���...."+localIpHead);
//			detecting();
//			
//			mHandler.postDelayed(mRunnable, DetectTimeRate);		
//
//		}
//	};

	/**
	 * ͨ���۳�Binder ��ʵ��Ibinder ��
	 * 
	 * @author User
	 *
	 */
	public class NetRateBinder extends Binder{
		public void startGetNetRate(){
			Log.e(TAG,"Yes, start==========");
   
		}
	
		public void stopGetNetRate(){
			Log.e(TAG,"no, stop ==========");

		}
	}
	
	/**
	 * ��ʱ�ص��ķ���
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG,"---- NetRateBinder is onRun! ----");

		return netRateBinder;
	}
	
	/**
	 * �����ʱ�ص��ķ���
	 * 
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG,"---- NetRateBinder is quited ! ----");
        return true;
	}
	
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        
//		mHandler.postDelayed(mRunnable, 500);
       
        
        return START_STICKY;
    }
	

    private String getLocalIpHead(){  
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);    
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();    
        int ipAddress = wifiInfo.getIpAddress();   
        Log.d(TAG, "int ip "+ipAddress);  
        if(ipAddress==0)return null;  
        String temp= ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
        
        return temp.substring(0, temp.lastIndexOf('.')+1);
        
    } 

	/**
	 * �ڷ������ʱɾ����Ϣ����
	 * 
	 */
	@Override
	public void onDestroy() {

//		mHandler.removeCallbacks(mRunnable);
		super.onDestroy();
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG,"---- onCreate ! ----");

        
//		mHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				super.handleMessage(msg);
//				if (msg.what == 1){
//					
//				}
//			}
//		};
//		mHandler.postDelayed(mRunnable, 500);
        localIpHead=getLocalIpHead();
        new Thread(){
        	@Override
        	public void run(){
                while(true){

                	
                   try {
					  sleep(3000);
				   } catch (InterruptedException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				    }
                   
                   
                   detecting();

                }
        	}
        }.start();

	}

}
