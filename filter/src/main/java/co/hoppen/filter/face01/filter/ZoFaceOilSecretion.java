package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceOilSecretion extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Bitmap originalImage = getOriginalImage();
      Bitmap bitmap =  getFilterPartsImages();
      Mat yuvMat = new Mat();
      Utils.bitmapToMat(bitmap,yuvMat);
      Imgproc.cvtColor(yuvMat,yuvMat,Imgproc.COLOR_RGB2GRAY);
      Mat detect = new Mat();
      List<Mat> channels = new ArrayList<>();
      Core.split(yuvMat,channels);
      Mat outputMark = channels.get(0);
      Imgproc.threshold(outputMark,outputMark,0,255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
      yuvMat.copyTo(detect,outputMark);
      Utils.matToBitmap(detect,bitmap);

      yuvMat.release();
      detect.release();
      outputMark.release();

      int width = originalImage.getWidth();
      int height = originalImage.getHeight();

      int [] pixels = new int[width * height];
      int [] dst = new int[width * height];
      int [] original = new int[width * height];

      int [] area = new int[width * height];

      bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
      originalImage.getPixels(original, 0, width, 0, 0, width, height);

      int totalGray = 0;
      int count = 0;

      float skinArea = 0;

      for (int pixel : pixels) {
         int r = Color.red(pixel);
         int g = Color.green(pixel);
         int b = Color.blue(pixel);
         int color = Color.rgb(r, g, b);
         if (color != Color.BLACK) {
            int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
            totalGray += gray;
            count++;
            skinArea++;
         }
      }

      int avgGray = (int) (totalGray/count + (totalGray/count * 0.17f));

      int areaCount = 0;

      for (int i = 0; i < pixels.length; i++) {
         int r = Color.red(pixels[i]);
         int g = Color.green(pixels[i]);
         int b = Color.blue(pixels[i]);
         int color = Color.rgb(r,g,b);
         if (color==Color.BLACK) {
            dst[i] = 0x00FFFFFF;
         }else {
            int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
            if (gray>avgGray){
               dst[i] = Color.rgb(clamp(r+50),clamp(g+50),b);
               area[i] = Color.WHITE;
               areaCount++;
            }else {
               dst[i] = 0x00FFFFFF;
            }
         }
      }
      //-------------------score-------------------------------
      float score = 85;
      if (areaCount<skinArea) {
         float percent = areaCount * 100f / skinArea;
         if (percent>15 && percent<=100){//15-100   20-55
            score = ((1-((percent - 15f) / 85f)) * 35f)  + 20f;
         }else if (percent>10 &&percent<=15){
            score = ((1-((percent - 10f) / 5f)) * 10f)  + 55f;
         }else if (percent>5 &&percent<=10){
            score = ((1-((percent - 5) / 5f)) * 10f)  + 65f;
         }else if (percent>0 &&percent<=5){
            score = ((1-(percent/ 5f)) * 10f)  + 75f;
         }else {
            score = 65;
         }
      }else {
         score = 65;
      }
      filterFaceInfoResult.setScore((int) score);
      //-------------------score-------------------------------

      Bitmap topBitmap = Bitmap.createBitmap(dst,width,height, Bitmap.Config.ARGB_8888);
      Mat top = new Mat();
      Utils.bitmapToMat(topBitmap,top);
      Mat oriMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(),oriMat);
      Core.addWeighted(oriMat,1,top,0.2,0,oriMat);
      Utils.matToBitmap(oriMat,topBitmap);
      top.release();
      oriMat.release();

      filterFaceInfoResult.setFilterImage(topBitmap);

   }

   private int clamp(int value) {
      return Math.max(0, Math.min(255, value));
   }

}
