package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * 色素沉着
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceSuperficialPlaque2 extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
         Mat operateMat = new Mat();
         Utils.bitmapToMat(getOriginalImage(),operateMat);

       Mat hsvImage = new Mat();
       Imgproc.cvtColor(operateMat, hsvImage, Imgproc.COLOR_RGB2HSV);
       // 分离通道
       List<Mat> channels = new ArrayList<>();
       Core.split(hsvImage, channels);
       // 增加亮度
       double alpha = 1.5; // 增加的常数
       Core.multiply(channels.get(2), new Scalar(alpha), channels.get(2));
       // 合并通道
       Core.merge(channels, hsvImage);
       // 转换回 RGB 色彩空间
       Imgproc.cvtColor(hsvImage, operateMat, Imgproc.COLOR_HSV2RGB);

       Mat gray = new Mat();

       Imgproc.cvtColor(operateMat,gray,Imgproc.COLOR_RGB2GRAY);


       CLAHE clahe = Imgproc.createCLAHE(3, new Size(8, 8));

       clahe.apply(gray,gray);

       //Imgproc.cvtColor(gray,operateMat,Imgproc.COLOR_GRAY2RGB);

       //Imgproc.equalizeHist(gray,gray);

       //Imgproc.cvtColor(gray,operateMat,Imgproc.COLOR_GRAY2RGB);

       Mat mask = new Mat();

       Imgproc.threshold(gray,mask,0,255,Imgproc.THRESH_OTSU);


//       Mat result = new Mat();
//
//       Core.copyTo(operateMat,result,mask);

//       Imgproc.cvtColor(result,gray,Imgproc.COLOR_RGB2GRAY);
//
//       clahe.apply(gray,gray);
//
//       Core.bitwise_not(gray,gray,mask);

//       Imgproc.cvtColor(result,hsvImage,Imgproc.COLOR_RGB2HSV);
//
//       // 提取饱和度通道
//       Mat saturation = new Mat();
//       Core.extractChannel(hsvImage, saturation, 0);
//
//       // 增强对比度
//       Mat histEq = new Mat();
//       Imgproc.equalizeHist(saturation, histEq);
//
//       // 将直方图均衡化后的饱和度通道替换原图的饱和度通道
//       List<Mat> matList = new ArrayList<>();
//
//
//       Core.split(hsvImage, matList);
//       histEq.copyTo(matList.get(0));
//       Core.merge(matList, hsvImage);
//
//       // 转换回BGR颜色空间
//       Mat enhancedImage = new Mat();
//       Imgproc.cvtColor(hsvImage, enhancedImage, Imgproc.COLOR_HSV2RGB);


         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(mask,resultBitmap);

         Mat areaMat = new Mat();
         Utils.bitmapToMat(getFaceAreaImage(),areaMat);
         filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));

         filterInfoResult.setFilterBitmap(resultBitmap);
         filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

   }

   private Mat getPlSkin(){
         try {
            Mat oriMat = new Mat();
            Utils.bitmapToMat(getOriginalImage(),oriMat);

            Mat hsvMat = new Mat();
            Imgproc.cvtColor(oriMat,hsvMat,Imgproc.COLOR_RGB2HSV);
            Core.inRange(hsvMat,new Scalar(0,15,0),new Scalar(17,170,255),hsvMat);

            Mat yCrBrMat = new Mat();
            Imgproc.cvtColor(oriMat,yCrBrMat,Imgproc.COLOR_RGB2YCrCb);
            Core.inRange(yCrBrMat,new Scalar(0,135,85),new Scalar(255,180,135),yCrBrMat);

            Mat mask = new Mat();
            Core.bitwise_and(hsvMat,yCrBrMat,mask);

            Mat result = new Mat();
            Core.bitwise_and(oriMat,oriMat,result,mask);

            oriMat.release();
            hsvMat.release();
            yCrBrMat.release();
            mask.release();
            return result;
         }catch (Exception e){
         }
         return null;
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_SKIN};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.AREA;
   }

}
