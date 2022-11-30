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

   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
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
      return filterInfoResult;
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
