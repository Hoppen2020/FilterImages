package co.hoppen.filter.filter;

import android.graphics.Bitmap;

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
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
         Bitmap operateBitmap = getOriginalImage();
         Mat operateMat = new Mat();
         Utils.bitmapToMat(operateBitmap,operateMat);

         Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2GRAY);

         CLAHE clahe = Imgproc.createCLAHE(3.0, new Size(8, 8));

         clahe.apply(operateMat,operateMat);

         Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

         Utils.matToBitmap(operateMat,resultBitmap);

         operateMat.release();

         Mat areaMat = new Mat();

         Utils.bitmapToMat(getFaceAreaImage(),areaMat);

         filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));

         filterInfoResult.setFilterBitmap(resultBitmap);

         filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

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

}
