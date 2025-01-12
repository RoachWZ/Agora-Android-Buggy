/*
 * This file is part of the Autonomous Android Vehicle (AAV) application.
 *
 * AAV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AAV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AAV.  If not, see <http://www.gnu.org/licenses/>.
 */

package ioio.aav;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.llw.bledemo.callback.BleCallback;

public class AAVActivity extends IOIOActivity implements CvCameraViewListener2 {

	private static final String _TAG = "AAVActivity";

	static final double MIN_CONTOUR_AREA = 100;

	private Mat _rgbaImage;

	// Android4.4之后 红外遥控ConsumerIrManager add by wangzheng
	private ConsumerIrManager mCIR;

	private JavaCameraView _opencvCameraView;
	private ActuatorController _mainController;

	volatile double _contourArea = 7;
	volatile Point _centerPoint = new Point(-1, -1);
	Point _screenCenterCoordinates = new Point(-1, -1);//Coordinates坐标
	int _countOutOfFrame = 0;

	Mat _hsvMat;
	Mat _processedMat;
	Mat _dilatedMat;
	Scalar _lowerThreshold;//scalar标量
	Scalar _upperThreshold;//Threshold门槛值
	final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	SharedPreferences _sharedPreferences;
	GestureDetector _gestureDetector;
	static int _trackingColor = 0;

	private boolean _showContourEnable = false;

	private Thread thread;

	/**
	 * Gatt回调
	 */
	private BleCallback bleCallback;
	/**
	 * Gatt
	 */
	private BluetoothGatt bluetoothGatt;

	// See Static Initialization of OpenCV (http://tinyurl.com/zof437m)
	//
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.d("ERROR", "Unable to load OpenCV");
		}
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					_opencvCameraView.enableView();
					_hsvMat = new Mat();
					_processedMat = new Mat();
					_dilatedMat = new Mat();
				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(_TAG, "onCreate");

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		// 获取系统的红外遥控服务
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		_trackingColor = Integer.parseInt(_sharedPreferences.getString(getString(R.string.color_key), "0"));

		if (_trackingColor == 0) {
			//Threshold门槛值
			_lowerThreshold = new Scalar(60, 100, 30); // Green
			_upperThreshold = new Scalar(130, 255, 255);
		} else if (_trackingColor == 1) {
			_lowerThreshold = new Scalar(160, 50, 90); // Purple
			_upperThreshold = new Scalar(255, 255, 255);
		} else if (_trackingColor == 2) {
			_lowerThreshold = new Scalar(1, 50, 150); // Orange
			_upperThreshold = new Scalar(60, 255, 255);
		}
		_showContourEnable = _sharedPreferences.getBoolean("contour", false);

		_opencvCameraView = (JavaCameraView) findViewById(R.id.aav_activity_surface_view);
		_opencvCameraView.setCvCameraViewListener(this);

		_opencvCameraView.setMaxFrameSize(352, 288); // (176, 144); //(320, 240); <-Callback buffer is too small for these resolutions.
		_mainController = new ActuatorController(mCIR);
		_countOutOfFrame = 0;

		_gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public void onLongPress(MotionEvent e) {
				startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), 0);
			}
		});

		//初始化
		bleCallback = new BleCallback();
		//获取上个页面传递过来的设备
		//获取上个页面传递过来的设备
		BluetoothDevice device = getIntent().getParcelableExtra("device");
		//连接gatt 设置Gatt回调
		bluetoothGatt = device.connectGatt(this, false, bleCallback);

		//为了不阻断启动 放到线程里去执行 add by wangzheng
		thread = new Thread(new IR_Task());
		thread.start();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_showContourEnable = _sharedPreferences.getBoolean("contour", false);
		_trackingColor = Integer.parseInt(_sharedPreferences.getString(getString(R.string.color_key), "0"));

		switch (_trackingColor) {
			case 0: // Green
				_lowerThreshold.set(new double[] { 60, 100, 30, 0 });
				_upperThreshold.set(new double[] { 130, 255, 255, 0 });
				break;
			case 1: // Purple
				_lowerThreshold.set(new double[] { 160, 50, 90 });
				_upperThreshold.set(new double[] { 255, 255, 255, 0 });
				break;
			case 2: // Orange
				_lowerThreshold.set(new double[] { 1, 50, 150 });
				_upperThreshold.set(new double[] { 60, 255, 255, 0 });
				break;
			default:
				_lowerThreshold.set(new double[] { 60, 100, 30, 0 });
				_upperThreshold.set(new double[] { 130, 255, 255, 0 });
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i(_TAG, "onResume");

		if (!OpenCVLoader.initDebug()) {
			Log.d(_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback)) {
				Log.e(_TAG, "Cannot connect to OpenCV Manager");
			}
		} else {
			Log.d(_TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}

		hideNavigationBar();

	}

	@Override
	public void onPause() {
		super.onPause();

		Log.i(_TAG, "onPause");

		if (_opencvCameraView != null)
			_opencvCameraView.disableView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i(_TAG, "onDestroy");

		if (_opencvCameraView != null)
			_opencvCameraView.disableView();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
		return _gestureDetector.onTouchEvent(event);
	}

	private void hideNavigationBar() {
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		_rgbaImage = new Mat(height, width, CvType.CV_8UC4);
		_screenCenterCoordinates.x = _rgbaImage.size().width / 2;
		_screenCenterCoordinates.y = _rgbaImage.size().height / 2;
	}

	@Override
	public void onCameraViewStopped() {
		_mainController.reset();
		_rgbaImage.release();
		_centerPoint.x = -1;
		_centerPoint.y = -1;
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		synchronized (inputFrame) {

			_rgbaImage = inputFrame.rgba();

			if (android.os.Build.MODEL.equalsIgnoreCase("Nexus 5X")) {
				Core.flip(_rgbaImage, _rgbaImage, -1);
			}

			double current_contour;

			// In contrast to the C++ interface, Android API captures images in the RGBA format.
			// Also, in HSV space, only the hue determines which color it is. Saturation determines
			// how 'white' the color is, and Value determines how 'dark' the color is.
			Imgproc.cvtColor(_rgbaImage, _hsvMat, Imgproc.COLOR_RGB2HSV_FULL);

			Core.inRange(_hsvMat, _lowerThreshold, _upperThreshold, _processedMat);

			// Imgproc.dilate(_processedMat, _dilatedMat, new Mat());
			Imgproc.erode(_processedMat, _dilatedMat, new Mat());
			Imgproc.findContours(_dilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			MatOfPoint2f points = new MatOfPoint2f();
			_contourArea = 7;
			for (int i = 0, n = contours.size(); i < n; i++) {
				current_contour = Imgproc.contourArea(contours.get(i));
				if (current_contour > _contourArea) {
					_contourArea = current_contour;
					contours.get(i).convertTo(points, CvType.CV_32FC2); // contours.get(x) is a single MatOfPoint, but to use minEnclosingCircle we need to pass a MatOfPoint2f so we need to do a
					// conversion
				}
			}
			if (!points.empty() && _contourArea > MIN_CONTOUR_AREA) {
				Imgproc.minEnclosingCircle(points, _centerPoint, null);
				// Core.circle(_rgbaImage, _centerPoint, 3, new Scalar(255, 0, 0), Core.FILLED);
				if (_showContourEnable)
					Core.circle(_rgbaImage, _centerPoint, (int) Math.round(Math.sqrt(_contourArea / Math.PI)), new Scalar(255, 0, 0), 3, 8, 0);// Core.FILLED);
			}
			contours.clear();
		}
		return _rgbaImage;
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run every time the application is resumed and aborted when it is paused. The method setup() will be called right after a
	 * connection with the IOIO has been established (which might happen several times!). Then, loop() will be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {

		private PwmOutput _pwmPan;
		private PwmOutput _pwmTilt;
		private PwmOutput _pwmMotor;
		private PwmOutput _pwmFrontWheels;

		private double[] _pwmValues = new double[4];

		// IRs
		private AnalogInput _sideLeftIR, _sideRightIR, _frontRightIR, _frontLeftIR;

		boolean is_backing = false;

		int pwm_counter = 0;

		/**
		 * Called every time a connection with IOIO has been established. Typically used to open pins.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException
		 *
		 * see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException, InterruptedException {

			try {
				_pwmValues = _mainController.getPWMValues();

				_pwmPan = ioio_.openPwmOutput(14, 100);
				_pwmTilt = ioio_.openPwmOutput(13, 100);
				_pwmMotor = ioio_.openPwmOutput(12, 100);
				_pwmFrontWheels = ioio_.openPwmOutput(11, 100);

				_sideLeftIR = ioio_.openAnalogInput(37);
				_frontLeftIR = ioio_.openAnalogInput(38);
				_sideRightIR = ioio_.openAnalogInput(33);
				_frontRightIR = ioio_.openAnalogInput(34);

				_pwmPan.setPulseWidth((int) _pwmValues[0]);
				_pwmTilt.setPulseWidth((int) _pwmValues[1]);
				_pwmMotor.setPulseWidth((int) _pwmValues[2]);
				_pwmFrontWheels.setPulseWidth((int) _pwmValues[3]);

			} catch (ConnectionLostException e) {
				Log.e(_TAG, e.getMessage());
				throw e;
			}
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 *
		 * see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {

			Log.i(_TAG, "begin loop");

			try {
				synchronized (_mainController) {

					if (_contourArea > MIN_CONTOUR_AREA) {
						//_mainController.updatePanTiltPWM(_screenCenterCoordinates, _centerPoint);
						//_mainController._irSensors.updateIRSensorsVoltage(_sideLeftIR.getVoltage(), _sideRightIR.getVoltage(), _frontRightIR.getVoltage(), _frontLeftIR.getVoltage());
						//_mainController.updateMotorPWM(_contourArea);
						_countOutOfFrame = 0;
					} else {
						if (_countOutOfFrame > 5) {


							_mainController.reset();
							_countOutOfFrame = 0;
						}
						_countOutOfFrame++;
					}

					_pwmValues = _mainController.getPWMValues();

					_pwmPan.setPulseWidth((int) _pwmValues[0]);
					_pwmTilt.setPulseWidth((int) _pwmValues[1]);
					_pwmFrontWheels.setPulseWidth((int) _pwmValues[3]);
					_pwmMotor.setPulseWidth((int) _pwmValues[2]);
				}
				Thread.sleep(20);

			} catch (InterruptedException e) {
				ioio_.disconnect();
			}
		}

		@Override
		public void disconnected() {
			_sideLeftIR.close();
			_frontLeftIR.close();
			_sideRightIR.close();
			_frontRightIR.close();

			_pwmPan.close();
			_pwmTilt.close();
			_pwmMotor.close();
			_pwmFrontWheels.close();
		}
	}

	/**
	 * A method to create our IOIO thread.
	 *
	 * see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	/**
	 * create by wangzheng 2022-08-09
	 */
	class IR_Task implements Runnable {

		public void run() {

			while(true){
//				Log.i(_TAG, "begin loop");可以走到这步

				try {
					synchronized (_mainController) {

						if (_contourArea > MIN_CONTOUR_AREA) {
//							Log.i(_TAG, "begin moving");可以走到这步
							_mainController.updatePanTiltPWM( _centerPoint,bluetoothGatt);//wangzheng add by 2022-08-11

							//_mainController.updatePanTiltPWM(_screenCenterCoordinates, _centerPoint);
							//_mainController._irSensors.updateIRSensorsVoltage(_sideLeftIR.getVoltage(), _sideRightIR.getVoltage(), _frontRightIR.getVoltage(), _frontLeftIR.getVoltage());
							_mainController.updateMotorPWM(_contourArea,bluetoothGatt);
							_countOutOfFrame = 0;
						} else {
							if (_countOutOfFrame > 5) {


								_mainController.reset();
								_countOutOfFrame = 0;
							}
							_countOutOfFrame++;
						}

						//				_pwmValues = _mainController.getPWMValues();
						//
						//				_pwmPan.setPulseWidth((int) _pwmValues[0]);
						//				_pwmTilt.setPulseWidth((int) _pwmValues[1]);
						//				_pwmFrontWheels.setPulseWidth((int) _pwmValues[3]);
						//				_pwmMotor.setPulseWidth((int) _pwmValues[2]);
					}
					Thread.sleep(20);

				} catch (InterruptedException e) {
					//			ioio_.disconnect();
				}
			}

		}
	}

}