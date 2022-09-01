package com.csst.videotalk;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.csst.ffmpeg.control.ConnectListeningServer;
import com.onLineDetect.service.OnLineDetectService;

public class FfmpegActivity extends Activity {
    /** Called when the activity is first created. */
	String TAG="FfmpegActivity";
	ImageButton video_Talk,video_monitor;
	EditText edit_To;
	TextView textViewShowIP;
    private ListView denifinitonList;
	private List<Map<String, Object>> MovieDefinitions=new ArrayList<Map<String, Object>>();      //Data of MoiveSourceGridView
	String localIp="";
	String localIpHead="";
	String VideoTalk="com.csst.ffmpeg.video_talk";
	String VideoMonitor="com.csst.ffmpeg.video_monitor";
	private Socket clientSocket = null;
	private PrintStream outStream=null;

	private static  String HOSTIP="192.168.10.177";
	private static final int PORT=5432;   //�ͱ��еļ����߳�ͨ�ŵĶ˿�

    OnLineDetectService.NetRateBinder netRateBinder;

    /**
     * ���������ֻ������еģ�Ҳ����һ��ƽ���ϵ�apk(ƽ���ui�ÿ��㵫��bug�࣬�ȿ���wifi)
     * �Խ��������豸Ӧ��ͬһ��WLAN�С�����Ip������ʼ�������Ŀ�����ɣ�
     * 
     * 
     */
    private ServiceConnection conn=new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.e(TAG,"Activity �Ͽ�����service");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.e(TAG,"Activity ׼������ service");
			netRateBinder=(OnLineDetectService.NetRateBinder)service;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.main);
        textViewShowIP=(TextView)findViewById(R.id.textViewShowIP);
        edit_To = (EditText)findViewById(R.id.edit_to);
        video_Talk=(ImageButton) findViewById(R.id.imageButton1);
        video_monitor=(ImageButton) findViewById(R.id.imageButton2);
		ButtonListenser btnListener = new ButtonListenser();
		video_Talk.setOnClickListener(btnListener);
		video_monitor.setOnClickListener(btnListener);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //Ӧ������ʱ��������Ļ������������
		localIp=getlocalIp();
		new ConnectListeningServer(this).start();
		//this.setTitle(this.getTitle()+"  (����豸����ͬһ WLAN ��)"+"  -- "+getlocalIp()+" # ����QQ��480474041");
		textViewShowIP.setText("  (����豸����ͬһ WLAN ��)"+"  -- "+getlocalIp());
		if(localIp!=""){
			localIpHead=localIp.substring(0, localIp.lastIndexOf('.')+1);
		}    
    }
    
         
    /**
     * int--> (ip)int
     * 
     * @return
     */
    public String getlocalIp(){  
       WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);    
       WifiInfo mWifiInfo = wifiManager.getConnectionInfo();   
       if(mWifiInfo ==null){
    	   return "ip=null";
       }else{
           int ipAddress=mWifiInfo.getIpAddress();
           Log.e("222","!!=="+ipAddress+"===");
           if(ipAddress==0)  return "ip=null";
           return "ip="+((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                   +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
       }
    }
    
    
    /**
     * �����Խ��߳�
     * 
     */
    public void startCallSomeBody(){
		new Thread(){ 
			 @Override
			 public void run(){
				    boolean connectFlag=true;
				    HOSTIP=edit_To.getText().toString();
				    if(!HOSTIP.equals(localIp)){
				    	int time=0;
					    while(connectFlag){   
							try {
								   clientSocket = new Socket(HOSTIP,PORT);
								   clientSocket.setSoTimeout(5000);
							} catch (Exception e) {
									System.out.println("Client: ���Ӵ���"+e);
							}
							if(clientSocket!=null){
					   			Log.e(TAG,"���е���Ϣ��"+clientSocket.getInetAddress().getHostAddress());
					   			connectFlag=false;
				 				Intent intent = new Intent();
				 				intent.setClass(FfmpegActivity.this, VideoTalkActivity.class);
				 				intent.putExtra("CONNECTIP",clientSocket.getInetAddress().getHostAddress());
				 				intent.putExtra("CONNECTTYPE", "DOCALLER");
				 				FfmpegActivity.this.startActivity(intent);	
							}else{
								try {
									sleep(500);
									time++;
									System.out.println("���Ӳ��ϰ�");
									if(time==20){
										connectFlag=false;
//								    	Toast.makeText(getApplicationContext(), "�Է������ߣ�",
//								    		     Toast.LENGTH_SHORT).show();
									}

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

//		 				Intent intent = new Intent();
//		 				intent.setClass(FfmpegActivity.this, MainActivity.class);
//		 				intent.putExtra("CONNECTIP",HOSTIP);
//		 				intent.putExtra("CONNECTTYPE", "DOCALLER");
//		 				FfmpegActivity.this.startActivity(intent);	

				    }else{
//				    	Toast.makeText(getApplicationContext(), "�㲻�ܺ����Լ���",
//				    		     Toast.LENGTH_SHORT).show();
				    	}

			 }
		}.start();
    }
    class ButtonListenser implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.imageButton1) {	
				startCallSomeBody();							
			}else if(v.getId() == R.id.imageButton2){
				startCallSomeBody();  //���õ�
			}
		}	
	}//buttonlistening is over here
    
    
    
    //=======================��Activity�����������========================================
    /**
     * ����Activity ��ʱ��ᱻ�ص���
     * 
     */
    @Override
    protected void onStart(){
    	super.onStart();  
//		Intent intent = new Intent();
//		intent.setAction("ZLB.LIVE_TV.OnLineDetectService");
//        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    	Log.d(TAG, "--onStart");    	
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
    	Log.d(TAG, "--onResume");    
    	if(clientSocket!=null){
        	try {
    			clientSocket.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

		super.onResume();	
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
    	Log.d(TAG, "--onpause");    	

	}	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		super.onPause();
    	Log.d(TAG, "--onStop");      	

	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
//    	if(netRateBinder!=null){
//        	netRateBinder.stopGetNetRate();
//    	}
// 		unbindService(conn);
		super.onDestroy();
		
	}
    
    
    
    
    
    
    
    
    
}