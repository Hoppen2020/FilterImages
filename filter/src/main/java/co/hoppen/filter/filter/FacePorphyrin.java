package co.hoppen.filter.filter;

import android.graphics.Bitmap;

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
public class FacePorphyrin extends FaceFilter {

   /**
    *
    * 30↓——level1 30-80——level2 80-500——level3 500↑——level4
    */

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
         Bitmap operateBitmap = getFaceAreaImage();
         Mat operateMat = new Mat();
         Utils.bitmapToMat(operateBitmap,operateMat);
         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);
         Core.bitwise_not(operateMat,operateMat);

//         Imgproc.equalizeHist(operateMat,operateMat);

         Mat hsvMat = new Mat();
         Imgproc.cvtColor(operateMat,hsvMat,Imgproc.COLOR_GRAY2RGB);
         Imgproc.cvtColor(hsvMat,hsvMat,Imgproc.COLOR_RGB2HSV);
         Core.inRange(hsvMat,new Scalar(0,0,0),new Scalar(180,255,120),hsvMat);

         //膨胀
         Mat structuringElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(6, 6));
         Imgproc.dilate(hsvMat,hsvMat,structuringElement);

         List<MatOfPoint> contours = new ArrayList<>();
         Mat hierarchy = new Mat();
         Imgproc.findContours(hsvMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

         Mat drawPointsMat = new Mat();
         Utils.bitmapToMat(getOriginalImage(),drawPointsMat);

         for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawPointsMat,contours,i,new Scalar(0,255,0,255));
         }

         float score = 85;
         int count = contours.size();
        //       30↓——level1 30-80——level2 80-500——level3 500↑——level4
           if (count<=30){
               score = ((1 - (count / 30)) * 15) + 70f;
           }else if (count>30&&count<=80){
               score = ((1 - ((count-30) / 50f)) * 10) + 60f;
           }else if (count>80&&count<=500){
               score = ((1 - ((count-80) / 420f)) * 10) + 50f;
           }else if (count>500){
               if (count>500 && count<=600){
                   score =  ((1-((count - 500) /100f)) * 10) + 40;
               }else if (count>600 && count<=700){
                   score =  ((1-((count - 600) /100f)) * 10) + 30;
               }else if (count>700 && count<=800){
                   score =  ((1-((count - 700) /100f)) * 10) + 20;
               }else {
                   score = 20;
               }
           }
         filterInfoResult.setScore((int) score);

         Utils.matToBitmap(drawPointsMat,operateBitmap);

         operateMat.release();
         structuringElement.release();
         hierarchy.release();
         drawPointsMat.release();


         filterInfoResult.setFilterBitmap(operateBitmap);
         filterInfoResult.setDataTypeString(getFilterDataType(),count);

         Imgproc.cvtColor(hsvMat,hsvMat,Imgproc.COLOR_GRAY2RGBA);

         filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(hsvMat,39));

         filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

   }


   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_T,FacePart.FACE_LEFT_RIGHT_AREA};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COUNT;
   }

}
