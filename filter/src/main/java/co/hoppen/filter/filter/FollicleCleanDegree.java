package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/11.
 */
public class FollicleCleanDegree extends Filter{
    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
            Bitmap originalImage = getOriginalImage();
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int count = width * height;
                int [] originalPixels = new int[count];
                int [] filterPixels = new int[count];
                originalImage.getPixels(originalPixels,0,width,0,0,width,height);

//                ContrastAdjust contrastAdjust = new ContrastAdjust();
//                int[] execute = contrastAdjust.execute(originalPixels, 50);

                double totalPercentPixels = 0;
                double totalDepth = 0;

                for (int i = 0; i <originalPixels.length ; i++) {
                    int color = originalPixels[i];
                    float [] hsl = new float[3];
                    ColorUtils.colorToHSL(color,hsl);
                    if (hsl[0]<=60 || hsl[0]>=335){
                        //hsl[1] = hsl[1] * 0.3f + hsl[1];
                        //filterPixels[i] = ColorUtils.HSLToColor(hsl);
                        int R = oppositeColorValue(Color.red(color));
                        int G = oppositeColorValue(Color.green(color));
                        int B = oppositeColorValue(Color.blue(color));
//                        int R = Color.red(color);
//                        int G = Color.green(color);
//                        int B = Color.blue(color);
//                        hsl[1] = hsl[1] * 0.9f + hsl[1];
//                        if (hsl[1]>=1f)hsl[1] = 1;
                        if ( hsl[2]>=0.48f ){//0.45
                            int filterColor = Color.rgb(R,G,B);
                            filterPixels[i] = filterColor;
                            totalPercentPixels++;
                            totalDepth += (Color.red(filterColor) * 0.3 + Color.green(filterColor) * 0.59 + Color.blue(filterColor) * 0.11);
                        }else filterPixels[i] = color;
                    }else filterPixels[i] = color;
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(100);
//                filterInfoResult.setRatio(totalPercentPixels * 100 /count);
                if (totalDepth!=0){
//                    filterInfoResult.setDepth((float) (totalDepth / totalPercentPixels));
                }
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        return filterInfoResult;
    }

    private int oppositeColorValue(int value){
        int opposite = 255 - value;
        if(opposite>64 && opposite<128)
            opposite-=64;
        else if(opposite>=128 && opposite<192)
            opposite+=64;
        return opposite;
    }
}
