package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FaceZoFilterType;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceFollicleCleanDegree extends FaceZoFilter {

   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {

      Bitmap originalImage = getOriginalImage();
      Bitmap createBitmap = getFilterPartsImages();
      Mat filterMat = new Mat();
      Utils.bitmapToMat(createBitmap,filterMat);

      Mat oriMat = new Mat();
      Utils.bitmapToMat(originalImage,oriMat);

      List<Mat> rgbList = new ArrayList<>();

      Core.split(filterMat,rgbList);

      Mat blueMat = rgbList.get(2);

      Imgproc.blur(blueMat,filterMat,new Size(5,5));

      Imgproc.adaptiveThreshold(filterMat,filterMat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,7,2);

      Imgproc.medianBlur(filterMat,filterMat,5);

      Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));

      Imgproc.erode(filterMat,filterMat,structuringElement);

      Imgproc.dilate(filterMat,filterMat,structuringElement);

      List<MatOfPoint> list = new ArrayList<>();

      Mat hierarchy = new Mat();

      Imgproc.findContours(filterMat,list,hierarchy, Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_NONE);

      Mat resultMat = new Mat(oriMat.size(),oriMat.type(),new Scalar(0,0,0,0));

      Mat areaMat = new Mat(oriMat.size(),oriMat.type(),new Scalar(0,0,0,255));

      int count = 0;

      for (int i = 0; i < list.size(); i++) {
         MatOfPoint point = list.get(i);
         if (point.size().area()<20){
            Imgproc.drawContours(resultMat,list,i,new Scalar(255,0,0,255));
            count++;
            Imgproc.drawContours(areaMat,list,i,new Scalar(255,255,255,255),Imgproc.FILLED);
         }
      }
      //---------------------------------
      LogUtils.e(count);
      float score = 85;

      //659

      if (count<=150){//50
         score = ((1-(count / 150f)) * 15)  + 70;
      }else if (count>150 && count<=300){//50-100
         score = ((1-((count-150) / 150f)) * 10) + 60;
      }else if (count>300&&count<=500){//100-200
         score = ((1-((count-300) / 200f)) * 10) + 50;
      }else if (count>500){//200
         if (count>500&&count<=700){//200-300
            score =  ((1-((count - 500) /200f)) * 10) + 40;
         }else if (count>700 && count<=900){//300-350
            score =  ((1-((count - 700) /200f)) * 10) + 30;
         }else if (count>900 &&count<=1100){//350-400
            score =  ((1-((count - 900) /200f)) * 10) + 20;
         }else {
            score = 20;
         }
      }
      filterFaceInfoResult.setScore((int) score);

      //---------------------------------

      Utils.matToBitmap(resultMat,createBitmap);
      filterMat.release();
      oriMat.release();
      blueMat.release();

      structuringElement.release();
      hierarchy.release();

      Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
      Canvas canvas = new Canvas(resultBitmap);
      canvas.drawBitmap(createBitmap,0,0,null);

      filterFaceInfoResult.setFilterImage(resultBitmap);

   }

}
