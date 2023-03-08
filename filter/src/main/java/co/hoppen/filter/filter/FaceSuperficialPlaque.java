package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
public class FaceSuperficialPlaque extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
         Mat operateMat = getPlSkin();
         //Utils.bitmapToMat(getOriginalImage(),operateMat);
         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);

         List<Mat> splitList = new ArrayList<>();
         Core.split(operateMat,splitList);

         CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
         clahe.apply(splitList.get(1),splitList.get(1));

         Core.merge(splitList,operateMat);
         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);
         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2GRAY);

         Imgproc.equalizeHist(operateMat,operateMat);

         //CLAHE clahe = Imgproc.createCLAHE(2.0d, new Size(8, 8));
         clahe.apply(operateMat,operateMat);

         double v = Core.mean(operateMat).val[0];

//         LogUtils.e(v);

         byte [] p = new byte[operateMat.channels() * operateMat.cols()];

         int grayTotal = 0;
         int grayCount = 0;
         int avgGray = 0;

         int max = -1;
         int min = -1;

         for (int h = 0; h < operateMat.rows(); h++) {
            operateMat.get(h,0,p);
            for (int w = 0; w < operateMat.cols(); w++) {
               int index = operateMat.channels() * w;;
               int value =  p[index] & 0xff;
               if (value>10){
                  grayTotal+=value;
                  grayCount++;
                  if (max==-1){
                     max = value;
                  }else if (value>max){
                     max = value;
                  }
                  if (min==-1){
                     min = value;
                  }else if (value<min){
                     min = value;
                  }
               }
            }
         }
//         if (grayCount!=0){
//            avgGray = grayTotal / grayCount;
//         }

         //LogUtils.e("avg: "+avgGray,"total: "+grayTotal,"count: "+grayCount,"max: "+max,"min: "+min);

         Mat mask = new Mat(operateMat.size(),CvType.CV_8UC1);

         float count = 0;

         for (int h = 0; h < operateMat.rows(); h++) {
            operateMat.get(h,0,p);
            for (int w = 0; w < operateMat.cols(); w++) {
               int index = operateMat.channels() * w;
               int value =  p[index] & 0xff;
               if (value>10){
                  float mix = ((float)value - min) / ((float)max -min)* 255f;
                  //LogUtils.e(mix);
                  value = mix>255?255:mix<0?0:(int)mix;
                  if (value<=(min + max) / 2){
                     value = Color.WHITE;
                      count++;
                  }else value = Color.BLACK;
               }else {
                  value = Color.BLACK;
               }
               p[index] = (byte) value;
            }
            mask.put(h,0,p);
         }


         //0.542 0.48  0.5  0.51 0.51
       LogUtils.e(count, FaceSkinUtils.getSkinArea(),count / FaceSkinUtils.getSkinArea());

       float score = 85;
       float areaPercent = FaceSkinUtils.getSkinArea()>count?count * 100f /FaceSkinUtils.getSkinArea():100f;
       //level1——25~40 level2——40~50 level3——50~60 level4——60~100
       //level1——0~5 level2——5~8 level3——8~15 level4——15~20 20~30 30~40

       if (areaPercent<=5){
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


         Utils.bitmapToMat(getOriginalImage(),operateMat);

         Core.add(operateMat,new Scalar(40,20,20,255),operateMat,mask);

         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

         Utils.matToBitmap(operateMat,resultBitmap);

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
