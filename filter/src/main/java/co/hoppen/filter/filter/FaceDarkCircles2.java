package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Picture;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles2 extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
           Mat operateMat = new Mat();
           Utils.bitmapToMat(getFaceAreaImage(),operateMat);
           Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);


           //-----------眼部mask-----------------------
           Mat gray = new Mat();
           Imgproc.cvtColor(operateMat,gray, Imgproc.COLOR_RGB2GRAY);
//
//           Mat mask = new Mat();
//           Imgproc.threshold(gray,mask,0,255,Imgproc.THRESH_OTSU);
//
//           Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7, 7));
//
//           Imgproc.erode(mask,mask,kernel);
           //Imgproc.equalizeHist(gray,gray);

           //-----------眼部mask-----------------------

           //-----------增亮-----------------------
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
           Mat outputImage = new Mat();
           Imgproc.cvtColor(hsvImage, outputImage, Imgproc.COLOR_HSV2RGB);
           //-----------增亮-----------------------


           //-----------分离HSV---------------------

           Imgproc.cvtColor(operateMat,gray,Imgproc.COLOR_RGB2GRAY);

           Mat hsvMat = new Mat();
           Imgproc.cvtColor(operateMat,hsvMat,Imgproc.COLOR_RGB2HSV);



               //灰度程度阶梯
               float cs0=0.00f;
               float cs1=40.00f;//15
               float cs2=115.00f;
               float cs3=190.00f;
               float cs4=255.00f;

               //H值
               float a0=0.00f;
               float a1=10.00f/180.00f;
               float a2=14.00f/180.00f;
               float a3=10.00f/180.00f;
               float a4=0.00f;

               //S值
               float b0=0f;
               float b1=0.01f;
               float b2=0.5f;
               float b3=0.99f;
               float b4=1f;

               //V值
               float c0=1.00f;
               float c1=240.00f/255.00f;
               float c2=125.00f/255.00f;
               float c3=100.00f/255.00f;
               float c4=96.00f/255.00f;

               Mat mixHMat = new Mat(outputImage.size(),CvType.CV_32FC3);
               List<Mat> hsvSplit = new ArrayList<>();
               Core.split(mixHMat,hsvSplit);
               Mat matH = hsvSplit.get(0);
               matH.convertTo(matH,CvType.CV_32FC1,1.0/180d);
               Mat matS = hsvSplit.get(1);
               matS.convertTo(matS,CvType.CV_32FC1,1.0/255d);
               Mat matV = hsvSplit.get(2);
               matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);

               float[] hsvHP = new float[matH.channels() * matH.cols()];

               float[] hsvSP = new float[matS.channels() * matS.cols()];

               float[] hsvVP = new float[matV.channels() * matV.cols()];

               byte[] p = new byte[gray.channels() * gray.cols()];

               int totalCount = 0;

               for (int h = 0; h < gray.rows(); h++) {
                   gray.get(h, 0, p);
                   matH.get(h,0,hsvHP);
                   matS.get(h,0,hsvSP);
                   matV.get(h,0,hsvVP);
                   for (int w = 0; w < gray.cols(); w++) {
                       int index = gray.channels() * w;

                       float hsvH = 0;
                       float hsvS = 0;
                       float hsvV = 0;

                       int hsvIndex = matH.channels() * w;

                       if (p[index]!=-1){
                           int oriGray = (int)p[index] & 0xff;
                           if (oriGray!=0){
                               totalCount++;
                               //灰度阈值（percent 0.5 = 127.5）
                               float percent = 0.75f;
                               int grayColor = (int) (percent * oriGray + 255 * (1 - percent));
                               if (grayColor>=cs2&&grayColor<cs3){
                                   //115~190
                                   if (grayColor>=115&&grayColor<120){ //115~120
                                       hsvH = a1 - (((a2-a1)*cs1)/(cs2-cs1)) + (((a2-a1) * grayColor) / (cs2-cs1));
                                       hsvS = b1 - (((b2-b1)*cs1)/(cs2-cs1)) + (((b2-b1) * grayColor) / (cs2-cs1));
                                       hsvV = c1 - (((c2-c1)*cs1)/(cs2-cs1)) + (((c2-c1) * grayColor) / 255f);
                                   }else if (grayColor>=120&&grayColor<140){ //120~130
                                       hsvH = a2 -(((a3-a2)*cs2)/(cs3-cs2)) + (((a3-a2) * grayColor) / (cs3-cs2));
                                       hsvS = b2 -(((b3-b2)*cs2)/(cs3-cs2)) + (((b3-b2) * grayColor) / (cs3-cs2));
                                       hsvV = c1 -(((c3-c1)*cs1)/(cs3-cs1)) + (((c3-c1) * grayColor) / 255f);
                                   }
                               }
                           }
                       }
                       hsvHP[hsvIndex] = hsvH;
                       hsvSP[hsvIndex] = hsvS;
                       hsvVP[hsvIndex] = hsvV;
                   }
                   matH.put(h,0,hsvHP);
                   matS.put(h,0,hsvSP);
                   matV.put(h,0,hsvVP);
                   gray.put(h, 0, p);
               }

               Mat brownMat = new Mat();
               hsvSplit.clear();
               matH.convertTo(matH,CvType.CV_8UC1,180.0d);
               matS.convertTo(matS,CvType.CV_8UC1,255.0d);
               matV.convertTo(matV,CvType.CV_8UC1,255.0d);

               hsvSplit.add(matH);
               hsvSplit.add(matS);
               hsvSplit.add(matV);
               Core.merge(hsvSplit,brownMat);

               Mat result = new Mat();
               Imgproc.cvtColor(brownMat,result,Imgproc.COLOR_HSV2RGB);


              int resultChannels = result.channels();
              int col = result.cols();
              int row = result.rows();

               p = new byte[resultChannels * col];
               byte[] p2 = new byte[operateMat.channels() * col];


               Utils.bitmapToMat(getOriginalImage(),operateMat);
               Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);

               int count = 0;

               for (int h = 0; h < row; h++) {
                   result.get(h, 0, p);
                   operateMat.get(h,0,p2);
                   for (int w = 0; w < col; w++) {
                       int index = resultChannels * w;
                       int r = p[index] & 0xff;
                       int g = p[index + 1] & 0xff;
                       int b = p[index + 2] & 0xff;
                       if (r!=0 && g!=0 && b!=0){
                           p2[index] = (byte) r;
                           p2[index + 1] = (byte) g;
                           p2[index + 2] = (byte) b;
                           count++;
                       }
                   }
                   result.put(h, 0, p2);
               }

               //LogUtils.e(count,totalCount,totalCount / count);
               //6% 12% 7% 6 %

               float score = 85;
               float areaPercent = totalCount>count?count * 100f /totalCount:100f;

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
           Utils.matToBitmap(result,resultBitmap);

                   operateMat.release();
                   gray.release();
                   hsvImage.release();
                   outputImage.release();
                   hsvMat.release();
                   matH.release();
                   matS.release();
                   matV.release();
                   brownMat.release();
                   result.release();
            Mat areaMat = new Mat();
            Utils.bitmapToMat(getFaceAreaImage(),areaMat);
            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
            areaMat.release();
            //filterInfoResult.setDataTypeString(getFilterDataType(),"");
            filterInfoResult.setFilterBitmap(resultBitmap);
            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_EYE_BOTTOM};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.AREA;
   }

    /**
     *
     * @param color
     * @return 0~255
     */
   private int colorDepth(int color){
       return (int) (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color));
   }


}
