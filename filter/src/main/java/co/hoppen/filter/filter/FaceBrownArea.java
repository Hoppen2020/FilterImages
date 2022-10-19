package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceBrownArea extends FaceFilter{

   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
      try {
         Bitmap operateBitmap = getFaceAreaImage();
         Mat operateMat = new Mat();
         Utils.bitmapToMat(operateBitmap,operateMat);

         Mat labMat = new Mat();
         Imgproc.cvtColor(operateMat,labMat,Imgproc.COLOR_RGB2Lab);


         List<Mat> labSplit = new ArrayList<>();
         Core.split(labMat,labSplit);
         Mat matB = labSplit.get(2);
         matB.convertTo(matB,CvType.CV_32FC1,1.0/255d);

         int channels = matB.channels();
         int col = matB.cols();
         int row = matB.rows();
         int type = matB.type();
         LogUtils.e("TAG", "通道数： " + channels + " 宽度：" + col + " 高度：" + row + " 类型：" + type);


         float percent = 0f;

         float minB = 0;
         float maxB = 0;


         //用于保存一行像素的数据，单个像素点的数据*一行的像素
         float[] p = new float[channels * col];

         List<Float> sortList = new ArrayList<>();

         for (int h = 0; h < row; h++) {
            matB.get(h, 0, p);
            for (int w = 0; w < col; w++) {
               int index = channels * w;
               sortList.add(p[index]);
            }
         }
         float [] sortMatB = new float[sortList.size()];
         for (int i = 0; i < sortList.size(); i++) {
            sortMatB[i] = sortList.get(i);
         }
         ArrayUtils.sort(sortMatB);
         minB = percent==0?sortMatB[0]:sortMatB[0] * percent;
         maxB = percent==0?sortMatB[sortList.size() -1 ]:sortMatB[sortList.size() -1 ] * (1f-percent);

         LogUtils.e(minB,maxB);

         for (int h = 0; h < row; h++) {
            //col = 0 表示这是一行的数据，把一行的像素点读取到p数组来
            matB.get(h, 0, p);
            for (int w = 0; w < col; w++) {
               //当前操作像素数组索引，通道数x当前像素位置 = 当前索引位置
               int index = channels * w;
               float value = p[index];
               if (value<minB){
                  p[index] = 0;
               }else if (value>maxB){
                  p[index] = 1.0f;
               }else {
                  p[index] = (value - minB) / (maxB - minB);
               }
            }
            //同上，0表示是一行的数据，不再通过某个像素点写入
            matB.put(h, 0, p);
         }

         Mat hsvMat = new Mat();
         Imgproc.cvtColor(operateMat,hsvMat,Imgproc.COLOR_RGB2HSV);

         List<Mat> hsvSplit = new ArrayList<>();
         Core.split(hsvMat,hsvSplit);
         Mat matH = hsvSplit.get(0);
         matH.convertTo(matH,CvType.CV_32FC1,1.0/255d);
         Mat matV = hsvSplit.get(2);
         matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);

         hsvSplit.clear();
         hsvSplit.add(new Mat(operateMat.size(),CvType.CV_32FC1,new Scalar(0.0708d)));
         hsvSplit.add(matB);
         hsvSplit.add(matV);

         Mat mixMat = new Mat();
         Core.merge(hsvSplit,mixMat);

         mixMat.convertTo(mixMat,CvType.CV_8UC3,255.0d);
         Imgproc.cvtColor(mixMat,mixMat,Imgproc.COLOR_HSV2RGB);
         Imgproc.cvtColor(mixMat,labMat,Imgproc.COLOR_RGB2Lab);

         byte[] mixP = new byte[mixMat.channels() * col];

         byte[] labP = new byte[labMat.channels() * col];

         channels = mixMat.channels();

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

         hsvSplit = new ArrayList<>();
         Core.split(mixHMat,hsvSplit);
         matH = hsvSplit.get(0);
         matH.convertTo(matH,CvType.CV_32FC1,1.0/180d);
         Mat matS = hsvSplit.get(1);
         matS.convertTo(matS,CvType.CV_32FC1,1.0/255d);
         matV = hsvSplit.get(2);
         matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);


         float[] hsvHP = new float[matH.channels() * col];

         float[] hsvSP = new float[matS.channels() * col];

         float[] hsvVP = new float[matV.channels() * col];


         for (int h = 0; h < row; h++) {
            mixMat.get(h, 0, mixP);
            labMat.get(h,0,labP);

            matH.get(h,0,hsvHP);
            matS.get(h,0,hsvSP);
            matV.get(h,0,hsvVP);

            for (int w = 0; w < col; w++) {
               int index = channels * w;
               int b =labP[index + 2] & 0xff;
               int G = mixP[index + 1] & 0xff;
               int B = mixP[index +2] &0xff;
               int mask =  b - (G / 20) - (B /25) - 30;
               float gray = (float) (-0.00009685f * Math.pow(mask,3) + 0.03784f * Math.pow(mask,2) -2.673 * mask + 48.12f);
               if (gray<=0) {
                  gray = 0;
               }else if (gray>=255){
                  gray = 255;
               }
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

         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(brownMat,resultBitmap);

         operateMat.release();
         labMat.release();
         matB.release();
         hsvMat.release();
         matH.release();
         matV.release();
         mixMat.release();
         mixHMat.release();
         matS.release();
         brownMat.release();

         Mat areaMat = new Mat();
         Utils.bitmapToMat(getFaceAreaImage(),areaMat);
         filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));


         filterInfoResult.setFilterBitmap(resultBitmap);

         filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
      }catch (Exception e){
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
