package co.hoppen.filter.filter;

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

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/8.
 */
public class FaceFollicleCleanDegree extends FaceFilter {

    /**
     * 个数:239个 35分 / 72个  65分
     * 50↓——level1 / 50-100——level2 /  100-200——level3  200↑——level4
     *
     */

    @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
            Bitmap originalImage = getOriginalImage();
            Bitmap createBitmap = getFaceAreaImage();
                    //originalImage.copy(Bitmap.Config.ARGB_8888,true);
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
            float score = 85;

            if (count<=50){
                //70 - 85
                score = ((1-(count / 50f)) * 15)  + 70;
            }else if (count>50 && count<=100){
                //60 - 70
                score = ((1-((count-50) / 50f)) * 10) + 60;
            }else if (count>100&&count<=200){
                // 50 - 60
                score = ((1-((count-100) / 100f)) * 10) + 50;
            }else if (count>200){
                //20 - 50
                //score = (30 - (count - 200))  + 20;
                if (count>200&&count<=300){
                    score =  ((1-((count - 200) /100f)) * 10) + 40;
                }else if (count>300 && count<=350){
                    score =  ((1-((count - 300) /50f)) * 10) + 30;
                }else if (count>350 &&count<=400){
                    score =  ((1-((count - 350) /50f)) * 10) + 20;
                }else {
                    score = 20;
                }
            }
            filterInfoResult.setScore((int) score);

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

            filterInfoResult.setDataTypeString(getFilterDataType(),count);

            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));

            filterInfoResult.setFilterBitmap(resultBitmap);

            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_MIDDLE};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COUNT;
   }


}
