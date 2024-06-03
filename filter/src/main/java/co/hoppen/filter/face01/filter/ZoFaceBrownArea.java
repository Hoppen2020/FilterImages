package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceBrownArea extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {

      Mat operateMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(),operateMat);

      Core.bitwise_not(operateMat,operateMat);
      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);

      CLAHE clahe = Imgproc.createCLAHE(3.0d,new Size(8,8));
      clahe.apply(operateMat,operateMat);

      Mat faceMaskMat = getFaceMask();

      byte[] maskP = new byte[faceMaskMat.channels() * faceMaskMat.cols()];


      int channels = operateMat.channels();
      int col = operateMat.cols();
      int row = operateMat.rows();

      byte[] p = new byte[channels * col];

      float totalSkin = 0;

      for (int h = 0; h < row; h++) {
         operateMat.get(h, 0, p);
         faceMaskMat.get(h,0,maskP);
         for (int w = 0; w < col; w++) {
            int maskGray = maskP[w] & 0xff;
            int index = channels * w;
            if (maskGray!=0){
               float percent = 0.5f;
               int gray = (int) (percent * ((int)p[index] & 0xff) + 255 * (1 - percent));
               p[index] = (byte) gray;
               totalSkin++;
            }else {
               p[index] = -1;
            }
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

      Mat mixHMat = new Mat(operateMat.size(), CvType.CV_32FC3);
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


      float count = 0;

      for (int h = 0; h < row; h++) {
         operateMat.get(h, 0, p);
         matH.get(h,0,hsvHP);
         matS.get(h,0,hsvSP);
         matV.get(h,0,hsvVP);
         for (int w = 0; w < col; w++) {
            int index = channels * w;

            float hsvH = 0;
            float hsvS = 0;
            float hsvV = 0;

            int hsvIndex = matH.channels() * w;

            if (p[index]!=-1){
               int gray = p[index] & 0xff;
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
                  count++;
               }else if (gray>=cs3&&gray<cs4){
                  hsvH = a3 - (((a4-a3)*cs3) / (cs4-cs3)) + (((a4-a3)*gray) / (cs4-cs3));
                  hsvS = b3 - (((b4-b3)*cs3) / (cs4-cs3)) + (((b4-b3)*gray) / (cs4-cs3));
                  hsvV = c3 - (((c4-c3)*cs1) / (cs4-cs3)) + (((c4-c3)*gray) / 255f);
                  count++;
               }
            }
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

      faceMaskMat.release();
      //0.63 0.64 0.64 0.64 0.64 0.71
      //level1- 0~0.4 level2- 0.4~0.5 level3- 0.5~0.6 level4-0.6~1
      float score = 85f;
      float percent = count * 100f / totalSkin;

      if (percent<=40){
         //80~85
         score = ((percent / 40) * 5) + 80f;
      }else if (percent>40 && percent<=50){
         //60~80
         score = (((percent-40) / 10f) * 20f) + 60f;
      }else if (percent>50 && percent<=70){
         //40~60
         score = (((percent-50) / 20f) * 20f) + 40f;
      }else if (percent>70 && percent<=80){
         //20~40f
         score = (((percent-70f) / 10f) * 20f) + 20f;
      }else {
         //80%↑
         score = 20f;
      }

      filterFaceInfoResult.setScore((int) score);

      Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(),getOriginalImage().getConfig());

      Utils.matToBitmap(brownMat,resultBitmap);

      filterFaceInfoResult.setFilterImage(resultBitmap);

   }

   private Mat getFaceMask(){
      Bitmap filterPartsImages = getFilterPartsImages();
      int width = filterPartsImages.getWidth();
      int height = filterPartsImages.getHeight();
      int count = width * height;
      int [] partsPixels = new int[count];

      filterPartsImages.getPixels(partsPixels,0,width,0,0,width,height);

      for (int i = 0; i <partsPixels.length ; i++) {
         int color = partsPixels[i];
         if (Color.alpha(color)==0){
            partsPixels[i] = Color.BLACK;
         }else {
            partsPixels[i] = Color.WHITE;
         }
      }
      Bitmap maskBitmap = Bitmap.createBitmap(width,height,getOriginalImage().getConfig());
      maskBitmap.setPixels(partsPixels,0,width, 0, 0, width, height);
      Mat mat = new Mat();
      Utils.bitmapToMat(maskBitmap,mat);
      List<Mat> rgbList = new ArrayList<>();
      Core.split(mat,rgbList);
      rgbList.get(1).release();
      rgbList.get(2).release();
      if (!maskBitmap.isRecycled())maskBitmap.recycle();
      return rgbList.get(0);
   }

}