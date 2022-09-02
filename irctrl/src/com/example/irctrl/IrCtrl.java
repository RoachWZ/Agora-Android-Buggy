package com.example.irctrl;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.hardware.ConsumerIrManager;
public class IrCtrl extends Activity {

	
		 
	/**
	 * Android������ң�عٷ�Demo
	 * 
	 * @description��
	 * @author ldm
	 * @date 2016-4-28 ����5:06:28
	 */
	    private static final String TAG = "ConsumerIrTest";
	    
	    // Android4.4֮�� ����ң��ConsumerIrManager�����Ա�С��4����
	    private ConsumerIrManager mCIR;

	    @SuppressLint("InlinedApi")
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        // ��ȡϵͳ�ĺ���ң�ط���
	        mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
	        initViewsAndEvents();
	    }

	    private void initViewsAndEvents() {
	        findViewById(R.id.send_button1).setOnClickListener(mSend1ClickListener);
	        findViewById(R.id.send_button2).setOnClickListener(mSend2ClickListener);
	        findViewById(R.id.send_button3).setOnClickListener(mSend3ClickListener);
	        findViewById(R.id.send_button4).setOnClickListener(mSend4ClickListener);
	        
	    }

	    View.OnClickListener mSend1ClickListener = new View.OnClickListener() {
	        @TargetApi(Build.VERSION_CODES.KITKAT)
	        public void onClick(View v) {
	            if (!mCIR.hasIrEmitter()) {
	                Log.e(TAG, "δ�ҵ����ⷢ������");
	                return;
	            }

	            // һ�ֽ�����ز�����ģʽ��ͨ���������
	          int[] pattern = { 9000, 4500,
	            560, 560, 560, 560, 560, 560, 560, 560,		 560, 560, 560, 560, 560, 560, 560, 560, 
	            560,1690, 560,1690, 560,1690, 560,1690,		 560,1690, 560,1690, 560,1690, 560,1690,
	            560, 560, 560,1690, 560, 560, 560, 560,		 560,1690, 560, 560, 560, 560, 560, 560, 
	            560,1690, 560, 560, 560,1690, 560,1690,		 560, 560, 560,1690, 560,1690, 560,1690, 
	            560,42020, 9000,2250,560,98190 };
	            
	            // ��38KHz�����½���ģʽת��
	            mCIR.transmit(38000, pattern);
	        }
	    };
	    View.OnClickListener mSend2ClickListener = new View.OnClickListener() {
	        @TargetApi(Build.VERSION_CODES.KITKAT)
	        public void onClick(View v) {
	            if (!mCIR.hasIrEmitter()) {
	                Log.e(TAG, "δ�ҵ����ⷢ������");
	                return;
	            }

	            int[] pattern = { 9000, 4500,
	    	            560, 560, 560, 560, 560, 560, 560, 560,		 560, 560, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560,1690, 560,1690, 560,1690,		 560,1690, 560,1690, 560,1690, 560,1690,
	    	            560, 560, 560, 560, 560, 560, 560,1690,		 560,1690, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560,1690, 560,1690, 560, 560,		 560, 560, 560,1690, 560,1690, 560,1690, 
	    	            560,42020, 9000,2250,560,98190 };
	    	            
	            mCIR.transmit(38000, pattern);
	        }
	    };

	    View.OnClickListener mSend3ClickListener = new View.OnClickListener() {
	        @TargetApi(Build.VERSION_CODES.KITKAT)
	        public void onClick(View v) {
	            if (!mCIR.hasIrEmitter()) {
	                Log.e(TAG, "δ�ҵ����ⷢ������");
	                return;
	            }

	            // һ�ֽ�����ز�����ģʽ��ͨ���������
	            int[] pattern = { 9000, 4500,
	    	            560, 560, 560, 560, 560, 560, 560, 560,		 560, 560, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560,1690, 560,1690, 560,1690,		 560,1690, 560,1690, 560,1690, 560,1690,
	    	            560, 560, 560,1690, 560,1690, 560, 560,		 560,1690, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560, 560, 560, 560, 560,1690,		 560, 560, 560,1690, 560,1690, 560,1690, 
	    	            560,42020, 9000,2250,560,98190 };
	          
	            // ��38KHz�����½���ģʽת��
	            mCIR.transmit(38000, pattern);
	        }
	    };
	    View.OnClickListener mSend4ClickListener = new View.OnClickListener() {
	        @TargetApi(Build.VERSION_CODES.KITKAT)
	        public void onClick(View v) {
	            if (!mCIR.hasIrEmitter()) {
	                Log.e(TAG, "δ�ҵ����ⷢ������");
	                return;
	            }

	            int[] pattern = { 9000, 4500,
	    	            560, 560, 560, 560, 560, 560, 560, 560,		 560, 560, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560,1690, 560,1690, 560,1690,		 560,1690, 560,1690, 560,1690, 560,1690,
	    	            560, 560, 560, 560, 560,1690, 560, 560,		 560,1690, 560, 560, 560, 560, 560, 560, 
	    	            560,1690, 560,1690, 560, 560, 560,1690,		 560, 560, 560,1690, 560,1690, 560,1690, 
	    	            560,42020, 9000,2250,560,98190 };
	    	            
	    	            
	            mCIR.transmit(38000, pattern);
	        }
	    };
	    
	}
