package com.csst.ir.control;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendCtrlThread extends Thread {
	String ip ;
	int motorCode;
	public SendCtrlThread( String ip,int motorCode) {
		this.ip=ip;
		this.motorCode=motorCode;
	}

	public void run() {
		// ����д�����߳���Ҫ���Ĺ���

		try {
			Socket socket = new Socket(ip, 7788);
			OutputStream os = socket.getOutputStream();// ��ȡ�ͻ��˵������
			System.out.println("��ʼ��ͻ��˽�������");
			switch (motorCode) {
			case 1:os.write(("01").getBytes());break;//w��
			case 2:os.write(("02").getBytes());break;//s��
			case 3:os.write(("03").getBytes());break;//a��
			case 4:os.write(("04").getBytes());break;//d��
			case 5:os.write(("05").getBytes());break;//ͣ
			case 6:os.write(("s").getBytes());break;//ȫ��
			case 7:os.write(("s1").getBytes());break;//�ٶ�1
			case 8:os.write(("s2").getBytes());break;//�ٶ�2
			case 9:os.write(("s3").getBytes());break;//�ٶ�3
			case 10:os.write(("L").getBytes());break;//���������
			case 11:os.write(("C").getBytes());break;//�л�����ͷ
			} 			
			System.out.println("������ͻ��˽�������");
			os.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
