package com.csst.ffmpeg.views;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.csst.ffmpeg.FFMpegIF;
import com.csst.videotalk.VideoTalkActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * ��ʾ���е�ӳ����,surface ˫����
 * 
 * @author User
 *
 */
public class DoCalledImageView extends SurfaceView implements SurfaceHolder.Callback 
{

	private SurfaceHolder doCalledImageViewHolder;        
	private int sWidth ;                  
	private int sHeight;                  
	private int displayTime=1;	                      
	private String TAG="DoCalledImageView";
	private Timer timer=null;                  
	private boolean playFlag=true;            
	private boolean firstSuccessFlag=true;  
	
    //private boolean isPreview = false; 
    public Camera myCamera = null; 
    private AutoFocusCallback myAutoFocusCallback = null; 

	/*�趨¼������ش�С*/
//	private final int CAMERA_W = 640;
//	private final int CAMERA_H = 480;
    private int cameraId = 0;
	
    
	private Boolean startEncodeNow=false;
	
	public void startEncodeNow(boolean b){
		this.startEncodeNow=b;
	}
	
	
    /**
     * �����췽�����ڲ���ʹ��
     *    
     * @param context  ��������������
     * @param vector 
     */
	public DoCalledImageView(Context context){
		super(context);
	    doCalledImageViewHolder = this.getHolder();// ��ȡholder
	    doCalledImageViewHolder.addCallback(this);	
	    getHolder().setFormat(PixelFormat.TRANSLUCENT);  
	    setFocusable(true); 
	}
	
	
	
	/**
	 * �����췽������ͨ����Դ�ļ������й��첢������ʽ
	 * @param context     ��������������
	 * @param attrs       ��Դ�����ļ�
	 */
	public DoCalledImageView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    doCalledImageViewHolder = this.getHolder();// ��ȡholder
	    doCalledImageViewHolder.addCallback(this);	
	    setFocusable(true); // ���ý���  

	}

	/**
	 * construction
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DoCalledImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	  
	
    /**
     * This is called immediately after any structural changes
     * (format or size) have been made to the surface.
     * 
     */
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		// TODO Auto-generated method stub
		initCamera();  
	}
	
	
	/**
	 * This is called immediately after the surface is first created.
	 * 
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    sWidth=doCalledImageViewHolder.getSurfaceFrame().width();    
	    sHeight=doCalledImageViewHolder.getSurfaceFrame().height();  
		Log.d(TAG,"Surface_Width" +sWidth+"Surface_Height"+sHeight);

		/*cameraId = findFrontFacingCamera();
		Log.i(TAG, "front face id="+cameraId);*/
		myCamera = Camera.open(cameraId);  
		
		if(myCamera == null)
			Log.e(TAG, "Open camera failed");
		else {
			try {
				myCamera.setPreviewDisplay(doCalledImageViewHolder);  
				Log.i(TAG, "SurfaceHolder.Callback: surfaceCreated!");
			} catch (IOException e) { 
				// TODO Auto-generated catch block
				if(null != myCamera){
					myCamera.release();
					myCamera = null;
				}
				e.printStackTrace();
			}  
		}
	}
      

	/**
	 * 
	 * Called when surface is Destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
		//û�гɹ���ֹͣ����
		 Log.i(TAG, "SurfaceHolder.Callback��Surface Destroyed"); 

		 if(null != myCamera) 
		 { 
		   myCamera.setPreviewCallback(null); /*������PreviewCallbackʱ���������ǰ��Ȼ�˳����� ����ʵ����ע�͵�Ҳû��ϵ*/ 

		   myCamera.stopPreview(); 
		   //isPreview = false; 
		   myCamera.release(); 
		   myCamera = null; 
		 }
		doCalledImageViewHolder = null;
	}
 

	/*
	 * ��������ͷ
	 * 	1����һ����������ͷ��Ԥ�����
		2���ڶ�����ֹͣԤ��Ч��
		3�����������ͷ�����ͷ
		��ΪϵͳĬ��ֻ��ͬʱ����һ������ͷ������ǰ������ͷ���Ǻ�������ͷ�����Բ��õ�ʱ��һ��Ҫ�ͷ�
		4�����Ĳ����ÿ�����ͷ����
	 ***/
	public void destroyCamera() {
		if (myCamera == null) {
			return;
		}
		myCamera.setPreviewCallback(null);
		myCamera.stopPreview();
		myCamera.release();
		myCamera = null;
		doCalledImageViewHolder = null;

	}

	/**
	 * 
	 * ˯�ߵȴ�չʾ�����Ż�
	 * 
	 */
	public void waitForDisplay(){
		 try {
				Thread.sleep(displayTime);
			 } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
	}
	
	//��ʼ�����  
    public void initCamera() {  
    	
		//�Զ��۽������ص�  
		myAutoFocusCallback = new AutoFocusCallback() {  

	        public void onAutoFocus(boolean success, Camera camera) {  
	            // TODO Auto-generated method stub  
	            if(success)//success��ʾ�Խ��ɹ�  
	            {  
	                Log.i(TAG, "myAutoFocusCallback: success...");  
	                //myCamera.setOneShotPreviewCallback(null);  
	            }  
	            else  
	            {  
	                //δ�Խ��ɹ�  
	                Log.i(TAG, "myAutoFocusCallback: failed...");  
	            }  
	        }  
	    }; 
    	
    	
	    /*if(isPreview) {  
	        myCamera.stopPreview();  
	    }*/  
	    if(null != myCamera) {             
	        
	    	Camera.Parameters myParam = myCamera.getParameters();  
	    	
	    	/*ö������ͷ֧�ֵ�֡��*/
	        List<int[]> range=myParam.getSupportedPreviewFpsRange();   
	        Log.d(TAG, "range:"+range.size());   
	        for(int j=0;j<range.size();j++) {   
	            int[] r=range.get(j);   
	            for(int k=0;k<r.length;k++) {   
	                Log.d(TAG, "Preview fps:"+r[k]/1000);   
	            }   
	        } 
	        
	
	        //��ѯcamera֧�ֵ�picturesize��previewsize  
	        List<Size> pictureSizes = myParam.getSupportedPictureSizes();  
	        List<Size> previewSizes = myParam.getSupportedPreviewSizes();  
	        for(int i=0; i<pictureSizes.size(); i++){  
	            Size size = pictureSizes.get(i);  
	            Log.i(TAG, "����ͷ֧�ֵ�pictureSizes: width = "+size.width+"height = "+size.height);  
	        }  
	        for(int i=0; i<previewSizes.size(); i++){  
	            Size size = previewSizes.get(i);  
	            Log.i(TAG, "����ͷ֧�ֵ�previewSizes: width = "+size.width+"height = "+size.height);  
	        }  
	        
	        //�������ݸ�ʽ
	        myParam.setPictureFormat(ImageFormat.JPEG); //�������պ�洢��ͼƬ��ʽ  
	        myParam.setPreviewFormat(ImageFormat.NV21); //����Ԥ�������ݸ�ʽ  
	
	        /*���ô�С*/
	        myParam.setPreviewSize(VideoTalkActivity.CAMERA_W, VideoTalkActivity.CAMERA_H);  
	        
	        /*���÷���*/
//	        myParam.set("rotation", 90);                
//	        myCamera.setDisplayOrientation(90);    
	        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
	        	//�ж�ϵͳ�汾�Ƿ���ڵ���2.2
//	        	myCamera.setDisplayOrientation(90);//��ת90�㣬ǰ���ǵ�ǰҳportrait������
	        } 
	        else {
	        	//ϵͳ�汾��2.2���µĲ�������ķ�ʽ��ת
	        	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	        		myParam.set("orientation", "portrait");
	        		myParam.set("rotation", 90);
	        	}
	        	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        		myParam.set("orientation", "landscape");
	        		myParam.set("rotation", 90);
	        	}
	        }	        
	        
	        /*����֡�ʣ����������ã�����֣���������������ȷ�ģ�����w callbackƵ�ʲ�û�иı䣬���������Ӱ���������*/
//	        myParam.setPreviewFpsRange(20000, 20000);    	//������Ч��
//	        myParam.setPreviewFrameRate(5);				//������Ч��

//	        myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);  //̨��ƽ�岻֧��
	        
	        /*�Ѳ������õ�����ͷ*/
	        myCamera.setParameters(myParam);
	        
	        /*����Ԥ���ص�*/
	        myCamera.setPreviewCallback(mPreviewCallback);

	        myCamera.startPreview();  
	        myCamera.autoFocus(myAutoFocusCallback);  
	        
	        //isPreview = true;  
	    }  
    }

    Camera.PreviewCallback  mPreviewCallback = new Camera.PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			/*preview  �Ļص�������ÿ��ʾһ֡���ͻ���ô˺���
			���ǵ���������ʱ��϶࣬���������һ��AsyncTask����������ı��빤��*/
			if(startEncodeNow){
				EncodeTask mEncTask= new EncodeTask(data);
				mEncTask.execute((Void)null);
//				Log.e(TAG,"PreviewCallback");
			}
				
		}
	};
	
	private int findFrontFacingCamera() {
		int cameraId = -1;
		//��ǰ������ͷ
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				Log.d(TAG, "Camera found");
				cameraId = i;
				break;
			}
		}
		if(cameraId<0) {
			/*û��ǰ������ͷ����Ĭ�ϵ�����ͷ*/
			cameraId = 0;
		}
		return cameraId;
	}

	/**
	 * 
	 * 
	 * @author User
	 *
	 */
	private class EncodeTask extends AsyncTask<Void, Void, Void>{

		private byte[] mData;
        //���캯��
		EncodeTask(byte[] data){
			this.mData = data;
		}
        
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
        	/*����JNI����ɱ���һ֡�Ĺ���*/
			FFMpegIF.Encoding(mData, mData.length, null);
			return null;
        }
    } 
}

