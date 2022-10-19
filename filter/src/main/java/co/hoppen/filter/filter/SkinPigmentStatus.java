package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public class SkinPigmentStatus extends Filter {

    public static int TEST = 2;//236    236
    public static int Gray = 177;//177    245

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
//            if (!isEmptyBitmap(originalImage)){
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int count = width * height;
                int [] originalPixels = new int[count];
                int [] filterPixels = new int[count];
                originalImage.getPixels(originalPixels,0,width,0,0,width,height);

                int saturation = TEST;
                LogUtils.e(saturation);
                double percent = saturation / 100d;
                float avgS = 0;
                float avgL = 0;
                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    int R = Color.red(originalPixel);
                    int G = Color.green(originalPixel);
                    int B = Color.blue(originalPixel);
                    double max = Math.max(B,Math.max(G,R));
                    double min = Math.min(B,Math.min(G,R));
                    double delta = (max - min) / 255d;
                    double value = (max + min) / 255d;
                    if (delta==0){
                        filterPixels[i] = originalPixel;
                        continue;
                    }
                    double light = value / 2;
                    double sat,alpha;
                    if (light<0.5f){
                        sat=delta/value;
                    } else {
                        sat=delta/(2-value);
                    }
                    double newR,newG,newB;
                    if(percent>=0) {
                        if((percent+sat)>=1) {
                            alpha=sat;
                        } else {
                            alpha=1-percent;
                        }
                        alpha=1/alpha-1;
                        newR=R+(R-light*255)*alpha;
                        newG=G+(G-light*255)*alpha;
                        newB=B+(B-light*255)*alpha;
                    } else {
                        alpha=percent;
                        newR=light*255+(R-light*255)*(1+alpha);
                        newG=light*255+(G-light*255)*(1+alpha);
                        newB=light*255+(B-light*255)*(1+alpha);
                    }
                    int r = (int) newR,g = (int) newG,b = (int) newB;
                    filterPixels[i] = Color.rgb(r,g,b);
                    float [] a = new float[3];
                    ColorUtils.colorToHSL(filterPixels[i],a);
                    avgS +=a[1];
                    avgL +=a[2];
                }
                avgS = avgS / count;
                avgL = avgL / count;
//                LogUtils.e(avgS,avgS/count);
                double totalPercentPixels = 0;
                double totalDepth = 0;

                for (int i = 0; i <filterPixels.length ; i++) {
                    int pixel = originalPixels[i];
                    int R = Color.red(pixel);
                    int G = Color.green(pixel);
                    int B = Color.blue(pixel);

                    float [] b = new float[3];
                    ColorUtils.colorToHSL(pixel,b);
                    if (b[1]>=avgS&&b[2]<=avgL){
                        b[1] = (b[1] * 0.9f) + b[1];
                        if (b[1]>=1)b[1] = 1f;
                        int filterColor = ColorUtils.HSLToColor(b);
                        if (b[0]>=320||b[0]<=70){
                            filterPixels[i] = filterColor;
                            totalPercentPixels++;
                            totalDepth += (Color.red(filterColor) * 0.3 + Color.green(filterColor) * 0.59 + Color.blue(filterColor) * 0.11);
                        }else filterPixels[i] = pixel;
                    }else {
                        filterPixels[i] = pixel;
                    }
                }



//                int percentFormat = (int) formatDouble((totalPercentPixels * 100 / count),0);
//                if (percentFormat>35){
//                    score = 20;
//                }else if (percentFormat>15&&percentFormat<=35){
//                    score = (int) (35f *((percentFormat - 15f)/20f) + 20);
//                }else if (percentFormat>4&&percentFormat<=15){
//                    score = (int) (15f *((percentFormat - 4f)/11f) + 55);
//                }else if (percentFormat>1&&percentFormat<=4){
//                    score = (int) (10f *((percentFormat - 1f)/4f) + 70);
//                }else{
//                    score = 90;
//                }

                int score = 0;
                double percentFormat = formatDouble((totalPercentPixels * 100 / count),1);
                if (percentFormat>35){
                    score = 20;
                }else if (percentFormat>15&&percentFormat<=35){
                    score = (int) (35 *((35 - percentFormat)/20) + 20);
                }else if (percentFormat>4&&percentFormat<=15){
                    score = (int) (15 *((15 - percentFormat)/11) + 55);
                }else if (percentFormat>1&&percentFormat<=4){
                    score = (int) (20 *((4 - percentFormat)/3) + 70);
                }else{
                    score = 90;
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(score);
//                filterInfoResult.setRatio(percentFormat);
                if (totalDepth!=0){
//                    filterInfoResult.setDepth((float) (totalDepth / totalPercentPixels));
                }
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
//            }else filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }catch (Exception e){
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }


    private int opposite(int color){
        int cc = 255 - color;
        if(cc>64 && cc<128)
            cc-=64;
        else if(cc>=128 && cc<192)
            cc+=64;
        return cc;
    }

    private double formatDouble(double d,int scale){
        BigDecimal bg = new BigDecimal(d).setScale(scale, RoundingMode.HALF_UP);
        return bg.doubleValue();
    }


}
