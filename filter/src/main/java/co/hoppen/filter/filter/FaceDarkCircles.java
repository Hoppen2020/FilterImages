package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles extends FaceFilter {

   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
            Bitmap originalImage = getOriginalImage();
            Bitmap filterBitmap = getFaceAreaImage();

            Mat filterMat = new Mat();
            Utils.bitmapToMat(filterBitmap,filterMat);

            Mat oriMat = new Mat();
            Utils.bitmapToMat(filterBitmap,oriMat);

            Imgproc.cvtColor(filterMat,filterMat,Imgproc.COLOR_RGB2HSV);
            Imgproc.cvtColor(filterMat,filterMat,Imgproc.COLOR_RGB2HSV);

            Mat dst = new Mat();

            Core.inRange(filterMat,new Scalar(35,43,46),
                    new Scalar(77,255,255),dst);
//
            Core.bitwise_not(dst,dst);
//
            Mat and = new Mat();

            Core.bitwise_and(oriMat,oriMat,and,dst);
//
            Mat yCrCb = new Mat();
//
            Imgproc.cvtColor(and,yCrCb,Imgproc.COLOR_RGB2HSV);
            Imgproc.cvtColor(yCrCb,yCrCb,Imgproc.COLOR_RGB2HSV);
//
            Core.inRange(yCrCb,new Scalar(78,43,46),
                    new Scalar(99,255,255),yCrCb);

            Utils.matToBitmap(yCrCb,filterBitmap);


            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            int [] originalPixels = new int[width * height];
            int [] filterPixels = new int[width*height];
            int [] resultPixels = new int[width*height];
            int [] areaPixels = new int[width * height];

            originalImage.getPixels(originalPixels, 0, width, 0, 0, width, height);
            filterBitmap.getPixels(filterPixels, 0, width, 0, 0, width, height);

            Bitmap areaBitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), Bitmap.Config.ARGB_8888);
            areaBitmap.setPixels(areaPixels, 0, width, 0, 0, width, height);

            for (int i = 0; i < filterPixels.length; i++) {
               areaPixels[i] = Color.BLACK;
               if (filterPixels[i]==Color.BLACK){
                  resultPixels[i] = originalPixels[i];
               }else {
                  areaPixels[i] = Color.WHITE;
                  double [] lab = new double[3];
                  ColorUtils.colorToLAB(originalPixels[i],lab);
                  lab[0] = lab[0] - (lab[0] * 0.2f);
                  resultPixels[i] = ColorUtils.LABToColor(lab[0],lab[1],lab[2]);
               }
            }
            filterBitmap.setPixels(resultPixels,0,width, 0, 0, width, height);
            areaBitmap.setPixels(areaPixels,0,width, 0, 0, width, height);

            filterMat.release();
            oriMat.release();
            dst.release();
            and.release();
            yCrCb.release();

            Mat areaMat = new Mat();
            Utils.bitmapToMat(areaBitmap,areaMat);
            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));

            filterInfoResult.setFilterBitmap(filterBitmap);
            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
      return filterInfoResult;
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_EYE_BOTTOM};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COLOR;
   }

//   @Override
//   public FaceAreaInfo createAreaBitmap(Object... obj) {
//      Bitmap areaBitmap = (Bitmap) obj[0];
//      Mat mat = new Mat();
//      Utils.bitmapToMat(areaBitmap,mat);
//      return createFaceArea(mat);
//   }
}
