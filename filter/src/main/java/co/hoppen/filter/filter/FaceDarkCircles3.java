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

/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles3 extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
           Mat oriMat = new Mat();
           Utils.bitmapToMat(getOriginalImage(),oriMat);
           Imgproc.cvtColor(oriMat,oriMat,Imgproc.COLOR_RGBA2RGB);

//           Mat mask = new Mat();
//           Imgproc.cvtColor(operateMat,mask,Imgproc.COLOR_RGBA2GRAY);


           Mat operMat = new Mat();
           Utils.bitmapToMat(getFaceAreaImage(),operMat);
           Imgproc.cvtColor(operMat,operMat,Imgproc.COLOR_RGBA2RGB);

           Mat gray = new Mat();
           Imgproc.cvtColor(operMat,gray,Imgproc.COLOR_RGB2GRAY);

           //灰度程度阶梯
           float cs0=0.00f;
           float cs1=15.00f;//15
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

           byte[] p = new byte[gray.channels() * gray.cols()];

           for (int h = 0; h < gray.rows(); h++) {
               gray.get(h, 0, p);
               for (int w = 0; w < gray.cols(); w++) {
                   int index = gray.channels() * w;
                   float percent = 0.5f;
                   int G = (int)p[index] & 0xff;
                   if (G!=0){
                       G = (int) (percent * G + 255 * (1 - percent));
                   }
                   p[index] = (byte) G;
               }
               gray.put(h, 0, p);
           }

           Mat mixHMat = new Mat(operMat.size(),CvType.CV_32FC3);
           List<Mat> hsvSplit = new ArrayList<>();
           Core.split(mixHMat,hsvSplit);
           Mat matH = hsvSplit.get(0);
           matH.convertTo(matH,CvType.CV_32FC1,1.0/180d);
           Mat matS = hsvSplit.get(1);
           matS.convertTo(matS,CvType.CV_32FC1,1.0/255d);
           Mat matV = hsvSplit.get(2);
           matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);

           float[] hsvHP = new float[matH.channels() * gray.cols()];
           float[] hsvSP = new float[matS.channels() * gray.cols()];
           float[] hsvVP = new float[matV.channels() * gray.cols()];

           float maxH = 0;
           float minH = 1;
           float maxS = 0;
           float minS = 1;
           float maxV = 0;
           float minV = 1;



           for (int h = 0; h < gray.rows(); h++) {
               gray.get(h, 0, p);
               matH.get(h,0,hsvHP);
               matS.get(h,0,hsvSP);
               matV.get(h,0,hsvVP);
               for (int w = 0; w < gray.cols(); w++) {
                   int index = gray.channels() * w;
                   int grayP = p[index] & 0xff;
                   float hsvH = 0;
                   float hsvS = 0;
                   float hsvV = 0;
                   if (grayP!=0){
                       if (grayP<cs1){
                           //0~15
                           hsvH = a0 - (((a1-a0)*cs0) / (cs1-cs0)) + (((a1-a0) * grayP)/(cs1-cs0));
                           hsvS = b0 - (((b1-b0)*cs0) /(cs1-cs0)) + (((b1-b0) * grayP)/(cs1-cs0));
                           hsvV = c0 - (((c1-c0)*cs0) /(cs1-cs0)) + (((c1-c0) * grayP)/(cs1-cs0));
                       }else if (grayP>=cs1&&grayP<cs2){
                           //15~115
                           hsvH = a1 - (((a2-a1)*cs1)/(cs2-cs1)) + (((a2-a1) * grayP) / (cs2-cs1));
                           hsvS = b1 - (((b2-b1)*cs1)/(cs2-cs1)) + (((b2-b1) * grayP) / (cs2-cs1));
                           hsvV = c1 - (((c2-c1)*cs1)/(cs2-cs1)) + (((c2-c1) * grayP) / 255f);
                       }else if (grayP>=cs2&&grayP<cs3){
                           //115~190
                           if (grayP<=150){
                               //-0.033 + (-2.53 / )
                               hsvH = a2 -(((a3-a2)*cs2)/(cs3-cs2)) + (((a3-a2) * grayP) / (cs3-cs2));
                               hsvS = b2 -(((b3-b2)*cs2)/(cs3-cs2)) + (((b3-b2) * grayP) / (cs3-cs2));
                               hsvV = c1 -(((c3-c1)*cs1)/(cs3-cs1)) + (((c3-c1) * grayP) / 255f);
                           }

//                            if (maxH<hsvH)maxH = hsvH;
//                            if (minH>hsvH)minH = hsvH;
//
//                           if (maxS<hsvS)maxS = hsvS;
//                           if (minS>hsvS)minS = hsvS;
//
//                           if (maxV<hsvV)maxV = hsvV;
//                           if (minV>hsvV)minV = hsvV;


                       }else if (grayP>=cs3&&grayP<cs4){
                           //190~255
                           hsvH = a3 - (((a4-a3)*cs3) / (cs4-cs3)) + (((a4-a3)*grayP) / (cs4-cs3));
                           hsvS = b3 - (((b4-b3)*cs3) / (cs4-cs3)) + (((b4-b3)*grayP) / (cs4-cs3));
                           hsvV = c3 - (((c4-c3)*cs1) / (cs4-cs3)) + (((c4-c3)*grayP) / 255f);
                       }
                   }
                   int hsvIndex = matH.channels() * w;
                   hsvHP[hsvIndex] = hsvH;
                   hsvSP[hsvIndex] = hsvS;
                   hsvVP[hsvIndex] = hsvV;
               }
               matH.put(h,0,hsvHP);
               matS.put(h,0,hsvSP);
               matV.put(h,0,hsvVP);
               gray.put(h, 0, p);
           }

           LogUtils.e("MAX "+maxH+" "+maxH+" "+maxV,"MIN "+minH+" "+minS+" "+minV);

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

           p = new byte[brownMat.channels() * brownMat.cols()];
           byte [] oriP = new byte[oriMat.channels() * oriMat.cols()];

           for (int h = 0; h < brownMat.rows(); h++) {
               brownMat.get(h, 0, p);
               oriMat.get(h, 0, oriP);
               for (int w = 0; w < brownMat.cols(); w++) {
                   int index = brownMat.channels() * w;
                   int R = (int)p[index] & 0xff;
                   int G = (int)p[index + 1] & 0xff;
                   int B = (int)p[index + 2] & 0xff;
                   if (R == 0 && G ==0 && B ==0){
                       R = (int)oriP[index] & 0xff;
                       G = (int)oriP[index + 1] & 0xff;
                       B = (int)oriP[index + 2] & 0xff;
                   }
                   p[index] = (byte) R;
                   p[index + 1] = (byte) G;
                   p[index + 2] = (byte) B;
               }
               brownMat.put(h, 0, p);
           }


           Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
           Utils.matToBitmap(brownMat,resultBitmap);

            Mat areaMat = new Mat();
            Utils.bitmapToMat(getFaceAreaImage(),areaMat);
            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
            areaMat.release();
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
