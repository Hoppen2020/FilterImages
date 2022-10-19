package co.hoppen.filter.filter;

import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

/**
 * Created by YangJianHui on 2021/11/5.
 */
public class ContrastAdjust {
    private final int defaultSaturation = 50;
    private float avgS;
    private float avgL;

    /**
     * 数据源
     * 对比度值-100 ~ 100
     * @param srcPixels
     * @param saturation
     * @return
     */
    public int[] execute(int [] srcPixels, int saturation){
        if (saturation > 100 || saturation < -100) saturation =defaultSaturation;
        if (srcPixels==null||srcPixels.length==0) return null;
        int [] filterPixels = new int[srcPixels.length];
        double percent = saturation / 100d;
        for (int i = 0; i <srcPixels.length ; i++) {
            int originalPixel = srcPixels[i];
            int R = Color.red(originalPixel);
            int G = Color.green(originalPixel);
            int B = Color.blue(originalPixel);
            double maxColor = Math.max(B,Math.max(G,R));
            double minColor = Math.min(B,Math.min(G,R));
            double delta = (maxColor - minColor) / 255d;
            double value = (maxColor + minColor) / 255d;
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
                alpha= 1 / alpha - 1;
                newR=R+(R-light*255)* alpha;
                newG=G+(G-light*255)* alpha;
                newB=B+(B-light*255)* alpha;
            } else {
                alpha=percent;
                newR=light*255+(R-light*255)*(1+alpha);
                newG=light*255+(G-light*255)*(1+alpha);
                newB=light*255+(B-light*255)*(1+alpha);
            }
            filterPixels[i] = Color.rgb((int) newR,(int) newG,(int) newB);
            float [] tp = new float[3];
            ColorUtils.colorToHSL(filterPixels[i],tp);
            avgS +=tp[1];
            avgL +=tp[2];
        }
        //this.filterPixels = filterPixels;
        avgS = avgS / srcPixels.length;
        avgL = avgL / srcPixels.length;
        return filterPixels;
    }

    public float getAvgS() {
        return avgS;
    }

    public float getAvgL() {
        return avgL;
    }

}
