package com.csst.videotalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.ConsumerIrManager;
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
import android.widget.TextView;

import com.csst.ffmpeg.FFMpegIF;
import com.csst.ffmpeg.views.BeCalledImageView;
import com.csst.ffmpeg.views.DoCalledImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hlq.base.bean.MessageBean;
import hlq.base.constant.BltContant;
import hlq.service.ReceiveSocketService;
import hlq.utils.ToastUtil;
import hlq.utils.factory.ThreadPoolProxyFactory;
import wz.customComponents.SeekBarViewHorizontal;
import wz.customComponents.SeekBarViewVertical;
import io.agora.tutorials1v1vcall.R;
import wz.control.ReviceCtrlThread;
import wz.control.SendCtrlThread;
import wz.control.SendMsgThread;

/**
 * 可视对讲
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

	private final int beCalledImageDrawRate=100;    //被叫的影响的刷新间隔时间，单位 毫秒

	private String tag = "ffmpegdemo";

	/*设定录像的像素大小*/
	public  static int CAMERA_W = 640;
	public  static int CAMERA_H = 480;
	public  static int cameraId = 0;

	public  int remote_w;
	public  int remote_h;


	private  int port1=5444;    //主叫方使用port1 进行编码通信端口,port2进行解码通信端口
	private  int port2=5446;    //被叫方使用port1 进行解码通信端口,port2进行编码通信端口
	private String connectIp;
	private String connectType;


	/*控件变量声明*/
	//	private ImageButton btn_photo;
	private ImageButton btn_stop;
	private Button btn_SeekBarStyle;

	public   static Bitmap beCalledBitmap;

	private BeCalledImageView beCalledImageView;

	//	private ImageView beCalledImageView;


	private DoCalledImageView doCalledImageView;

	private RunState decodeState;
	private RunState encodeState;

	//    public static int[] colors;

	private static final String TAG = "ConsumerIrTest";
	// Android4.4之后 红外遥控ConsumerIrManager
	private ConsumerIrManager mCIR;
	/**
	 * 控制命令接收线程
	 */
	private Thread ctrl;

	private static final int CAMERA_CHANGE=1;
	private static final int CHANGE_LIGHT_ON=2;
	private static final int CHANGE_LIGHT_OFF=3;
	private static final int RXD_MSG_ON=4;


	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==CAMERA_CHANGE){
				doCalledImageView.destroyCamera();
				cameraId = 1 - cameraId;
				doCalledImageView.initCamera();
			}else if(msg.what==CHANGE_LIGHT_ON){
				Camera.Parameters parameters = doCalledImageView.myCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				doCalledImageView.myCamera.setParameters(parameters);
			}else if(msg.what==CHANGE_LIGHT_OFF){
				Camera.Parameters parameters = doCalledImageView.myCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				doCalledImageView.myCamera.setParameters(parameters);
			}else if(msg.what==RXD_MSG_ON){
				RXDmsg = msg.getData().getString("RXD_MSG");
				previewSVtext.setText("speed: "+ speed +"\n"+
									"T: "+ TXDmsg +"\n" +
									"R: "+ RXDmsg +"\n");
			}

		};
	};

	private SeekBarViewHorizontal mSeekBarHorizontal_L;
	private SeekBarViewVertical mSeekBarVertical_L;//左边的
	private SeekBarViewHorizontal mSeekBarHorizontal_R;
	private SeekBarViewVertical mSeekBarVertical_R;//右边的
	private SeekBarViewHorizontal mSeekBarVertical_Speeed;//速度设置

	private boolean center = false;
	private boolean isVertical = true;

	private int speed = 50;// 范围 0-255
	private String TXDmsg = "";// 数据发送
	private String RXDmsg = "";// 数据接受

	private Context mContext;
	private TextView previewSVtext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_talk);
		mContext = VideoTalkActivity.this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
		decodeState = RunState.RUN_STATE_IDLE;
		encodeState = RunState.RUN_STATE_IDLE;

		//获取解码显示区
		beCalledImageView=(BeCalledImageView)findViewById(R.id.imgview2);//对方画面
		doCalledImageView=(DoCalledImageView)findViewById(R.id.previewSV);//自己画面
		Log.i(tag, "ffmpeg version is " + FFMpegIF.GetFFmpegVer());
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		connectIp=bundle.getString("CONNECTIP");
		connectType=bundle.getString("CONNECTTYPE");


		// 获取系统的红外遥控服务
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

		initViewsAndEvents();//遥控按钮监听初始化
		previewSVtext = (TextView) findViewById(R.id.previewSVtext);
		btn_SeekBarStyle = (Button)findViewById(R.id.SeekBarStyle);//拖动条的 水平/垂直 样式切换按钮
		btn_stop = (ImageButton)findViewById(R.id.btn_stop);//挂断按钮
		ButtonListenser btnListener = new ButtonListenser();
		btn_stop.setOnClickListener(btnListener);

		//控制命令接收线程
		ctrl = new ReviceCtrlThread(mCIR,handler);
		ctrl.start();

	}


	/**
	 * 遥控按钮监听初始化
	 */
	private void initViewsAndEvents() {
		
		mSeekBarVertical_L = findViewById(R.id.seekbarVertical_l);//左边的拖动条
		mSeekBarVertical_L.setCenterModeEnable(true);
		mSeekBarVertical_L.setAutoZeroEnable(true);
		mSeekBarVertical_L.setOnTouchListener(mClickListener);
		
		mSeekBarVertical_R = findViewById(R.id.seekbarVertical_r);//右边的拖动条
		mSeekBarVertical_R.setCenterModeEnable(true);
		mSeekBarVertical_R.setAutoZeroEnable(true);
		mSeekBarVertical_R.setOnTouchListener(mClickListener);

		mSeekBarVertical_Speeed = findViewById(R.id.seekbarVertical_speed);//速度的拖动条
		mSeekBarVertical_Speeed.setProgress(speed);
		mSeekBarVertical_Speeed.setOnTouchListener(mClickListener);

		findViewById(R.id.send_button_Front).setOnTouchListener(mClickListener);//前进按钮
		findViewById(R.id.send_button_Back).setOnTouchListener(mClickListener);//后退按钮
		findViewById(R.id.send_button_Left).setOnTouchListener(mClickListener);//左拐
		findViewById(R.id.send_button_Right).setOnTouchListener(mClickListener);//右拐

		findViewById(R.id.send_button_L).setOnClickListener(mOnClickListener);//开关闪关灯
		findViewById(R.id.send_button_C).setOnClickListener(mOnClickListener);//切换摄像头

	}


	View.OnTouchListener mClickListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			//左右拖动条的监听
			if(v.getId() == R.id.seekbarVertical_l || v.getId() == R.id.seekbarVertical_r){
				if(action==MotionEvent.ACTION_DOWN){
					TXDmsg = "c0,0"+"\n";
					new SendMsgThread(connectIp, TXDmsg).start();
				}else if (action == MotionEvent.ACTION_MOVE) {
					TXDmsg = "c"+(-(mSeekBarVertical_L.getProgress()*speed/100))+
							","+(-(mSeekBarVertical_R.getProgress()*speed/100))+"\n";
					new SendMsgThread(connectIp, TXDmsg).start();

				}else if (action == MotionEvent.ACTION_UP) {
					TXDmsg = "c0,0"+"\n";
					new SendMsgThread(connectIp, TXDmsg).start();
				}
			//速度拖动条的监听	
			}else if(v.getId() == R.id.seekbarVertical_speed || action == MotionEvent.ACTION_MOVE){
				speed = mSeekBarVertical_Speeed.getProgress()*255/100;
				
			}
			previewSVtext.setText("speed: "+ speed +"\n"+
								"T: "+ TXDmsg +
								"R: "+ RXDmsg );
			//按钮的监听
			if(action==MotionEvent.ACTION_DOWN){
				switch (v.getId()) {
					case R.id.send_button_Front:
						//new SendCtrlThread(connectIp, 1).start();break;
						mSeekBarVertical_R.setProgress(-100);
						TXDmsg = "c"+(-(mSeekBarVertical_L.getProgress()*speed/100))+
								","+speed+"\n";
						new SendMsgThread(connectIp, TXDmsg).start();
						break;
					case R.id.send_button_Back:
						//new SendCtrlThread(connectIp, 2).start();break;
						mSeekBarVertical_R.setProgress(100);
						TXDmsg = "c"+(-(mSeekBarVertical_L.getProgress()*speed/100))+
								","+(-speed)+"\n";
						new SendMsgThread(connectIp, TXDmsg).start();
						break;
					case R.id.send_button_Left:
						//new SendCtrlThread(connectIp, 3).start();break;
						mSeekBarVertical_L.setProgress(-100);
						TXDmsg = "c"+255+
								","+(-(mSeekBarVertical_R.getProgress()*speed/100))+"\n";						new SendMsgThread(connectIp, TXDmsg).start();
						break;
					case R.id.send_button_Right:
						//new SendCtrlThread(connectIp, 4).start();break;
						mSeekBarVertical_L.setProgress(100);
						TXDmsg = "c"+(-255)+
								","+(-(mSeekBarVertical_R.getProgress()*speed/100))+"\n";
						new SendMsgThread(connectIp, TXDmsg).start();
						break;
				}
			}else if (action == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
					case R.id.send_button_Front:
					case R.id.send_button_Back:
						//new SendCtrlThread(connectIp, 13).start();
						mSeekBarVertical_R.setProgress(0);
						TXDmsg = "c"+(-(mSeekBarVertical_L.getProgress()*speed/100))+",0\n";
						new SendMsgThread(connectIp, TXDmsg).start();
						break;//前后停止
					case R.id.send_button_Left:
					case R.id.send_button_Right:
						//new SendCtrlThread(connectIp, 14).start();
						mSeekBarVertical_L.setProgress(0);
						TXDmsg = "c0,"+(-(mSeekBarVertical_R.getProgress()*speed/100))+"\n";
						new SendMsgThread(connectIp, TXDmsg).start();
						break;//左右停止
				}
			}

			return false;
		}
	};


	View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
				case R.id.send_button_L:
					new SendCtrlThread(connectIp, 10).start();//开关闪光灯
					Button button = (Button) v;
					if (button.isSelected()) {
						button.setSelected(false);
						button.getBackground().clearColorFilter();//清除颜色过滤
					} else {
						button.setSelected(true);//选中的话 设置颜色过滤 颜色变深
						button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
					}
					break;
				case R.id.send_button_C:
					new SendCtrlThread(connectIp, 11).start();//切换摄像头
					break;

			}
		}

	};
/*
	View.OnTouchListener mSend1ClickListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action=event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				new SendCtrlThread(connectIp, 1).start();

			}else if (action == MotionEvent.ACTION_UP) {
				new SendCtrlThread(connectIp, 13).start();
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
				new SendCtrlThread(connectIp, 13).start();
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
				new SendCtrlThread(connectIp, 14).start();
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
				new SendCtrlThread(connectIp, 14).start();
			}
			return false;
		}
	};
*/

	public void onSpeed4Clicked(View view) {
		Button button = (Button) view;
		if (button.isSelected()) {
			button.setSelected(false);
			button.getBackground().clearColorFilter();
			speed = 50;
		} else {
			button.setSelected(true);
			button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
			speed = 255;
		}
	}

	public void onSpeed3Clicked(View view) {
		Button button = (Button) view;
		if (button.isSelected()) {
			button.setSelected(false);
			button.getBackground().clearColorFilter();
			speed = 50;
		} else {
			button.setSelected(true);
			button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
			speed = 155;
		}
	}
	public void onSpeed2Clicked(View view) {
		Button button = (Button) view;
		if (button.isSelected()) {
			button.setSelected(false);
			button.getBackground().clearColorFilter();
			speed = 50;
		} else {
			button.setSelected(true);
			button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
			speed = 105;
		}
	}

	/**
	 * 拖动条的 水平/垂直 样式切换
	 */
	public void onSeekBarModeClicked(View view) {
//		center = !center;//拖动条起始位置 true中间开始 false侧边开始
//		mSeekBarVertical_L.setCenterModeEnable(center);
//		mSeekBarVertical_R.setCenterModeEnable(center);

		isVertical = !isVertical;
		mSeekBarVertical_L.setVerticalModeEnable(isVertical);
		if(isVertical){
			btn_SeekBarStyle.setText("vertical");
		}else{
			btn_SeekBarStyle.setText("horizontal");
		}

	}
	
	
	public void startVideoCall(){
		FFMpegIF.Init();

		//启动编码的线程
		if(encodeState == RunState.RUN_STATE_IDLE) {
			//启动线程，在线程中启动编码
			Log.i(tag, "start encode thread");
			encodeState = RunState.RUN_STATE_READY;
			//create decode thread and run
			EncodeThread encthread = new EncodeThread();
			new Thread(encthread).start();
		}

		//启动解码的线程
		if(decodeState == RunState.RUN_STATE_IDLE) {
			//启动线程，在线程中启动解码，因为解码会一直等待数据，如果不用线程，则程序会阻塞在ffmpegif.StartDecode()中
			Log.i(tag, "start decode thread");
			decodeState = RunState.RUN_STATE_READY;
			//create decode thread and run
			DecodeThread decThread = new DecodeThread();
			new Thread(decThread).start();
		}
	}

	/**
	 * 按键监听部分
	 *
	 * @author User
	 *
	 */
	class ButtonListenser implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//			if(v.getId() == R.id.btn_photo) {   //给对方拍照
			//
			//
			//			}
			//停止编解码线程
			if(v.getId() == R.id.btn_stop)
			{
				stopVideoTalk();

				//还应该告诉对方停止视频了，我都停止了

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
	 * 睡眠等待展示，待优化
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
	 * 解码线程
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

				/*获取接收到的视频resolution，以设置bitmap的参数*/
				do {
					remote_w = FFMpegIF.GetWidth();
					remote_h = FFMpegIF.GetHeight();
					try{
						Thread.sleep(beCalledImageDrawRate); //让当前线程休眠100毫秒
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

				if(FFMpegIF.Decoding(beCalledBitmap)<=0) {//再试一次
					//						if(FFMpegIF.Decoding(beCalledBitmap)<0){
					//							Log.e(tag,"还是失败？？？？");
					//
					//							Thread.yield();
					//						}else{
					//							beCalledImageView.drawBecalledImage();
					//							waitForDisplay();
					//						}

					//						if(beCalledImageView!=null){
					//							beCalledImageView.drawBecalledImage();
					//						}

				}else{  //解码成功，直接的刷新数据
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
	 * 编码的线程
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
					Log.e(tag,"端口转换 connectType:"+connectType);
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
		super.onStop();
		beCalledImageView = null;
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
