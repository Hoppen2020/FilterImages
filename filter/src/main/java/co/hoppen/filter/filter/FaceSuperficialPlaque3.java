package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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
public class FaceSuperficialPlaque3 extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
       Mat operateMat = new Mat();
       Utils.bitmapToMat(getOriginalImage(),operateMat);
       Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);

       Mat resultMat = new Mat();
       operateMat.copyTo(resultMat);

       CLAHE clahe = Imgproc.createCLAHE(3, new Size(8, 8));

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
           Mat firstMask = new Mat();
           Imgproc.cvtColor(hsvImage, firstMask, Imgproc.COLOR_HSV2RGB);
           Imgproc.cvtColor(firstMask,firstMask,Imgproc.COLOR_RGB2GRAY);

           clahe.apply(firstMask,firstMask);
           Imgproc.threshold(firstMask,firstMask,0,255,Imgproc.THRESH_OTSU);


       //H：颜色 S：饱和度 V：亮度
       Mat hsvMat = new Mat();
       Imgproc.cvtColor(operateMat,hsvMat,Imgproc.COLOR_RGB2HSV);

       List<Mat> splitList = new ArrayList<>();
       Core.split(hsvMat,splitList);


       Mat saturation = new Mat();
       Core.extractChannel(hsvMat,saturation,1);

       // 增强对比度
       Mat histEq = new Mat();
       Imgproc.equalizeHist(saturation, histEq);
       clahe = Imgproc.createCLAHE(8.0, new Size(8, 8));
       clahe.apply(histEq,histEq);


       splitList.set(1,histEq);

       //Mat result = new Mat();
       Core.merge(splitList,operateMat);
       Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);
       //Core.bitwise_and(result,result,result);

       Mat mask = new Mat();
       Imgproc.cvtColor(operateMat,mask,Imgproc.COLOR_RGB2GRAY);

       Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

       Imgproc.dilate(mask,mask,kernel);

       Imgproc.erode(mask,mask,kernel);

       Imgproc.adaptiveThreshold(mask,mask,255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,99,2);

       kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

       Imgproc.dilate(mask,mask,kernel);

       Imgproc.erode(mask,mask,kernel);


       int fMaskChannels = firstMask.channels();
       int col = firstMask.cols();
       int row = firstMask.rows();

       byte[] p = new byte[fMaskChannels * col];
       byte[] p2 = new byte[mask.channels() * col];

       for (int h = 0; h < row; h++) {
           firstMask.get(h, 0, p);
           mask.get(h,0,p2);
           for (int w = 0; w < col; w++) {
               int index = fMaskChannels * w;
               int grayP = p[index] & 0xff;
               if (grayP!=0){
                   grayP = p2[index] & 0xff;
               }
               p[index] = (byte)grayP;
           }
           firstMask.put(h, 0, p);
       }
       kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
       Imgproc.dilate(firstMask,firstMask,kernel);
       Imgproc.erode(firstMask,firstMask,kernel);
       Imgproc.erode(firstMask,firstMask,kernel);

       p2 = new byte[resultMat.channels() * col];

       float count = 0;

       for (int h = 0; h < row; h++) {
           firstMask.get(h, 0, p);
           resultMat.get(h,0, p2);
           for (int w = 0; w < col; w++) {
               int index = fMaskChannels * w;
               int grayP = p[index] & 0xff;
               if (grayP==255){
                   index = resultMat.channels() * w;

                   int r = p2[index] & 0xff;
                   int g = p2[index + 1] & 0xff;
                   int b = p2[index + 2] & 0xff;

                   r = Math.min(r + 30, 255);
                   g = Math.min(g + 15, 255);
                   b = Math.min(b + 2, 255);
                   p2[index] = (byte)r;
                   p2[index+1] = (byte)g;
                   p2[index+2] = (byte)b;
                   count++;

               }
           }
           resultMat.put(h, 0, p2);
       }

       float score = 85;
       float areaPercent = FaceSkinUtils.getSkinArea()>count?count * 100f /FaceSkinUtils.getSkinArea():100f;

       if (areaPercent<=5){//占比少高分
           //75~85
           score = ((1 - (areaPercent / 5)) * 10f) + 75f;
       }else if (areaPercent>5 && areaPercent<=8){
           //65~75
           score = ((1 - ((areaPercent-5) / 3f)) * 10f) + 65f;
       }else if (areaPercent>8 && areaPercent<=15){
           //55~65
           score = ((1 - ((areaPercent- 8) / 7f)) * 10) + 55f;
       }else if (areaPercent>15 && areaPercent<=20){
           //45~55
           score = ((1 - ((areaPercent-15f) / 5f)) * 10) + 45f;
       }else if (areaPercent>20 && areaPercent<=30){
           //35~45
           score = ((1 - ((areaPercent-20f) / 10f)) * 10) + 35f;
       }else if (areaPercent>30 && areaPercent<=40){
           //25~35
           score = ((1 - ((areaPercent-30) / 10f)) * 10) + 25f;
       } else {
           score = 20f;
       }


       filterInfoResult.setScore((int) score);
       filterInfoResult.setDataTypeString(getFilterDataType(),(double)areaPercent);

         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(resultMat,resultBitmap);

         operateMat.release();
         resultMat.release();
         hsvImage.release();
         firstMask.release();
         hsvMat.release();
         saturation.release();
         histEq.release();
         mask.release();
         kernel.release();


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
