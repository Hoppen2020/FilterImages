package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */

public class FaceNearRedLight extends FaceFilter{

   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
      try {
            Mat operateMat = new Mat();
            Utils.bitmapToMat(getOriginalImage(),operateMat);
            Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);
            Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);

            List<Mat> splitList = new ArrayList<>();
            Core.split(operateMat,splitList);
            splitList.set(0,new Mat(operateMat.size(), CvType.CV_8UC1,new Scalar(0)));

            float percent = 0f;

            Mat matS = splitList.get(1);

            byte[] p = new byte[matS.channels() * matS.cols()];

            for (int h = 0; h < matS.rows(); h++) {
               matS.get(h, 0, p);
               for (int w = 0; w < matS.cols(); w++) {
                  int index = matS.channels() * w;
                  int value = p[index] & 0xff;
                  percent = value / 255f + 0.1f;
                  int nValue = (int) (value + percent * value);
                  if (nValue>=255) {
                     nValue = 255;
                  }else if (nValue<=0){
                     nValue = 0;
                  }
                  p[index] = (byte) nValue;
               }
               matS.put(h, 0, p);
            }

            Core.merge(splitList,operateMat);
            Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);

            double oriBrightness = Core.mean(operateMat).val[0];

            double brightness = oriBrightness + (oriBrightness * 0.5d);

            LogUtils.e(oriBrightness,brightness);

            Core.add(operateMat,new Scalar(brightness - oriBrightness,brightness - oriBrightness,brightness - oriBrightness),operateMat);


            Mat areaMat = new Mat();
            Utils.bitmapToMat(getFaceAreaImage(),areaMat);
            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));


            Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(operateMat,resultBitmap);
            filterInfoResult.setFilterBitmap(resultBitmap);

            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
      }catch (Exception e){
         filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
      }
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
