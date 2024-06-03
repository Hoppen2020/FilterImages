package co.hoppen.filter.face01.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
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
public class ZoFaceUvSpot extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Bitmap operateBitmap = getOriginalImage();
      Mat operateMat = new Mat();
      Utils.bitmapToMat(operateBitmap,operateMat);

      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);

      CLAHE clahe = Imgproc.createCLAHE(3.0, new Size(10, 10));

      clahe.apply(operateMat,operateMat);

      Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

      Mat maskMat = getFaceMask();

      byte[] operateByte = new byte[operateMat.channels() * operateMat.cols()];
      byte[] maskByte = new byte[maskMat.channels() * maskMat.cols()];

      float totalCount = 0;
      float count = 0;

      for (int h = 0; h < operateMat.rows(); h++) {
         operateMat.get(h, 0, operateByte);
         maskMat.get(h,0,maskByte);
         for (int w =0;w <operateMat.cols();w++){
            int index = operateMat.channels() * w;
            int maskGray = maskByte[w] & 0xff;
            if (maskGray!=0){
               totalCount++;
               int gray = operateByte[index] & 0xff;
               if (gray<=120){
                  count++;
               }
               operateByte[index] = (byte) gray;
            }else {
               operateByte[index] = 0;
            }
         }
         operateMat.put(h, 0, operateByte);
      }
      maskMat.release();

      float score = 85f;

      float percent = count * 100f / totalCount;

      LogUtils.e(count,totalCount,percent);

      if (percent<=20){
         score = ((1-(percent / 20f)) * 10f)  + 75f;
      }else if (percent>20 &&percent<=40){
         score = ((1-((percent - 20f) / 20f)) * 10f)  + 65f;
      }else if (percent>40 && percent<=50){
         score = ((1-((percent - 40f) / 30f)) * 20f)  + 45f;
      }else if (percent>50 && percent<=60){
         score = ((1-((percent - 50f) / 10f)) * 10f)  + 35f;
      }else if (percent>60 && percent<=70){
         score = ((1-((percent - 60f) / 10f)) * 10f)  + 25f;
      }else {
         score = 20f;
      }
      filterFaceInfoResult.setScore((int) score);

      Utils.matToBitmap(operateMat,resultBitmap);

      operateMat.release();

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
