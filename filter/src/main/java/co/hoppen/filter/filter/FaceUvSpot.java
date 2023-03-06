package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceUvSpot extends FaceFilter{

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
      Bitmap operateBitmap = getOriginalImage();
      Mat operateMat = new Mat();
      Utils.bitmapToMat(operateBitmap,operateMat);

      Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);

      CLAHE clahe = Imgproc.createCLAHE(3.0, new Size(10, 10));

      clahe.apply(operateMat,operateMat);

      Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

      LogUtils.e(operateMat.toString());

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

      // 44 44 39 37 47 66
      //level1 0-20 level2 20-40 level3 40-50 level4 50-60 60-70


      if (percent<=20){
         //75-85
         score = ((1-(percent / 20f)) * 10f)  + 75f;
      }else if (percent>20 &&percent<=40){
         //65-75
         score = ((1-((percent - 20f) / 20f)) * 10f)  + 65f;
      }else if (percent>40 && percent<=50){
         //45-65
         score = ((1-((percent - 40f) / 30f)) * 20f)  + 45f;
      }else if (percent>50 && percent<=60){
         //35-45
         score = ((1-((percent - 50f) / 10f)) * 10f)  + 35f;
      }else if (percent>60 && percent<=70){
         //25-35
         score = ((1-((percent - 60f) / 10f)) * 10f)  + 25f;
      }else {
         score = 20f;
      }
      filterInfoResult.setScore((int) score);

      filterInfoResult.setDataTypeString(getFilterDataType(),(double)percent);

      Utils.matToBitmap(operateMat,resultBitmap);

      operateMat.release();

      Mat areaMat = new Mat();

      Utils.bitmapToMat(getFaceAreaImage(),areaMat);

      filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));

      filterInfoResult.setFilterBitmap(resultBitmap);

      filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_SKIN};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.AREA;
   }

}
