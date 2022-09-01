package com.csst.ffmpeg.views;
import java.util.Vector;

import com.csst.videotalk.R;
import com.csst.videotalk.VideoTalkActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * ��ʾ���е�ӳ����
 * 
 * @author User
 *
 */
public class BeCalledImageView extends SurfaceView implements SurfaceHolder.Callback 
{
	private SurfaceHolder sHolder;        
	private int sWidth ;                  
	private int sHeight;                  
	private int displayTime=100;	          
	
	private Paint mPaint = null;                  
	private Canvas canvas = null;    
	private Bitmap cameraBitmap=null;
	
	private String TAG="BeCalledImageView";

	private boolean playFlag=true;            


    /**
     * �����췽�����ڲ���ʹ��
     *    
     * @param context  ��������������
     * @param vector 
     */
	public BeCalledImageView(Context context){
		super(context);
	    sHolder = this.getHolder();// ��ȡholder
	    sHolder.addCallback(this);	
	    getHolder().setFormat(PixelFormat.TRANSLUCENT);  
	    setFocusable(true); 
	}
	

	
	/**
	 * ��ʱ����   
	 * 
	 * @param context
	 * @param vector
	 */
	private BeCalledImageView(Context context, Vector<String> vector){ //,IniData.IniPicInfo iniPicInfo) {
		super(context);
	    sHolder = this.getHolder();// ��ȡholder
	    sHolder.addCallback(this);	
	    getHolder().setFormat(PixelFormat.TRANSLUCENT);  //����͸������
	    setFocusable(true); // ���ý���   
//		setZOrderOnTop(true);   //
	}
	
	
	/**
	 * �����췽������ͨ����Դ�ļ������й��첢������ʽ
	 * @param context     ��������������
	 * @param attrs       ��Դ�����ļ�
	 */
	public BeCalledImageView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    sHolder = this.getHolder();// ��ȡholder
	    sHolder.addCallback(this);	
	    setFocusable(true); // ���ý���  
		Log.d(TAG,"Surface_Width" +sWidth+"Surface_Height"+sHeight);

	}

	/**
	 * construction
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public BeCalledImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		Log.d(TAG,"Surface_Width 1" +sWidth+"Surface_Height"+sHeight);

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
 
	}
	
	
	/**
	 * This is called immediately after the surface is first created.
	 * 
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    sWidth=sHolder.getSurfaceFrame().width();    
	    sHeight=sHolder.getSurfaceFrame().height();  
		Log.d(TAG,"Surface_Width 2" +sWidth+"Surface_Height"+sHeight);
		mPaint=new Paint();
		Resources res = getResources();
//	   Resources res = getResources();

		cameraBitmap=BitmapFactory.decodeResource(res, R.drawable.camerapic);

	}
     

	/**
	 * 
	 * Called when surface is Destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		playFlag=false;   
		sHolder = null;
	}
	
	
	/**
	 * ˢ�½�����ͼ��
	 * 
	 * 
	 */
	public void drawBecalledImage(){
	    	try {
	    	    canvas = sHolder.lockCanvas();        

			    if(canvas!=null&&VideoTalkActivity.beCalledBitmap!=null){
			    	canvas.drawBitmap(VideoTalkActivity.beCalledBitmap, 
			    		   new Rect(0, 0, VideoTalkActivity.beCalledBitmap.getWidth(), VideoTalkActivity.beCalledBitmap.getHeight()),
//			    		   new Rect(0, 0, sHeight*4/3,sHeight), 
			    		   new Rect(0, 0, sWidth,sHeight), 
			    		   mPaint);
//			       canvas.drawBitmap(VideoTalkActivity.beCalledBitmap,0,0,mPaint);
			    }else{
			       Log.e(TAG,"��ͼ��ͼ��Ϊ�գ�"+"    canvas:"+canvas+"    bitmap:"+VideoTalkActivity.beCalledBitmap);
			    }
	
	    	} catch (Exception e) {
	    		Log.d(TAG, "draw is Error!"+e.getMessage());
	    	} finally {
	    		if(canvas!=null&&sHolder!=null){
		    		sHolder.unlockCanvasAndPost(canvas);
	    		}
	       }
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
	
}

