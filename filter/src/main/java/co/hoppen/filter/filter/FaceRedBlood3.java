package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceRedBlood3 extends FaceFilter{


   @RequiresApi(api = Build.VERSION_CODES.N)
   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
       Mat srcMat = new Mat();
       Utils.bitmapToMat(getOriginalImage(),srcMat);

       // 将图像转换到HSV空间
       Mat hsvMat = new Mat(srcMat.size(), CvType.CV_8UC3, new Scalar(0));
       Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_RGB2HSV);

       // 定义浅红色范围
       Scalar lowerRed = new Scalar(0, 50, 50);
       Scalar upperRed = new Scalar(8, 255, 255);

       // 创建二值图像来分割出浅红色部分
       Mat redMask = new Mat();
       Core.inRange(hsvMat, lowerRed, upperRed, redMask);

       // 进行形态学处理以去除噪声并填补空洞
       Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
       Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_OPEN, kernel);
       Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_CLOSE, kernel);

       // 在浅红色区域深化颜色以突出显示红血丝
       Mat redMaskCopy = redMask.clone();
       for (int r = 0; r < redMask.rows(); r++) {
           for (int c = 0; c < redMask.cols(); c++) {
               if (redMask.get(r, c)[0] == 255) {
                   hsvMat.get(r, c)[2] += 50;  // V value
               }
           }
       }
       // 将颜色重新转换为BGR空间，以便在原始图像上显示结果
       Mat resultMat = new Mat(hsvMat.size(), hsvMat.type());
       Imgproc.cvtColor(hsvMat, resultMat, Imgproc.COLOR_HSV2RGB);
       Core.bitwise_and(resultMat, resultMat, resultMat, redMaskCopy);

       //区域截取
//       Mat maskMat = getFaceMask();
//       byte [] maskByte = new byte[maskMat.channels() * maskMat.cols()];
//       byte [] operateByte = new byte[operateMat.channels() * operateMat.cols()];
//       float count = 0;
//       float totalCount = 0;
//       for (int h = 0; h < operateMat.rows(); h++) {
//           operateMat.get(h,0,operateByte);
//           maskMat.get(h,0,maskByte);
//           for (int w = 0; w < operateMat.cols(); w++) {
//               int index = operateMat.channels() * w;
//               int maskGray = maskByte[w] & 0xff;
//               if (maskGray!=0){
//                   totalCount++;
//                   int r = operateByte[index]&0xff;
//                   if (r<=100){
//                       count++;
//                   }
//               }else {
//                   operateByte[index] = 0;
//                   operateByte[index +1] = 0;
//                   operateByte[index + 2] = 0;
//               }
//           }
//           operateMat.put(h,0,operateByte);
//       }

       filterInfoResult.setScore(0);
       filterInfoResult.setDataTypeString(getFilterDataType(),0d);

       Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
       Utils.matToBitmap(resultMat,resultBitmap);

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
