package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceRedBlood extends FaceFilter{


   @RequiresApi(api = Build.VERSION_CODES.N)
   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
      try {
          Mat operateMat = new Mat();
          Utils.bitmapToMat(getOriginalImage(),operateMat);
          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);
          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);

          List<Mat> splitHsv = new ArrayList<>();
          Core.split(operateMat,splitHsv);

          Mat matS = splitHsv.get(1);

          byte [] p = new byte[matS.channels() * matS.cols()];

          for (int h = 0; h < matS.rows(); h++) {
              matS.get(h, 0, p);
              for (int w = 0; w < matS.cols(); w++) {
                  int index = matS.channels() * w;
                  int value = p[index] & 0xff;
                  value = value - 30;
                  if (value<=0)value = 0;
                  p[index] = (byte) value;
              }
              matS.put(h, 0, p);
          }

          Core.merge(splitHsv,operateMat);
          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);

          List<Mat> labList = new ArrayList<>();
          Core.split(operateMat,labList);

          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);
          splitHsv.clear();
          Core.split(operateMat,splitHsv);
          splitHsv.set(0,new Mat(operateMat.size(),CvType.CV_8UC1));
          splitHsv.set(1,labList.get(1));


          Core.merge(splitHsv,operateMat);

          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);


//          Mat mask = new Mat();
//          Core.inRange(operateMat,new Scalar(50,160,100),new Scalar(255,180,255),mask);
//          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);
//
//          Mat dst = new Mat();
//          Core.copyTo(operateMat,dst,mask);
//
//          Core.addWeighted(operateMat,1.0,dst,-0.2,0,operateMat);


          Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
          Utils.matToBitmap(operateMat,resultBitmap);


          filterInfoResult.setFilterBitmap(resultBitmap);
          filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
      }catch (Exception e){

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
