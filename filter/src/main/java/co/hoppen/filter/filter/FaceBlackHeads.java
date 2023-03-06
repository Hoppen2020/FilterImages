package co.hoppen.filter.filter;

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

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;


/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceBlackHeads extends FaceFilter {

    /**
     * tag 颜色
     *
     * 120↑——level4 90-120——level3 40-90——level2 10-40——level1
     *
     */

    @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
          Bitmap originalImage = getOriginalImage();
          Bitmap createBitmap = getFaceAreaImage();

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

          MatOfPoint areaMatOfPoint = new MatOfPoint();
          List<Point> areaPoints = new ArrayList<>();

          for (int i = 0; i < list.size(); i++) {
              MatOfPoint point = list.get(i);
              if (point.size().area()<20){
                  Imgproc.drawContours(resultMat,list,i,new Scalar(255,0,0,255));
                  count++;
                  areaPoints.addAll(point.toList());
              }
          }
          if (count!=0){
              areaMatOfPoint.fromList(areaPoints);
              filterInfoResult.setFaceAreaInfo(createFaceAreaInfoByPoints(areaMatOfPoint,originalImage.getWidth(),originalImage.getHeight()));
          }

          //120↑——level4 90-120——level3 40-90——level2 10-40——level1

          float score = 85;
          if (count<=40){
              //70 - 85
              score = ((1 - (count / 40f)) * 15) + 70f;
          }else if (count>40 && count<=90){
              //60 - 70
              score = ((1 - ((count-40) / 50f)) * 10) + 60f;
          }else if (count>90 && count<=120){
             // 50 - 60
              score = ((1 - ((count-90) / 30f)) * 10) + 50f;
          }else if (count>120){
              // 20 - 50
              if (count>120 &&count<=140){
                  score =  ((1-((count - 120) /20f)) * 10) + 40;
              }else if (count>140 && count<=160){
                  score =  ((1-((count - 140) /20f)) * 10) + 30;
              }else if (count>160 && count<=180){
                  score =  ((1-((count - 160) /20f)) * 10) + 20;
              }else {
                  score = 20;
              }
          }
          filterInfoResult.setScore((int) score);

          Utils.matToBitmap(resultMat,createBitmap);
          resultMat.release();
          filterMat.release();
          blueMat.release();

          structuringElement.release();
          hierarchy.release();

          filterInfoResult.setDataTypeString(getFilterDataType(),count);

          filterInfoResult.setFilterBitmap(createBitmap);

          filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

   }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_NOSE};
   }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.COUNT;
    }

}
