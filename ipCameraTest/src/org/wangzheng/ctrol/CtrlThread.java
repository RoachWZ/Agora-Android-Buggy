package org.wangzheng.ctrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import android.hardware.ConsumerIrManager;
import android.util.Log;

public class CtrlThread extends Thread {

	private String ipname;
	private ConsumerIrManager mCIR;

	/*
	 * �����⣬�˴���׿���򣬺�С����Ƭ������������벻һ�������ܶ�Ӧִ��
	 * ��Ƭ�� 0x76 3 5 4
	 * ��׿     0x12 8 6 4
	 * �����������뷴�ã�����0x12=0001 0010����Ϊ 0100 1000
	 * ���ܺͽ����й�ϵ��ֻ�з�����֮����ܽ�������
	 * */
	// һ�ֽ�����ز�����ģʽ��ͨ���������
	//�����룬��ַ�룬��ַ�룬�����룬���ݷ���
	
	//0x76	0111 0110
	int[] pattern1 = { 9000, 4500, 
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0100 1000*/560, 560,	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560,1690, 	560, 1690, 	560, 1690, 
			560, 42020, 9000, 2250, 560, 98190 };
	//0x73
	int[] pattern2 = { 9000, 4500, 
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 
/*0001 1000*/560, 560,	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 
			560, 42020, 9000, 2250, 560, 98190 };
	//0x75
	int[] pattern3 = { 9000, 4500, 
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690,
/*0110 1000*/560, 560,	560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560, 	
			560, 1690, 	560, 560, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690,	560, 1690, 	560, 1690, 
			560, 42020, 9000, 2250, 560, 98190 };
	//0x74
	int[] pattern4 = { 9000, 4500, 
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 
/*0010 1000*/560, 560,	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 
			560, 42020, 9000, 2250, 560, 98190 };
	//0x74
	int[] pattern5 = { 9000, 4500, 
			560, 560, 	560, 560, 	560, 560, 	560, 560, 	560,560, 	560, 560, 	560, 560, 	560, 560,
			560, 1690, 	560, 1690, 	560, 1690,	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 	560, 1690, 
/*1101 1000*/560,1690,	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 560, 	560, 560, 	560, 560, 
			560, 1690, 	560, 1690, 	560, 560, 	560, 1690, 	560, 560, 	560, 1690, 	560, 1690, 	560, 1690, 
			560, 42020, 9000, 2250, 560, 98190 };

	private int hz = 38000;

	public CtrlThread(String ipname, ConsumerIrManager mCIR) {
		this.ipname = ipname;
		this.mCIR = mCIR;
	}

	public void run() {
		// ����д�����߳���Ҫ���Ĺ���

		Socket socket = null;

		while (true) {
			try {
				socket = new Socket(ipname, 7788);
				// ���ܷ���������Ϣ
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String mstr = br.readLine();
				if (mstr.equals("01")) {

					{
						System.out.println("----------recive--ctrl---ok");
						mCIR.transmit(hz, pattern1);//ǰ
						System.out.println("----------IR>>>send1 ok");
					}
				} else if (mstr.equals("02")) {

					{
						System.out.println("----------recive--ctrl---ok");
						mCIR.transmit(hz, pattern2);//��
						System.out.println("----------IR>>>send2 ok");
					}
				} else if (mstr.equals("03")) {

					{
						System.out.println("----------recive--ctrl---ok");
						mCIR.transmit(hz, pattern3);//��
						System.out.println("----------IR>>>send3 ok");
					}
				} else if (mstr.equals("04")) {

					{
						System.out.println("----------recive--ctrl---ok");
						mCIR.transmit(hz, pattern4);//��
						System.out.println("----------IR>>>send4 ok");
					}
				} else if (mstr.equals("05")) {
					
					{
						System.out.println("----------recive--ctrl---ok");
						mCIR.transmit(hz, pattern5);//ֹͣ
						System.out.println("----------IR>>>send5 ok");
					}
				} else {
					System.out.println("���ݴ���");
				}

				br.close();
				socket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("ctrlSocket", e.toString());
			}
		}

	}

}
