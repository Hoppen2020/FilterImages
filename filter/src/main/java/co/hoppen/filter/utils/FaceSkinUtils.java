package co.hoppen.filter.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.SPUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FilterCacheConfig;

/**
 * Created by YangJianHui on 2022/9/19.
 */
public class FaceSkinUtils {

   public static void saveFaceSkinArea(Bitmap bitmap){
      if (bitmap!=null){
         Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
         Mat oriMat = new Mat();
         Utils.bitmapToMat(copy,oriMat);

         Mat hsvMat = new Mat();
         Imgproc.cvtColor(oriMat,hsvMat,Imgproc.COLOR_RGB2HSV);
         Core.inRange(hsvMat,new Scalar(0,15,0),new Scalar(17,170,255),hsvMat);

         Mat yCrBrMat = new Mat();
         Imgproc.cvtColor(oriMat,yCrBrMat,Imgproc.COLOR_RGB2YCrCb);
         Core.inRange(yCrBrMat,new Scalar(0,135,85),new Scalar(255,180,135),yCrBrMat);

         Mat mask = new Mat();
         Core.bitwise_and(hsvMat,yCrBrMat,mask);

         Mat result = new Mat();
         Core.bitwise_and(oriMat,oriMat,result,mask);
         Utils.matToBitmap(result,copy);

         oriMat.release();
         hsvMat.release();
         yCrBrMat.release();
         mask.release();
         result.release();

         int width = copy.getWidth();
         int height = copy.getHeight();
         int [] pixels = new int[width * height];
         int skinArea = 0;
         copy.getPixels(pixels,0,width,0,0,width,height);
         for (int i = 0; i < pixels.length; i++) {
            int color =  pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
            if (gray>10){
               skinArea++;
            }
         }
         //ImageUtils.save(copy, Environment.getExternalStorageDirectory().getPath() + "/test/a.jpg", Bitmap.CompressFormat.JPEG);
         if (!copy.isRecycled())copy.recycle();
         SPUtils.getInstance().put(FilterCacheConfig.CACHE_FACE_SKIN_AREA,skinArea);
      }
   }

   /**
    * 获取上一次的皮肤面积
    * @return
    */
   public static int getSkinArea(){
      return SPUtils.getInstance().getInt(FilterCacheConfig.CACHE_FACE_SKIN_AREA,0);
   }

   public static void clearSkinArea(){
      SPUtils.getInstance().put(FilterCacheConfig.CACHE_FACE_SKIN_AREA,0);
   }

}
