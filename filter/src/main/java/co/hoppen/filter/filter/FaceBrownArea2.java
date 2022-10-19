package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceBrownArea2 extends FaceFilter{

   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
      try {
         Mat operateMat = new Mat();
         Utils.bitmapToMat(getOriginalImage(),operateMat);

         Core.bitwise_not(operateMat,operateMat);
         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);

         CLAHE clahe = Imgproc.createCLAHE(3.0d,new Size(8,8));
         clahe.apply(operateMat,operateMat);


         int channels = operateMat.channels();
         int col = operateMat.cols();
         int row = operateMat.rows();

         byte[] p = new byte[channels * col];

         for (int h = 0; h < row; h++) {
            operateMat.get(h, 0, p);
            for (int w = 0; w < col; w++) {
               int index = channels * w;
               float percent = 0.5f;
               int gray = (int) (percent * ((int)p[index] & 0xff) + 255 * (1 - percent));
               p[index] = (byte) gray;
            }
            operateMat.put(h, 0, p);
         }

         clahe.apply(operateMat,operateMat);


         float cs0=0.00f;
         float cs1=15.00f;
         float cs2=115.00f;
         float cs3=190.00f;
         float cs4=255.00f;
         float a0=0.00f;
         float a1=10.00f/180.00f;
         float a2=14.00f/180.00f;
         float a3=10.00f/180.00f;
         float a4=0.00f;
         float b0=0f;
         float b1=0.01f;
         float b2=0.5f;
         float b3=0.99f;
         float b4=1f;
         float c0=1.00f;
         float c1=240.00f/255.00f;
         float c2=125.00f/255.00f;
         float c3=100.00f/255.00f;
         float c4=96.00f/255.00f;

         Mat mixHMat = new Mat(operateMat.size(),CvType.CV_32FC3);
         List<Mat> hsvSplit = new ArrayList<>();
         Core.split(mixHMat,hsvSplit);
         Mat matH = hsvSplit.get(0);
         matH.convertTo(matH,CvType.CV_32FC1,1.0/180d);
         Mat matS = hsvSplit.get(1);
         matS.convertTo(matS,CvType.CV_32FC1,1.0/255d);
         Mat matV = hsvSplit.get(2);
         matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);


         float[] hsvHP = new float[matH.channels() * col];

         float[] hsvSP = new float[matS.channels() * col];

         float[] hsvVP = new float[matV.channels() * col];


         for (int h = 0; h < row; h++) {
            operateMat.get(h, 0, p);
            matH.get(h,0,hsvHP);
            matS.get(h,0,hsvSP);
            matV.get(h,0,hsvVP);
            for (int w = 0; w < col; w++) {
               int index = channels * w;
               int gray = p[index] & 0xff;
               float hsvH = 0;
               float hsvS = 0;
               float hsvV = 0;

               if (gray<cs1){
                  hsvH = a0 - (((a1-a0)*cs0) / (cs1-cs0)) + (((a1-a0) * gray)/(cs1-cs0));
                  hsvS = b0 - (((b1-b0)*cs0) /(cs1-cs0)) + (((b1-b0) * gray)/(cs1-cs0));
                  hsvV = c0 - (((c1-c0)*cs0) /(cs1-cs0)) + (((c1-c0) * gray)/(cs1-cs0));
               }else if (gray>=cs1&&gray<cs2){
                  hsvH = a1 - (((a2-a1)*cs1)/(cs2-cs1)) + (((a2-a1) * gray) / (cs2-cs1));
                  hsvS = b1 - (((b2-b1)*cs1)/(cs2-cs1)) + (((b2-b1) * gray) / (cs2-cs1));
                  hsvV = c1 - (((c2-c1)*cs1)/(cs2-cs1)) + (((c2-c1) * gray) / 255f);
               }else if (gray>=cs2&&gray<cs3){
                  hsvH = a2 -(((a3-a2)*cs2)/(cs3-cs2)) + (((a3-a2) * gray) / (cs3-cs2));
                  hsvS = b2 -(((b3-b2)*cs2)/(cs3-cs2)) + (((b3-b2) * gray) / (cs3-cs2));
                  hsvV = c1 -(((c3-c1)*cs1)/(cs3-cs1)) + (((c3-c1) * gray) / 255f);
               }else if (gray>=cs3&&gray<cs4){
                  hsvH = a3 - (((a4-a3)*cs3) / (cs4-cs3)) + (((a4-a3)*gray) / (cs4-cs3));
                  hsvS = b3 - (((b4-b3)*cs3) / (cs4-cs3)) + (((b4-b3)*gray) / (cs4-cs3));
                  hsvV = c3 - (((c4-c3)*cs1) / (cs4-cs3)) + (((c4-c3)*gray) / 255f);
               }
               int hsvIndex = matH.channels() * w;
               hsvHP[hsvIndex] = hsvH;
               hsvSP[hsvIndex] = hsvS;
               hsvVP[hsvIndex] = hsvV;
            }
            matH.put(h,0,hsvHP);
            matS.put(h,0,hsvSP);
            matV.put(h,0,hsvVP);
            operateMat.put(h, 0, p);
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

         Imgproc.cvtColor(brownMat,brownMat,Imgproc.COLOR_HSV2RGB);

         Mat areaMat = new Mat();

         Utils.bitmapToMat(getFaceAreaImage(),areaMat);

         filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));


         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(brownMat,resultBitmap);

         filterInfoResult.setFilterBitmap(resultBitmap);
         filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
      }catch (Exception e){
         LogUtils.e(e.toString());
         filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
      }
      return filterInfoResult;
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_SKIN};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.AREA;
   }

//   convertTo(lab,CvType.CV_32F,1/255f);
//convertTo(matB32,CvType.CV_8UC1,255/255);

}
