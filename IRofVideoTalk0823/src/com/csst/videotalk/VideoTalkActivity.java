package com.csst.videotalk;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import com.csst.ffmpeg.FFMpegIF;
import com.csst.ffmpeg.views.BeCalledImageView;
import com.csst.ffmpeg.views.DoCalledImageView;
import com.csst.videotalk.R;
import com.csst.ir.control.ReviceCtrlThread;
import com.csst.ir.control.SendCtrlThread;

/**
 * ���ӶԽ�
 *    
 * 
 * @author User
 *
 */
public class VideoTalkActivity extends Activity  {
	enum RunState {
		RUN_STATE_IDLE,
		RUN_STATE_READY,
		RUN_STATE_RUNNING,
		RUN_STATE_STOP,
	};

	private final int beCalledImageDrawRate=100;    //���е�Ӱ���ˢ�¼��ʱ�䣬��λ ����

	private String tag = "ffmpegdemo";

	/*�趨¼������ش�С*/
	public  static int CAMERA_W = 640;
	public  static int CAMERA_H = 480;

	public  int remote_w;
	public  int remote_h;


	private  int port1=5444;    //���з�ʹ��port1 ���б���ͨ�Ŷ˿�,port2���н���ͨ�Ŷ˿�
	private  int port2=5446;    //���з�ʹ��port1 ���н���ͨ�Ŷ˿�,port2���б���ͨ�Ŷ˿�
	private String connectIp;
	private String connectType;


	/*�ؼ���������*/
	//	private ImageButton btn_photo;
	private ImageButton btn_stop;
	public   static Bitmap beCalledBitmap;

	private BeCalledImageView beCalledImageView;

	//	private ImageView beCalledImageView;


	private DoCalledImageView doCalledImageView;

	private RunState decodeState;
	private RunState encodeState;

	//    public static int[] colors;

	private static final String TAG = "ConsumerIrTest";
	// Android4.4֮�� ����ң��ConsumerIrManager
	private ConsumerIrManager mCIR;
	private int cameraId = 0;
	
    private static final int CAMERA_CHANGE=1;
	private static final int CHANGE_LIGHT_ON=2;
	private static final int CHANGE_LIGHT_OFF=3;


	private Handler handler= new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==CAMERA_CHANGE){
				doCalledImageView.destroyCamera();
				doCalledImageView.myCamera = Camera.open(1-cameraId);
				doCalledImageView.initCamera();
			}else if(msg.what==CHANGE_LIGHT_ON){
				Camera.Parameters parameters = doCalledImageView.myCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				doCalledImageView.myCamera.setParameters(parameters);
			}else if(msg.what==CHANGE_LIGHT_OFF){
				Camera.Parameters parameters = doCalledImageView.myCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				doCalledImageView.myCamera.setParameters(parameters);
			}

		};
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_talk);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //Ӧ������ʱ��������Ļ������������
		decodeState = RunState.RUN_STATE_IDLE; 
		encodeState = RunState.RUN_STATE_IDLE; 

		btn_stop = (ImageButton)findViewById(R.id.btn_stop);
		ButtonListenser btnListener = new ButtonListenser();
		btn_stop.setOnClickListener(btnListener);

		// ��ȡϵͳ�ĺ���ң�ط���
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
		initViewsAndEvents();//����ң�ذ�ť������ʼ��
		
		//��ȡ������ʾ��
		beCalledImageView=(BeCalledImageView)findViewById(R.id.imgview2);
		doCalledImageView=(DoCalledImageView)findViewById(R.id.previewSV);	
		Log.i(tag, "ffmpeg version is " + FFMpegIF.GetFFmpegVer());
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		connectIp=bundle.getString("CONNECTIP");
		connectType=bundle.getString("CONNECTTYPE");
		Thread ctrl = new ReviceCtrlThread(mCIR,handler);
		ctrl.start();
	}

	private void initViewsAndEvents() {
		findViewById(R.id.send_button1).setOnTouchListener(mSend1ClickListener);//ǰ����ť
		findViewById(R.id.send_button2).setOnTouchListener(mSend2ClickListener);//���˰�ť
		findViewById(R.id.send_button3).setOnTouchListener(mSend3ClickListener);//���
		findViewById(R.id.send_button4).setOnTouchListener(mSend4ClickListener);//�ҹ�
		findViewById(R.id.send_button_s4).setOnTouchListener(mSendS4ClickListener);//ȫ��
		findViewById(R.id.send_button_s3).setOnTouchListener(mSendS3ClickListener);//�ٶ�3
		findViewById(R.id.send_button_s2).setOnTouchListener(mSendS2ClickListener);//�ٶ�2
		findViewById(R.id.send_button_L).setOnClickListener(mSendLClickListener);//�������ص�
		findViewById(R.id.send_button_C).setOnClickListener(mSendCClickListener);//�л�����ͷ

	}
	View.OnClickListener mSendCClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			new SendCtrlThread(connectIp, 11).start();
			
		}
		
	};
	View.OnClickListener mSendLClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			new SendCtrlThread(connectIp, 10).start();
			
		}
		
	};
	View.OnTouchListener mSend1ClickListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 1).start();
			
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 5).start();
			}
			return false;
		}
	};
	View.OnTouchListener mSend2ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 2).start();
			
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 5).start();
			}
			return false;
		}
	};

	View.OnTouchListener mSend3ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 3).start();
			
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 5).start();
			}
			return false;
		}
	};
	View.OnTouchListener mSend4ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 4).start();
			
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 5).start();
			}
			return false;
		}
	};
	View.OnTouchListener mSendS4ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 6).start();
			
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 7).start();
			}
			return false;
		}
	};
	View.OnTouchListener mSendS3ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 9).start();
				
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 7).start();
			}
			return false;
		}
	};
	View.OnTouchListener mSendS2ClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 8).start();
				
			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 7).start();
			}
			return false;
		}
	};

	

	public void startVideoCall(){
		FFMpegIF.Init();

		//����������߳�
		if(encodeState == RunState.RUN_STATE_IDLE) {
			//�����̣߳����߳�����������
			Log.i(tag, "start encode thread");
			encodeState = RunState.RUN_STATE_READY;
			//create decode thread and run
			EncodeThread encthread = new EncodeThread(); 
			new Thread(encthread).start();
		}

		//����������߳�
		if(decodeState == RunState.RUN_STATE_IDLE) {
			//�����̣߳����߳����������룬��Ϊ�����һֱ�ȴ����ݣ���������̣߳�������������ffmpegif.StartDecode()��
			Log.i(tag, "start decode thread");
			decodeState = RunState.RUN_STATE_READY; 
			//create decode thread and run
			DecodeThread decThread = new DecodeThread(); 
			new Thread(decThread).start();
		}
	}

	/**
	 * ������������
	 * 
	 * @author User
	 *
	 */
	class ButtonListenser implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//			if(v.getId() == R.id.btn_photo) {   //���Է�����
			//			   
			//
			//			}
			//ֹͣ������߳�
			if(v.getId() == R.id.btn_stop)
			{
				stopVideoTalk();

				//��Ӧ�ø��߶Է�ֹͣ��Ƶ�ˣ��Ҷ�ֹͣ��

				VideoTalkActivity.this.finish();


			}
		}

	}

	//	 final  Handler UiMangerHandler = new Handler(){
	//			@Override   
	//			public void handleMessage(Message msg) {  
	//			// TODO Auto-generated method stub 
	//				switch(msg.what){ 		 
	//				case 0:
	//					beCalledImageView.setImageBitmap(beCalledBitmap);
	//
	//					break;
	//				}
	//			}
	//	 };

	/**
	 * 
	 * ˯�ߵȴ�չʾ�����Ż�
	 * 
	 */
	public void waitForDisplay(){
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * �����߳�
	 * 
	 * @author User
	 *
	 */
	class DecodeThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(decodeState == RunState.RUN_STATE_READY) {
				//				EditText textFrm = (EditText)findViewById(R.id.edit_from);

				//				String urlFrm = "rtp://"+textFrm.getText().toString();

				String urlFrm="rtp://"+connectIp+":"+port1;
				Log.i(tag, "from:"+urlFrm);

				if(FFMpegIF.StartDecode(urlFrm)<0) {
					Log.e(tag, "Start decode failed");
					FFMpegIF.StopDecode();
					decodeState = RunState.RUN_STATE_IDLE;
					return;
				}

				Log.i(tag, "Start decode successed");
				decodeState = RunState.RUN_STATE_RUNNING;

				/*��ȡ���յ�����Ƶresolution��������bitmap�Ĳ���*/
				do {
					remote_w = FFMpegIF.GetWidth();
					remote_h = FFMpegIF.GetHeight();
					try{  
						Thread.sleep(beCalledImageDrawRate); //�õ�ǰ�߳�����100����  
					}catch(InterruptedException ex){  
						ex.printStackTrace();  
					}
				} while(remote_w==0&&remote_h==0&&decodeState==RunState.RUN_STATE_RUNNING);

				if(decodeState==RunState.RUN_STATE_RUNNING) {
					Log.i("222", "width="+remote_w+" height="+remote_h);
					beCalledBitmap = Bitmap.createBitmap(remote_w, remote_h, Bitmap.Config.ARGB_8888);
				}
			} 
			while(decodeState == RunState.RUN_STATE_RUNNING)
			{

				if(FFMpegIF.Decoding(beCalledBitmap)<=0) {//����һ��
					//						if(FFMpegIF.Decoding(beCalledBitmap)<0){
					//							Log.e(tag,"����ʧ�ܣ�������");
					//							
					//							Thread.yield();
					//						}else{
					//							beCalledImageView.drawBecalledImage();
					//							waitForDisplay();
					//						}	

					//						if(beCalledImageView!=null){
					//							beCalledImageView.drawBecalledImage();
					//						}

				}else{  //����ɹ���ֱ�ӵ�ˢ������
					if(beCalledImageView!=null){
						beCalledImageView.drawBecalledImage();
						beCalledBitmap.recycle();
						beCalledBitmap = Bitmap.createBitmap(CAMERA_W, CAMERA_H, Bitmap.Config.ARGB_8888);						
					}

				}
			}	
			decodeState = RunState.RUN_STATE_IDLE;
		}

	}

	/**
	 * ������߳�
	 * 
	 * @author User
	 *
	 */
	class EncodeThread implements Runnable {
		Message 	msg;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(encodeState == RunState.RUN_STATE_READY) {
				String urlTo="rtp://"+connectIp+":"+port2;

				if(FFMpegIF.StartEncode(urlTo, CAMERA_W, CAMERA_H,10)<0) {
					Log.e(tag, "Start encode failed");
					FFMpegIF.StopEncode();
					return;
				}

				encodeState = RunState.RUN_STATE_RUNNING;
				doCalledImageView.startEncodeNow(true);
			} 
			Log.i(tag, "Start encode successed");

		}
	}
	@Override
	protected void onStart(){
		new Thread(){ 
			@Override
			public void run(){
				Log.e(tag,"connectType:"+connectType);

				if(connectType.equals("BECALLER")){
					int temp=port1;
					port1=port2;
					port2=temp;
					Log.e(tag,"�˿�ת�� connectType:"+connectType);
					Log.e(tag,port1+"  "+port2);
				}
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startVideoCall();

			}
		}.start();
		super.onStart();
	}


	public void stopVideoTalk(){

		FFMpegIF.DeInit();	

		beCalledImageView=null;
		doCalledImageView=null;

		decodeState = RunState.RUN_STATE_STOP;
		FFMpegIF.StopDecode();	

		FFMpegIF.StopEncode();
		encodeState = RunState.RUN_STATE_IDLE;


		FFMpegIF.Release();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		beCalledImageView=null;
		decodeState = RunState.RUN_STATE_STOP;

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		stopVideoTalk();
		super.onDestroy();

	}

}
