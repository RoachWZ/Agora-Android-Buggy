package com.csst.ir.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.hardware.ConsumerIrManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import hlq.service.SendSocketService;

public class ReviceCtrlThread extends Thread {



	/*2019-03-03
	 * 有问题，此处安卓程序，和小车单片机程序的数据码不一样，但能对应执行
	 * 单片机 0x76 3 5 4
	 * 安卓     0x12 8 6 4
	 *
	 * 可能和接收有关系，只有反置了之后才能接收正常
	 *
	 *2019-05-13
	 * 红外接收已解决，是单片机端红外接收程序的问题
	 * 单片机 0x12 8 4 6
	 * 安卓     0x12 8 4 6
	 * */
/*（红外遥控已废弃，改为蓝牙连接）
	//下面的数组是一种交替的载波序列模式，通过毫秒测量
	//引导码，地址码，地址码，数据码，数据反码
	//第三行数据码反置，比如0x12=0001 0010反置为 0100 1000


	//停止 0x10
	int[] patternS = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 560, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//前进 0x12
	int[] pattern1 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560,1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//后退 0x18
	int[] pattern2 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//左转 0x14
	int[] pattern3 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//右转 0x16
	int[] pattern4 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//左自转 0x17
	int[] pattern5 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 1690,	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//右自转 0x19
	int[] pattern6 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 1690,	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560,1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//全速  0x01
	int[] speed = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560,1690,	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560,  560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度1 0x00
	int[] speed1 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度2 0x02
	int[] speed2 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 560,	560, 1690, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };
	//速度3 0x03
	int[] speed3 = { 9000, 4500,
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
			560, 1690,	560, 1690, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690,
			560, 42020, 9000, 2250, 560, 98190 };


	private int hz = 38000; */
	private ConsumerIrManager mCIR;

	private Handler handler;
	private boolean LightFlag=false;

	public ReviceCtrlThread( ConsumerIrManager mCIR,Handler handler) {
		this.mCIR = mCIR;
		this.handler=handler;
	}

	public void run() {
		// 这里写入子线程需要做的工作

		ServerSocket ss=null;
		Socket socket = null;

		while (true) {
			try {
				ss = new ServerSocket(7788);
				socket = ss.accept();
				// 接受服务器的信息
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String mstr = br.readLine();
				if (mstr.equals("05")) {
					//mCIR.transmit(hz, patternS);//停
					SendSocketService.sendMessage("ONF");
				} else if (mstr.equals("01")) {
					//mCIR.transmit(hz, pattern1);//前
					SendSocketService.sendMessage("ONA");
				} else if (mstr.equals("02")) {
					//mCIR.transmit(hz, pattern2);//后
					SendSocketService.sendMessage("ONB");
				} else if (mstr.equals("03")) {
					//mCIR.transmit(hz, pattern3);//左
					SendSocketService.sendMessage("ONC");
				} else if (mstr.equals("04")) {
					//mCIR.transmit(hz, pattern4);//右
					SendSocketService.sendMessage("OND");
				} else if (mstr.equals("13")) {
					//mCIR.transmit(hz, pattern5);//左自转 rc_car前后停止
					SendSocketService.sendMessage("ONR");
				} else if (mstr.equals("14")) {
					//mCIR.transmit(hz, pattern6);//右自转 rc_car左右停止
					SendSocketService.sendMessage("ONL");
				} else if (mstr.equals("s")) {
					//mCIR.transmit(hz, speed);//全速
				} else if (mstr.equals("s1")) {
					//mCIR.transmit(hz, speed1);//速度1
				} else if (mstr.equals("s2")) {
					//mCIR.transmit(hz, speed2);//速度2
				} else if (mstr.equals("s3")) {
					//mCIR.transmit(hz, speed3);//速度3
				} else if (mstr.equals("L")) {
					lightSwitch();				//开关闪关灯
				} else if (mstr.equals("C")) {
					cameraSwitch();			//后置摄像头
				}else if (mstr.startsWith("c")) {
					SendSocketService.sendMessage(mstr);
				} else {
					System.out.println("数据错误");
				}

				br.close();
				socket.close();
				ss.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("ctrlSocket", e.toString());
			}
		}

	}

	private void cameraSwitch() {
		Message msg = new Message();
		msg.what=1;//切换摄像头
		handler.sendMessage(msg);
	}

	private void lightSwitch() {
		Message msg = new Message();
		if(!LightFlag){
			msg.what=2;//开灯
			LightFlag=true;
		}else{
			msg.what=3;//关灯
			LightFlag=false;
		}
		handler.sendMessage(msg);
	}

}
