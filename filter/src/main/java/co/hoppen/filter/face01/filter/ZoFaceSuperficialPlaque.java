package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceSuperficialPlaque extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
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

      float filterCount = 0;

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
               filterCount++;
            }
         }
         resultMat.put(h, 0, p2);
      }
      Bitmap filterBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
      Utils.matToBitmap(resultMat,filterBitmap);

      int width = filterBitmap.getWidth();
      int height = filterBitmap.getHeight();
      int count = width * height;
      int [] partsPixels = new int[count];
      int [] originalPixels = new int[count];
      int [] filterPixels = new int[count];
      getFilterPartsImages().getPixels(partsPixels,0,width,0,0,width,height);
      getOriginalImage().getPixels(originalPixels,0,width,0,0,width,height);
      filterBitmap.getPixels(filterPixels,0,width,0,0,width,height);

      float skinArea = 0;

      for (int i = 0; i <partsPixels.length ; i++) {
         int color = partsPixels[i];
         if (Color.alpha(color)==0){
            partsPixels[i] = originalPixels[i];
         }else {
            skinArea++;
            partsPixels[i] = filterPixels[i];
         }
      }
      filterBitmap.setPixels(partsPixels,0,width, 0, 0, width, height);

      float score = 85;
      float areaPercent = skinArea>filterCount?filterCount * 100f /skinArea:100f;

      if (areaPercent<=5){//占比少高分
         score = ((1 - (areaPercent / 5)) * 10f) + 75f;
      }else if (areaPercent>5 && areaPercent<=8){
         score = ((1 - ((areaPercent-5) / 3f)) * 10f) + 65f;
      }else if (areaPercent>8 && areaPercent<=15){
         score = ((1 - ((areaPercent- 8) / 7f)) * 10) + 55f;
      }else if (areaPercent>15 && areaPercent<=20){
         score = ((1 - ((areaPercent-15f) / 5f)) * 10) + 45f;
      }else if (areaPercent>20 && areaPercent<=30){
         score = ((1 - ((areaPercent-20f) / 10f)) * 10) + 35f;
      }else if (areaPercent>30 && areaPercent<=40){
         score = ((1 - ((areaPercent-30) / 10f)) * 10) + 25f;
      } else {
         score = 20f;
      }

      filterFaceInfoResult.setScore((int) score);

      operateMat.release();
      resultMat.release();
      hsvImage.release();
      firstMask.release();
      hsvMat.release();
      saturation.release();
      histEq.release();
      mask.release();
      kernel.release();

      filterFaceInfoResult.setFilterImage(filterBitmap);

   }

}
