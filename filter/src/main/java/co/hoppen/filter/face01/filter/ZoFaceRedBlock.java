package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

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
public class ZoFaceRedBlock extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Mat operateMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(),operateMat);
      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);

      Mat labMat = new Mat();
      Imgproc.cvtColor(operateMat,labMat,Imgproc.COLOR_RGB2Lab);

      Mat hsvMat = new Mat(operateMat.size(), CvType.CV_8UC3);

      Mat faceMask = getFaceMask();

      byte[] p = new byte[operateMat.channels() * operateMat.cols()];
      byte[] labP = new byte[labMat.channels() * labMat.cols()];
      byte[] hsvP = new byte[hsvMat.channels() * hsvMat.cols()];
      byte[] maskP = new byte[faceMask.channels() * faceMask.cols()];

      int count = 0;

      float level1 = 0;
      float level2 = 0;
      float level3 = 0;
      float level4 = 0;

      for (int h = 0; h < operateMat.rows(); h++) {
         operateMat.get(h, 0, p);
         labMat.get(h, 0, labP);
         hsvMat.get(h, 0, hsvP);
         faceMask.get(h,0,maskP);
         for (int w = 0; w < operateMat.cols(); w++) {
            int index = operateMat.channels() * w;
            int maskGray = maskP[w] & 0xff;
            if (maskGray!=0){
               int g = p[index + 1] & 0xff;
               int b = p[index + 2] & 0xff;
               int a = labP[index + 1]& 0xff;
               int mask = a - (g/10) - (b/15) - 30;
               //int maskIndex = maskMat.channels() * w;

               float gray = (float) (-0.00009685f * Math.pow(mask,3) + (0.03784f * Math.pow(mask,2)) - (2.673f * mask) + 48.12f);
               gray = gray<=0?0:gray>=255?255:gray;

               if (gray<25){
                  hsvP[index] = (byte) 176;
                  hsvP[index + 1] = (byte) 30;
                  hsvP[index + 2] = (byte) 241;
               }else if (gray>=25){
                  hsvP[index] = (byte) 176;
                  hsvP[index + 1] = (byte) (30 + (180 * ((gray-25) / (255 - 25))));
                  hsvP[index + 2] = (byte) (241 - (200 * ((gray - 25) / (255 - 25))));
                  if (gray<50){
                     level1++;
                  }else if (gray<100){
                     level2++;
                  }else if (gray<150){
                     level3++;
                  }else if (gray<=255){
                     level4++;
                  }
               }
               count++;
            }else {
               hsvP[index] = 0;
               hsvP[index + 1] = 0;
               hsvP[index + 2] = 0;
            }
         }
         hsvMat.put(h, 0, hsvP);
      }

      float score1 = 35;
      float countPercent = (level1 / count + level2 / count) * 100f;
      if (countPercent<=50){
         score1 = ((1-(countPercent / 50f)) * 15f)  + 20f;
      }else if (countPercent>50 &&countPercent<=90){
         score1 = ((1-((countPercent - 50f) / 40f))* 10f)  + 10f;
      }else if (countPercent> 90 && countPercent<=100){
         score1 = ((1-((countPercent - 90f) / 10f))* 10f);
      }

      float score2 = 50;
      float level2Percent = level2 / count * 100f;
      if (level2Percent<=20){
         score2 = ((1-(level2Percent / 20f)) * 10f)  + 40f;
      }else if (level2Percent>20 &&level2Percent<=40){
         score2 = ((1-((level2Percent - 20f) / 20f)) * 10f)  + 30f;
      }else if (level2Percent>40 &&level2Percent<=50){
         score2 = ((1-((level2Percent - 40f) / 10f)) * 20f) + 10f;
      }else score2 = 10f;


      filterFaceInfoResult.setScore((int) (score1 + score2));

      CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
      List<Mat> hsvList = new ArrayList<>();
      Core.split(hsvMat,hsvList);
      clahe.apply(hsvList.get(1),hsvList.get(1));
      Core.merge(hsvList,hsvMat);

      Imgproc.cvtColor(hsvMat,operateMat,Imgproc.COLOR_HSV2RGB);

      lastStrengthen(operateMat);

      Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

      Utils.matToBitmap(operateMat,resultBitmap);

      operateMat.release();
      labMat.release();
      hsvMat.release();
      faceMask.release();

      filterFaceInfoResult.setFilterImage(resultBitmap);

   }

   private void lastStrengthen(Mat operateMat){
      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);
      int channels = operateMat.channels();
      int col = operateMat.cols();
      int row = operateMat.rows();
      int type = operateMat.type();
      Log.d("TAG", "通道数： " + channels + " 宽度：" + col + " 高度：" + row + " 类型：" + type);

      //用于保存一行像素的数据，单个像素点的数据*一行的像素
      byte[] p = new byte[channels * col];

      int r = 0, g = 0, b = 0;

      for (int h = 0; h < row; h++) {
         //col = 0 表示这是一行的数据，把一行的像素点读取到p数组来
         operateMat.get(h, 0, p);
         for (int w = 0; w < col; w++) {
            //当前操作像素数组索引，通道数x当前像素位置 = 当前索引位置
            int index = channels * w;

            r = p[index] & 0xff;
            g = p[index + 1] & 0xff;
            b = p[index + 2] & 0xff;

            float R = (float) (-0.00003566f * Math.pow(r,3) + 0.01467f * Math.pow(r,2) - 0.4392f * r - 6.082f);
            float G = (float) (-0.00003566f * Math.pow(g,3) + 0.01467f * Math.pow(g,2) - 0.4392f * g - 6.082f);
            float B = (float) (-0.00003566f * Math.pow(b,3) + 0.01467f * Math.pow(b,2) - 0.4392f * b - 6.082f);

            p[index] = (byte) rgbRange(R);
            p[index + 1] = (byte) rgbRange(G);
            p[index + 2] = (byte) rgbRange(B);

         }
         //同上，0表示是一行的数据，不再通过某个像素点写入
         operateMat.put(h, 0, p);
      }
   }

   private int rgbRange(float value){
      return value<=0?0:value>=255?255:(int)value;
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
