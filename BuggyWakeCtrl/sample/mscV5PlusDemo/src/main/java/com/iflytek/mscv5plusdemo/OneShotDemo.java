package com.iflytek.mscv5plusdemo;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.iflytek.speech.util.JsonParser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.hardware.ConsumerIrManager;
import android.media.MediaPlayer;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class OneShotDemo extends Activity implements OnClickListener{
	//停止 0x10
	public static int[] patternS = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0000 1000*/560, 560,	560, 560, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//前进 0x12
	public static int[] pattern1 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0100 1000*/560, 560,	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560,1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//后退 0x18
	public static int[] pattern2 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0001 1000*/560, 560,	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//左转 0x14
	public static int[] pattern3 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0010 1000*/560, 560,	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//右转 0x16
	public static int[] pattern4 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0110 1000*/560, 560,	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//左自转 0x17
	public static int[] pattern5 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*1110 1000*/560, 1690,	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//右自转 0x19
	public static int[] pattern6 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*1001 1000*/560, 1690,	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560,1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//全速  0x01
	public static int[] speed = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			/*1000 0000*/560,1690,	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560,  560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度1 0x00
	public static int[] speed1 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			/*0000 0000*/560, 560,	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度2 0x02
	public static int[] speed2 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			/*0100 0000*/560, 560,	560, 1690, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度3 0x03
	public static int[] speed3 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			/*1100 0000*/560, 1690,	560, 1690, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };

	public static int delay = 1000;//3秒
	public static int hz = 38000;
	public static ConsumerIrManager mCIR;

	public static void sendIR(String cmd){
		String mstr = cmd;
//		Timer timer = new Timer();

		if (mstr.equals("05")) {
			mCIR.transmit(hz, patternS);//停
		} else if(mstr.equals("01")||mstr.equals("02")||mstr.equals("03")||mstr.equals("04")){
			if (mstr.equals("01")) {
				mCIR.transmit(hz, pattern1);//前
//				timer.schedule(task, delay);//n秒后执行TimeTask的run方法
				textView.setText("前进ok");
			} else if (mstr.equals("02")) {
				mCIR.transmit(hz, pattern2);//后
//				timer.schedule(task, delay);
				textView.setText("后退ok");
			} else if (mstr.equals("03")) {
				mCIR.transmit(hz, pattern3);//左
//				timer.schedule(task, delay);
				textView.setText("左转ok");
			} else if (mstr.equals("04")) {
				mCIR.transmit(hz, pattern4);//右
//				timer.schedule(task, delay);
				textView.setText("右转ok");
			}
			new Thread() {
				@Override
				public void run() {
					super.run();
					try {
						Thread.sleep(1000);//休眠3秒
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					/**
					 * 要执行的操作
					 */
					sendIR("05");
				}
			}.start();
		}else if (mstr.equals("13")) {
			mCIR.transmit(hz, pattern5);//左自转
		} else if (mstr.equals("14")) {
			mCIR.transmit(hz, pattern6);//右自转
		} else if (mstr.equals("s")) {
			mCIR.transmit(hz, speed);//全速
		} else if (mstr.equals("s1")) {
			mCIR.transmit(hz, speed1);//速度1
		} else if (mstr.equals("s2")) {
			mCIR.transmit(hz, speed2);//速度2
		} else if (mstr.equals("s3")) {
			mCIR.transmit(hz, speed3);//速度3
		} else if (mstr.equals("L")) {
//			lightSwitch();				//开关闪关灯
		} else if (mstr.equals("C")) {
//			cameraSwitch();			//后置摄像头
		} else {
			System.out.println("数据错误");
		}

	}

//	public static TimerTask task = new TimerTask() {
//		@Override
//		public void run() {
//			/**
//			 *要执行的操作
//			 */
//			sendIR("05");
//		}
//	};

	public static void judge(String word){
		if(word.equals("前进")||word.equals("往前进")||word.equals("向前进")||word.indexOf("前进")>=0){
			sendIR("01");
			Log.d("sendIR", "前进");

		}else if(word.equals("后退")||word.equals("往后退")||word.equals("向后退")||word.indexOf("后退")>=0){
			sendIR("02");
			Log.d("sendIR", "后退");

		}else if(word.equals("左转")||word.equals("往左转")||word.equals("向左转")||word.indexOf("左转")>=0){
			sendIR("03");
			Log.d("sendIR", "左转");

		}else if(word.equals("右转")||word.equals("往右转")||word.equals("向右转")||word.indexOf("右转")>=0){
			sendIR("04");
			Log.d("sendIR", "右转");

		}


	}
	private String TAG = "ivw";
	private Toast mToast;
	private static TextView textView;
	// 语音唤醒对象
	private VoiceWakeuper mIvw;
	// 语音识别对象
	private SpeechRecognizer mAsr;
	// 唤醒结果内容
	private String resultString;
	// 识别结果内容
	private String recoString;
	// 设置门限值 ： 门限值越低越容易被唤醒
	private TextView tvThresh;
	private SeekBar seekbarThresh;
	private final static int MAX = 3000;
	private final static int MIN = 0;
	private int curThresh = 1450;
	private String threshStr = "门限值：";
	// 云端语法文件
	private String mCloudGrammar = null;	
	// 云端语法id
	private String mCloudGrammarID;
	// 本地语法id
	private String mLocalGrammarID;
	// 本地语法文件
	private String mLocalGrammar = null;
	// 本地语法构建路径	
	private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/msc/test";
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	private MediaPlayer mediaPlayer;//MediaPlayer对象

	private boolean isPause=false;//是否暂停

	private File file;//要播放的文件
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.oneshot_activity);

		// 获取系统的红外遥控服务
//		JsonParser.mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

		initUI();
//		play();


		// 初始化唤醒对象
		mIvw = VoiceWakeuper.createWakeuper(this, null);
		// 初始化识别对象---唤醒+识别,用来构建语法
		mAsr = SpeechRecognizer.createRecognizer(this, null);
		// 初始化语法文件
		mCloudGrammar = readFile(this, "wake_grammar_sample.abnf", "utf-8");
		mLocalGrammar = readFile(this, "wake.bnf", "utf-8");
		start_grammar();
	}



	private void initUI() {
		findViewById(R.id.btn_oneshot).setOnClickListener(OneShotDemo.this);
		findViewById(R.id.btn_stop).setOnClickListener(OneShotDemo.this);
		findViewById(R.id.btn_grammar).setOnClickListener(OneShotDemo.this);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		textView = (TextView) findViewById(R.id.txt_show_msg);
		tvThresh = (TextView)findViewById(R.id.txt_thresh);

		seekbarThresh = (SeekBar)findViewById(R.id.seekBar_thresh);
		seekbarThresh.setMax(MAX - MIN);
		seekbarThresh.setProgress(curThresh);
		tvThresh.setText(threshStr + curThresh);

		seekbarThresh.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				curThresh = seekbarThresh.getProgress() + MIN;
				tvThresh.setText(threshStr + curThresh);
			}
		});
		//选择云端or本地
		RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioCloud) {
					mEngineType = SpeechConstant.TYPE_CLOUD;
				} else if (checkedId == R.id.radioLocal) {
					mEngineType = SpeechConstant.TYPE_LOCAL;
				}
			}
		});
	}



void start_grammar(){
	int ret = 0;
	if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
		// 设置参数
		mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		// 开始构建语法
		ret = mAsr.buildGrammar("abnf", mCloudGrammar, grammarListener);
		if (ret != ErrorCode.SUCCESS) {
			showTip("语法构建失败,错误码：" + ret+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
			start_grammar();
		}
	} else {
		mAsr.setParameter(SpeechConstant.PARAMS, null);
		mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		// 设置引擎类型
		mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置语法构建路径
		mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
		// 设置资源路径
		mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
		ret = mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener);
		if (ret != ErrorCode.SUCCESS) {
			showTip("语法构建失败,错误码：" + ret+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
			start_grammar();
		}
	}
}
void start_oneshot(){
	{
//		while (true)
		{


//			case R.id.btn_grammar:

//				break;

//			case R.id.btn_oneshot:
			// 非空判断，防止因空指针使程序崩溃
			mIvw = VoiceWakeuper.getWakeuper();
			if (mIvw != null) {
				resultString = "";
				recoString = "";
				textView.setText(resultString);

				final String resPath = ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets, "ivw/"+getString(R.string.app_id)+".jet");
				// 清空参数
				mIvw.setParameter(SpeechConstant.PARAMS, null);
				// 设置识别引擎
				mIvw.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// 设置唤醒资源路径
				mIvw.setParameter(ResourceUtil.IVW_RES_PATH, resPath);
				/**
				 * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
				 * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
				 */
				mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"
						+ curThresh);
				// 设置唤醒+识别模式
				mIvw.setParameter(SpeechConstant.IVW_SST, "oneshot");
				// 设置返回结果格式
				mIvw.setParameter(SpeechConstant.RESULT_TYPE, "json");
//
//				mIvw.setParameter(SpeechConstant.IVW_SHOT_WORD, "0");

				// 设置唤醒录音保存路径，保存最近一分钟的音频
				mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
				mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );

				if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
					if (!TextUtils.isEmpty(mCloudGrammarID)) {
						// 设置云端识别使用的语法id
						mIvw.setParameter(SpeechConstant.CLOUD_GRAMMAR,
								mCloudGrammarID);
						mIvw.startListening(mWakeuperListener);
					} else {
						showTip("请先构建语法");
					}
				} else {
					if (!TextUtils.isEmpty(mLocalGrammarID)) {
						// 设置本地识别资源
						mIvw.setParameter(ResourceUtil.ASR_RES_PATH,
								getResourcePath());
						// 设置语法构建路径
						mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
						// 设置本地识别使用语法id
						mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR,
								mLocalGrammarID);
						mIvw.startListening(mWakeuperListener);
					} else {
						showTip("请先构建语法");
					}
				}

			} else {
				showTip("唤醒未初始化");
//				continue;//
			}
//				break;


//			case R.id.btn_stop:
//				mIvw = VoiceWakeuper.getWakeuper();
//				if (mIvw != null) {
//					mIvw.stopListening();
//				} else {
//					showTip("唤醒未初始化");
//				}
//				break;

//			default:
//				break;


		}
	}
}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_oneshot:
			// 非空判断，防止因空指针使程序崩溃
			mIvw = VoiceWakeuper.getWakeuper();
			if (mIvw != null) {
				resultString = "";
				recoString = "";
				textView.setText(resultString);

				final String resPath = ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets, "ivw/"+getString(R.string.app_id)+".jet");
				// 清空参数
				mIvw.setParameter(SpeechConstant.PARAMS, null);
				// 设置识别引擎
				mIvw.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// 设置唤醒资源路径
				mIvw.setParameter(ResourceUtil.IVW_RES_PATH, resPath);
				/**
				 * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
				 * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
				 */
				mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"
						+ curThresh);
				// 设置唤醒+识别模式
				mIvw.setParameter(SpeechConstant.IVW_SST, "oneshot");
				// 设置返回结果格式
				mIvw.setParameter(SpeechConstant.RESULT_TYPE, "json");
//
//				mIvw.setParameter(SpeechConstant.IVW_SHOT_WORD, "0");

				// 设置唤醒录音保存路径，保存最近一分钟的音频
				mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
				mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );

				if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
					if (!TextUtils.isEmpty(mCloudGrammarID)) {
						// 设置云端识别使用的语法id
						mIvw.setParameter(SpeechConstant.CLOUD_GRAMMAR,
								mCloudGrammarID);
						mIvw.startListening(mWakeuperListener);
					} else {
						showTip("请先构建语法");
					}
				} else {
					if (!TextUtils.isEmpty(mLocalGrammarID)) {
						// 设置本地识别资源
						mIvw.setParameter(ResourceUtil.ASR_RES_PATH,
								getResourcePath());
						// 设置语法构建路径
						mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
						// 设置本地识别使用语法id
						mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR,
								mLocalGrammarID);
						mIvw.startListening(mWakeuperListener);
					} else {
						showTip("请先构建语法");
					}
				}

			} else {
				showTip("唤醒未初始化");
			}
			break;

		case R.id.btn_grammar:
			int ret = 0;
			if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
				// 设置参数
				mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
				// 开始构建语法
				ret = mAsr.buildGrammar("abnf", mCloudGrammar, grammarListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("语法构建失败,错误码：" + ret+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
				}
			} else {
				mAsr.setParameter(SpeechConstant.PARAMS, null);
				mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
				// 设置引擎类型
				mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// 设置语法构建路径
				mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
				// 设置资源路径
				mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
				ret = mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("语法构建失败,错误码：" + ret+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
				}
			}

			break;

		case R.id.btn_stop:
			mIvw = VoiceWakeuper.getWakeuper();
			if (mIvw != null) {
				mIvw.stopListening();
			} else {
				showTip("唤醒未初始化");
			}
			break;

		default:
			break;
		}
	}

	public void play(){
//			file=new File(Environment.getExternalStorageDirectory()+File.separator+ "raw/answer.mp3");

//			if(file.exists()){

		  mediaPlayer=new MediaPlayer();

		mediaPlayer=MediaPlayer.create(OneShotDemo.this,R.raw.answer);

//		try {

//                mediaPlayer.setDataSource(file.getAbsolutePath());

//			mediaPlayer.prepare();//预加载音频

			mediaPlayer.start();//播放音乐

//		}catch (Exception e) {
//
//			e.printStackTrace();
//
//		}
//				showTip("answer file exist");
//			}else{
//				showTip("answer file don't exist");
//			}
	}


	GrammarListener grammarListener = new GrammarListener() {
		@Override
		public void onBuildFinish(String grammarId, SpeechError error) {
			if (error == null) {
				if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
					mCloudGrammarID = grammarId;
				} else {
					mLocalGrammarID = grammarId;
				}
				showTip("语法构建成功：" + grammarId);
				start_oneshot();
			} else {
				showTip("语法构建失败,错误码：" + error.getErrorCode()+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
				start_grammar();
			}
		}
	};

	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			try {
				String text = result.getResultString();
				JSONObject object;
				object = new JSONObject(text);
				StringBuffer buffer = new StringBuffer();
				buffer.append("【RAW】 "+text);
				buffer.append("\n");
				buffer.append("【操作类型】"+ object.optString("sst"));
				buffer.append("\n");
				buffer.append("【唤醒词id】"+ object.optString("id"));
				buffer.append("\n");
				buffer.append("【得分】" + object.optString("score"));
				buffer.append("\n");
				buffer.append("【前端点】" + object.optString("bos"));
				buffer.append("\n");
				buffer.append("【尾端点】" + object.optString("eos"));
				resultString =buffer.toString();
			} catch (JSONException e) {
				resultString = "结果解析出错";
				e.printStackTrace();
			}
			textView.setText(resultString);

//			sendIR("01");
//			play();

		}


		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
			start_oneshot();
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
			Log.d(TAG, "eventType:"+eventType+ "arg1:"+isLast + "arg2:" + arg2);
			// 识别结果
			if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
				RecognizerResult reslut = ((RecognizerResult)obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
				recoString += JsonParser.parseGrammarResult(reslut.getResultString());
				textView.setText(recoString);
				judge(recoString);
//				String[] cmd = recoString.split("@");
//				judge(cmd[0]);
//				showTip(cmd[0]);

				play();
				start_oneshot();
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
			// TODO Auto-generated method stub
			
		}

	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy OneShotDemo");
		mIvw = VoiceWakeuper.getWakeuper();
		if (mIvw != null) {
			mIvw.destroy();
		} else {
			showTip("唤醒未初始化");
		}
		mediaPlayer.release();
		mediaPlayer = null;
	}
	
	/**
	 * 读取asset目录下文件。
	 * 
	 * @return content
	 */
	public static String readFile(Context mContext, String file, String code) {
		int len = 0;
		byte[] buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);
			len = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);

			result = new String(buf, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 获取识别资源路径
	private String getResourcePath() {
		StringBuffer tempBuffer = new StringBuffer();
		// 识别通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(this, 
				RESOURCE_TYPE.assets, "asr/common.jet"));
		return tempBuffer.toString();
	}
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
}
