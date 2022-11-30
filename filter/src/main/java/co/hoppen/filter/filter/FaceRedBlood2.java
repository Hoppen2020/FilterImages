package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

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

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceRedBlood2 extends FaceFilter{


   @RequiresApi(api = Build.VERSION_CODES.N)
   @Override
   public FilterInfoResult onFilter() {
      FilterInfoResult filterInfoResult = getFilterInfoResult();
          Mat operateMat = new Mat();
          Utils.bitmapToMat(getOriginalImage(),operateMat);

//          Mat grayMat = new Mat();
//          Imgproc.cvtColor(operateMat,grayMat,Imgproc.COLOR_RGB2GRAY);

//          CLAHE clahe = Imgproc.createCLAHE(2, new Size(8, 8));
//          clahe.apply(grayMat,grayMat);
//
//          Imgproc.cvtColor(grayMat,operateMat,Imgproc.COLOR_GRAY2RGB);
          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);
          List<Mat> splitList = new ArrayList<>();
          Core.split(operateMat,splitList);
          splitList.set(0,new Mat(operateMat.size(), CvType.CV_8UC1,new Scalar(0)));
          Imgproc.equalizeHist(splitList.get(1),splitList.get(1));
//          Imgproc.equalizeHist(splitList.get(2),splitList.get(2));

          CLAHE clahe = Imgproc.createCLAHE(2, new Size(8, 8));
          clahe.apply(splitList.get(1),splitList.get(1));

          Core.merge(splitList,operateMat);

          Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);


          Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
          Utils.matToBitmap(operateMat,resultBitmap);

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
