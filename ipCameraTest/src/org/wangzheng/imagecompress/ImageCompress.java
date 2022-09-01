package org.wangzheng.imagecompress;

import java.io.ByteArrayOutputStream;

import org.wangzheng.imagesend.ImagesendThread;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

@SuppressWarnings("deprecation")
public class ImageCompress implements Camera.PreviewCallback {
	private String ipname;

	public ImageCompress(String ipname) {
		this.ipname = ipname;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Size size = camera.getParameters().getPreviewSize();
		try {
			// ����image.compressToJpeg������YUV��ʽͼ������dataתΪjpg��ʽ
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			if (image != null) {
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, outstream);
				outstream.flush();
				// �����߳̽�ͼ�����ݷ��ͳ�ȥ
				Thread th = new ImagesendThread(outstream, ipname);
				th.start();

			}
		} catch (Exception ex) {
			Log.e("Sys", "Error:" + ex.getMessage());
		}
	}
}


