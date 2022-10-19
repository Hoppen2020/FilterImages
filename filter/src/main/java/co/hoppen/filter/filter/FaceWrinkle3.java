package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle3 extends Filter{

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult =getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                int height = originalImage.getHeight();
                int width = originalImage.getWidth();

                Bitmap bitmap =getOriginalImage();
                int [] skin = new int[width * height];
                bitmap.getPixels(skin, 0, width, 0, 0, width, height);

//                        originalImage.copy(Bitmap.Config.ARGB_8888,true);
                Mat grayMat = new Mat();
                Utils.bitmapToMat(bitmap,grayMat);
                Imgproc.cvtColor(grayMat,grayMat,Imgproc.COLOR_RGB2GRAY);
                Mat result = new Mat();
                Imgproc.Sobel(grayMat,result,-1,0,1,9);
                Core.convertScaleAbs(result, result);
                Utils.matToBitmap(result,bitmap);

                grayMat.release();
                result.release();

                int [] pixels = new int[width * height];
                int [] dstPixels = new int[width * height];

                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                originalImage.getPixels(dstPixels, 0, width, 0, 0, width, height);
                for (int i = 0; i < pixels.length; i++) {
                    int a = Color.alpha(skin[i]);
                    if (a==0){
                        dstPixels[i] = 0x00000000;
                        continue;
                    }
                    int r = Color.red(pixels[i]);
                    int g = Color.green(pixels[i]);
                    int b = Color.blue(pixels[i]);
                    int color = Color.rgb(r,g,b);
                    if (color==Color.BLACK) {
                        if (Color.alpha(dstPixels[i])!=0){
                            dstPixels[i] = Color.rgb(205,183,158);
                        }
                    }
                }
                Bitmap resultBitmap = Bitmap.createBitmap(dstPixels, width, height, Bitmap.Config.ARGB_8888);
                filterInfoResult.setFilterBitmap(resultBitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        } catch (Exception e) {
            LogUtils.e(e.toString());
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }
}