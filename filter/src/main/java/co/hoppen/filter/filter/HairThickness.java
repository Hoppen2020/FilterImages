package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2024/3/20.
 */
public class HairThickness extends Filter{
   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
      int width = getOriginalImage().getWidth();
      int height = getOriginalImage().getHeight();

      Mat orMat = new Mat();
      Utils.bitmapToMat(getOriginalImage(), orMat);

      Mat grayMat = new Mat();
      Mat sourceMat = new Mat();
      Mat binaryMat = new Mat();

      Imgproc.cvtColor(orMat, sourceMat, Imgproc.COLOR_RGBA2RGB);
      Imgproc.cvtColor(orMat, grayMat, Imgproc.COLOR_RGB2GRAY);
      Imgproc.GaussianBlur(grayMat, grayMat, new Size(5.0, 5.0), 2.0, 2.0);
      Imgproc.threshold(grayMat, binaryMat, 10.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

      Mat hsv = new Mat();
      Imgproc.cvtColor(sourceMat, hsv, Imgproc.COLOR_RGB2HSV);
      Mat mask = new Mat();
      Core.inRange(hsv, new Scalar(0.0, 0.0, 200.0), new Scalar(180.0, 255.0, 255.0), mask);
      Core.subtract(binaryMat, mask, binaryMat);

      List<MatOfPoint> contours = new ArrayList<>();
      Mat hierarchy = new Mat();
      Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

      Mat tmp = sourceMat.clone();

      for (int i = 0; i < contours.size(); i++) {
//         LogUtils.e(contours.get(i).size().toString(), contours.get(i).size().area());
         if (contours.get(i).size().area() <= 50.0) {
            continue;
         }
         MatOfPoint2f result = new MatOfPoint2f();
         MatOfPoint2f source = new MatOfPoint2f();
         source.fromList(contours.get(i).toList());
         Imgproc.approxPolyDP(source, result, 4.0, true);
         Point[] points = result.toArray();

         RotatedRect rect = Imgproc.minAreaRect(source);

         Imgproc.putText(tmp, "" + (int) contours.get(i).size().area(), rect.center,
                 3, 1.0, new Scalar(255.0, 0.0, 0.0), 1);

         for (int j = 0; j < points.length; j++) {
            Imgproc.line(tmp,
                    points[j % points.length],
                    points[(j + 1) % points.length],
                    new Scalar(255.0, 255.0, 0.0),
                    1,
                    Imgproc.LINE_8);
         }
      }

      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      Utils.matToBitmap(tmp, bitmap);

      orMat.release();
      grayMat.release();
      binaryMat.release();
      sourceMat.release();
      hsv.release();
      mask.release();
      hierarchy.release();
      tmp.release();

      filterInfoResult.setScore(new Random().nextInt(16) + 48);
      filterInfoResult.setHairFilterBitmap(bitmap);
      filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

   }
   
}
