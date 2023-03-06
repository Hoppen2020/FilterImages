package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

import static co.hoppen.filter.FacePart.FACE_LEFT_RIGHT_AREA;


/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceAcne extends FaceFilter {

    /**
     *
     * 5↓——level1 5-20——level2 20-30——level3 30↑——level4
     */

    @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
            Bitmap operateBitmap = getFaceAreaImage();

            Mat filterMat = new Mat();
            Utils.bitmapToMat(operateBitmap,filterMat);

            SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();
            //最小阈值
            params.set_minThreshold(20);
            //最大阈值
            params.set_maxThreshold(200);
            //区块过滤器
            params.set_filterByArea(true);
            //最小区块
            params.set_minArea(15);
            //圆过滤器
            params.set_filterByCircularity(true);
            //最小圆
            params.set_minCircularity(0.5f);//0.7
            //凸度过滤器
            params.set_filterByConvexity(true);
            //最小凸度
            params.set_minConvexity(0.5f);//0.87
            //偏心率过滤器
            params.set_filterByInertia(false);
            //最小偏心率
            params.set_minInertiaRatio(0.5f);

            MatOfKeyPoint keyPoints = new MatOfKeyPoint();
            SimpleBlobDetector detector = SimpleBlobDetector.create(params);
            Mat mask = getFaceSkinByRgb();
            detector.detect(filterMat,keyPoints,mask);

            Mat dst = new Mat();
            Utils.bitmapToMat(getOriginalImage(),dst);
            Features2d.drawKeypoints(dst,keyPoints,dst, new Scalar(255,0,0,255),Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

            //Features2d.drawKeypoints(areaMat,keyPoints,areaMat, new Scalar(255,255,255,255),Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
          MatOfPoint matOfPoint = new MatOfPoint();
          List<Point> areaPoints = new ArrayList<>();
          List<KeyPoint> keyPointList = keyPoints.toList();
          for (int i = 0; i < keyPointList.size(); i++) {
              areaPoints.add(keyPointList.get(i).pt);
          }
          if (areaPoints.size()!=0){
              matOfPoint.fromList(areaPoints);
              filterInfoResult.setFaceAreaInfo(createFaceAreaInfoByPoints(matOfPoint,operateBitmap.getWidth(),operateBitmap.getHeight()));
          }

          //5↓——level1 5-20——level2 20-30——level3 30↑——level4
          float score = 85;
          int count = areaPoints.size();
          if (count<=5){
              score = ((1 - (count / 5)) * 15) + 70f;
          }else if (count>5&&count<=20){
              score = ((1 - ((count-5) / 15f)) * 10) + 60f;
          }else if (count>20&&count<=30){
              score = ((1 - ((count-20) / 10f)) * 10) + 50f;
          }else if (count>30){
              if (count>30 && count<=60){
                  score =  ((1-((count - 30) /30f)) * 10) + 40;
              }else if (count>60 && count<=80){
                  score =  ((1-((count - 60) /20f)) * 10) + 30;
              }else if (count>80 && count<=100){
                  score =  ((1-((count - 80) /20f)) * 10) + 20;
              }else {
                  score = 20;
              }
          }
          filterInfoResult.setScore((int) score);


          Utils.matToBitmap(dst,operateBitmap);

            filterMat.release();
            mask.release();
            dst.release();
            //areaMat.release();
            filterInfoResult.setFilterBitmap(operateBitmap);
            filterInfoResult.setDataTypeString(getFilterDataType(),count);
            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
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


   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_FOREHEAD, FACE_LEFT_RIGHT_AREA};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COUNT;
   }

}
