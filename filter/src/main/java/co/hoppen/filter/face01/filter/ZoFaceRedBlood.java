package co.hoppen.filter.face01.filter;

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
import java.util.Map;

import co.hoppen.filter.face01.FaceZoFilter;
import co.hoppen.filter.face01.FilterFaceZoInfoResult;

/**
 * Created by YangJianHui on 2024/4/9.
 */
public class ZoFaceRedBlood extends FaceZoFilter {
   @Override
   public void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception {
      Mat operateMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(),operateMat);

      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);
      List<Mat> splitList = new ArrayList<>();
      Core.split(operateMat,splitList);
      splitList.set(0,new Mat(operateMat.size(), CvType.CV_8UC1,new Scalar(0)));
      Imgproc.equalizeHist(splitList.get(1),splitList.get(1));

      //直方图均衡化
      CLAHE clahe = Imgproc.createCLAHE(2, new Size(8, 8));
      clahe.apply(splitList.get(1),splitList.get(1));

      Core.merge(splitList,operateMat);

      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);

      Mat maskMat = getFaceMask();

      byte [] maskByte = new byte[maskMat.channels() * maskMat.cols()];
      byte [] operateByte = new byte[operateMat.channels() * operateMat.cols()];

      float count = 0;
      float totalCount = 0;

      for (int h = 0; h < operateMat.rows(); h++) {
         operateMat.get(h,0,operateByte);
         maskMat.get(h,0,maskByte);
         for (int w = 0; w < operateMat.cols(); w++) {
            int index = operateMat.channels() * w;
            int maskGray = maskByte[w] & 0xff;
            if (maskGray!=0){
               totalCount++;
               int r = operateByte[index]&0xff;
               if (r<=100){
                  count++;
               }
            }else {
               operateByte[index] = 0;
               operateByte[index +1] = 0;
               operateByte[index + 2] = 0;
            }
         }
         operateMat.put(h,0,operateByte);
      }

      float score = 85f;
      float percent = count * 100f /totalCount;

      if (percent<=30){
         score = ((1-(percent / 30f)) * 10f)  + 75f;
      }else if (percent>30 && percent<=50){
         score = ((1-((percent - 30f) / 20f)) * 10f)  + 65f;
      }else if (percent>50 && percent<=60){
         score = ((1-((percent - 50f) / 10f)) * 10f)  + 55f;
      }else if (percent>60 && percent<=70){
         score = ((1-((percent - 60f) / 10f)) * 10f)  + 45f;
      }else if (percent>70 && percent<=80){
         score = ((1-((percent - 70f) / 10f)) * 10f)  + 35f;
      }else if (percent>80 &&percent<=90){
         score = ((1-((percent - 80f) / 10f)) * 15f)  + 20f;
      }else {
         score = 20f;
      }

      Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
      Utils.matToBitmap(operateMat,resultBitmap);


      filterFaceInfoResult.setScore((int) score);
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
