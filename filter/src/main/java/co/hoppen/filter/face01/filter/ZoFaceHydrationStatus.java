package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/8.
 */
public class ZoFaceHydrationStatus extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Bitmap originalImage = getOriginalImage();
      int width = originalImage.getWidth();
      int height = originalImage.getHeight();
      int count = width * height;

      int [] originalPixels = new int[count];
      int [] partsPixels = new int[count];

      originalImage.getPixels(originalPixels,0,width,0,0,width,height);
      getFilterPartsImages().getPixels(partsPixels,0,width,0,0,width,height);

      int totalGray = 0;

      for (int originalPixel : originalPixels) {
         int R = Color.red(originalPixel);
         int G = Color.green(originalPixel);
         int B = Color.blue(originalPixel);
         int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
         totalGray += gray;
      }

      int avgGray = totalGray / count;
      float totalPercentPixels = 0;
      float skinArea = 0;

      for (int i = 0; i <partsPixels.length ; i++) {

         int color = partsPixels[i];
         if (Color.alpha(color)==0){
            partsPixels[i] = originalPixels[i];
         }else {
            skinArea++;
            int gray = (int) ((Color.red(color) * 0.3f) + (Color.green(color) * 0.59f) + (Color.blue(color) * 0.11f));
            partsPixels[i] = originalPixels[i];
            if (gray <avgGray * 1.2){
            } else if (gray>90 && gray<=110){
            }else if (gray>=150){
               totalPercentPixels++;
               int originalPixel = originalPixels[i];
               int R = Color.red(originalPixel);
               int G = Color.green(originalPixel);
               int B = Color.blue(originalPixel);
               B = B + 50;
               partsPixels[i] = Color.rgb(R,G,clamp(B));
            }
         }
      }

      //-------------------score-------------------------------
      float score = 85;
      if (totalPercentPixels<skinArea){
         float percent = totalPercentPixels * 100f / skinArea;
         if (percent>0 && percent<=10){
            score = ((percent / 10f) * 20f)  + 20f;
         }else if (percent>10 && percent<=30){
            score = (((percent-10f) / 20f) * 20f)  + 40f;
         }else if (percent>30 && percent<=50){
            score = (((percent-30f) / 20f) * 10f)  + 60f;
         }else if (percent>50 &&percent<=100){
            score = (((percent - 50f) / 50f) * 15f)  + 70f;
         }
      }else score = 65f;

      filterFaceInfoResult.setScore((int) score);

      //-------------------score-------------------------------

      Bitmap resultBitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
      resultBitmap.setPixels(partsPixels,0,width, 0, 0, width, height);
      filterFaceInfoResult.setFilterImage(resultBitmap);
   }

   private int clamp(int value) {
      return Math.max(0, Math.min(255, value));
   }

}
