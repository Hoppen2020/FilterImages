package co.hoppen.filter.filter;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceTest extends FaceFilter{

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
      Bitmap originalImage = getOriginalImage();
      Bitmap cacheBitmap = getOriginalImage();
//
      Mat resultMat = new Mat();
      Utils.bitmapToMat(cacheBitmap,resultMat);

      Mat frameMat = new Mat();
      Utils.bitmapToMat(cacheBitmap,frameMat);

      Imgproc.cvtColor(frameMat,frameMat,Imgproc.COLOR_RGBA2GRAY);

      Imgproc.equalizeHist(frameMat,frameMat);

      Imgproc.GaussianBlur(frameMat,frameMat,new Size(3,3),0);

      Mat copyMat =  new Mat();
      frameMat.copyTo(copyMat,getFaceSkinByRgb());


//      Imgproc.threshold(copyMat,frameMat,100,255,Imgproc.THRESH_BINARY_INV);

      Imgproc.adaptiveThreshold(copyMat,frameMat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,9,10);
//
      //Imgproc.morphologyEx(frameMat,frameMat,Imgproc.MORPH_OPEN,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));

//      List<MatOfPoint> list = new ArrayList<>();
//
//      Mat hierarchy = new Mat();
//
//      Imgproc.findContours(frameMat,list,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
//
//      int count = 0;
//
//      for (int i = 0; i < list.size(); i++) {
//         MatOfPoint point = list.get(i);
//         if (point.size().area()>5 && point.size().area()<=200){
//            count++;
//            Imgproc.drawContours(resultMat,list,i,new Scalar(255,0,0,255));
//         }
//      }
      Utils.matToBitmap(frameMat,cacheBitmap);
//      frameMat.release();
      //hierarchy.release();

      Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
      Canvas canvas = new Canvas(resultBitmap);
      canvas.drawBitmap(cacheBitmap,0,0,null);
      if (!cacheBitmap.isRecycled())cacheBitmap.recycle();
      filterInfoResult.setFilterBitmap(resultBitmap);
      //filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(resultMat,1));
      filterInfoResult.setDataTypeString(getFilterDataType(),0);
      filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
   }


   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_SKIN};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COUNT;
   }
   private Mat getFaceSkinByRgb(){
      Bitmap originalImage = getOriginalImage();
      Mat oriMat = new Mat();
      Utils.bitmapToMat(originalImage,oriMat);
      Mat hsvMat = new Mat();
      Imgproc.cvtColor(oriMat,hsvMat,Imgproc.COLOR_RGB2HSV);

      Mat ycrCbMat = new Mat();
      Imgproc.cvtColor(oriMat,ycrCbMat,Imgproc.COLOR_RGB2YCrCb);

      Core.inRange(hsvMat,new Scalar(0,15,0),new Scalar(17,170,255),hsvMat);

      Core.inRange(ycrCbMat,new Scalar(0,180,85),new Scalar(255,180,135),ycrCbMat);

      Core.add(hsvMat,ycrCbMat,oriMat);

      Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,new Size(3,3));
      //关操作,先膨胀后腐蚀
      Imgproc.morphologyEx(oriMat,oriMat,Imgproc.MORPH_DILATE,kernel);

      kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,new Size(6,6));

      Imgproc.morphologyEx(oriMat,oriMat,Imgproc.MORPH_ERODE,kernel);

      hsvMat.release();
      ycrCbMat.release();
      kernel.release();
      return oriMat;
   }
}
