package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceBlackHeads extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Bitmap originalImage = getOriginalImage();
      Bitmap createBitmap = getFilterPartsImages();

      Mat filterMat = new Mat();
      Utils.bitmapToMat(createBitmap,filterMat);

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

      Mat resultMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(),resultMat);

      int count = 0;

      for (int i = 0; i < list.size(); i++) {
         MatOfPoint point = list.get(i);
         if (point.size().area()<12){
            Imgproc.drawContours(resultMat,list,i,new Scalar(255,0,0,255));
            count++;
         }
      }

      //120↑——level4 90-120——level3 40-90——level2 10-40——level1

      float score = 85;
      if (count<=100){//40
         //70 - 85
         score = ((1 - (count / 100f)) * 15) + 70f;
      }else if (count>100 && count<=200){//40-90
         //60 - 70
         score = ((1 - ((count-100) / 100f)) * 10) + 60f;
      }else if (count>200 && count<=300){//90-120
         // 50 - 60
         score = ((1 - ((count-200) / 100f)) * 10) + 50f;
      }else if (count>300){//120
         // 20 - 50
         if (count>300 &&count<=400){//120-140
            score =  ((1-((count - 300) /100f)) * 10) + 40;
         }else if (count>400 && count<=500){//140-160
            score =  ((1-((count - 400) /100f)) * 10) + 30;
         }else if (count>500 && count<=600){//160-180
            score =  ((1-((count - 500) /100f)) * 10) + 20;
         }else {
            score = 20;
         }
      }
      LogUtils.e(count);
      filterFaceInfoResult.setScore((int) score);

      Utils.matToBitmap(resultMat,createBitmap);
      resultMat.release();
      filterMat.release();
      blueMat.release();
      structuringElement.release();
      hierarchy.release();

      filterFaceInfoResult.setFilterImage(createBitmap.copy(originalImage.getConfig(),true));

   }
}
