package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/11.
 */
public class SkinRedBloodStatus extends Filter {

//    public static int
    public static int COLOR_GAMUT = 25;

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

                //灰度处理
                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    int R = Color.red(originalPixel);
                    int G = Color.green(originalPixel);
                    int B = Color.blue(originalPixel);
                    int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
                    filterPixels[i] = Color.rgb(gray,gray,gray);
                }
                //边缘算法 去皮肤毛发
                double max = -999;
                double[] tmap = new double[count];
                for (int i = 0; i < width ; i++) {
                    for (int j = 0; j < height; j++) {
                        //计算横向数值
                        double gx = GX(i, j, filterPixels,width,height);
                        //计算纵向数值
                        double gy = GY(i, j, filterPixels,width,height);
                        //进行开方处理
                        tmap[j * width + i] = Math.sqrt(gx * gx + gy * gy);
                        //保存最大值
                        if (max < tmap[j * width + i]) {
                            max = tmap[j * width + i];
                        }
                    }
                }

                double threshold = 0.095f; //阙值 可多个进行控制
                int [] cmap = new int[count];

                //筛选计算
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (tmap[j * width + i] > max * threshold) {
                            cmap[j * width + i] = Color.WHITE;
                        }else {
                            cmap[j * width + i] = originalPixels[j * width + i];
                        }
                    }
                }
                //对比度加深   对比度调整
                int saturation = COLOR_GAMUT;//50
                double percent = saturation / 100d;
                float avgS = 0;
                float avgL = 0;


                double totalPercentPixels = 0;
                double totalDepth = 0;

                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
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
                avgS = avgS / count;
                avgL = avgL / count;

                for (int i = 0; i <filterPixels.length ; i++) {

                    int pixel = originalPixels[i];
                    if (cmap[i]==Color.WHITE){//去除皮肤毛发部分
                        filterPixels[i] = pixel;
                        continue;
                    }
                    //----------test--------------
//                    if (true) continue;
                    //----------test--------------
                    float [] tp = new float[3];
                    ColorUtils.colorToHSL(filterPixels[i],tp);
                    if (tp[1]>=avgS && tp[2]<=avgL){
                        if (tp[0]<=20){
                            tp[1] = (tp[1] * 0.95f) + tp[1];
                            if (tp[1]>=1)tp[1] = 1f;
                            int filterColor = ColorUtils.HSLToColor(tp);
                            filterPixels[i] = filterColor;

                            totalPercentPixels++;
                            totalDepth += (Color.red(filterColor) * 0.3 + Color.green(filterColor) * 0.59 + Color.blue(filterColor) * 0.11);

                        }else filterPixels[i] = pixel;
                    }else {
                        filterPixels[i] = pixel;
                    }
                }

                Bitmap bitmap =  Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
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


    /**
     * 处理颜色
     * @param color
     * @param value value为负数时颜色加深，为正数时颜色变淡
     * @return
     */
    private int getColor(int color, int value) {

        int cr, cg, cb;

        cr = (color & 0x00ff0000) >> 16;
        cg = (color & 0x0000ff00) >> 8;
        cb = color & 0x000000ff;

        cr += value;
        cg += value;
        cb += value;

        if(cr > 255){
            cr = 255;
        }
        if(cg > 255){
            cg = 255;
        }
        if(cb > 255){
            cb = 255;
        }

        if(cr < 0){
            cr = 0;
        }
        if(cg < 0){
            cg = 0;
        }
        if(cb < 0){
            cb = 0;
        }

        return Color.argb(255, cr, cg, cb);
    }

    /**
     * 获取横向的
     *
     * @param x      第x行
     * @param y      第y列
     * @param
     * @return
     */
    private static double GX(int x, int y, int [] filterPixels,int width,int height) {
        double res = (-1) * getPixel(x - 1, y - 1, filterPixels,width,height)
                + 1 * getPixel(x + 1, y - 1, filterPixels,width,height)
                + (-Math.sqrt(2)) * getPixel(x - 1, y, filterPixels,width,height)
                + Math.sqrt(2) * getPixel(x + 1, y, filterPixels,width,height)
                + (-1) * getPixel(x - 1, y + 1, filterPixels,width,height)
                + 1 * getPixel(x + 1, y + 1, filterPixels,width,height);

        return res;
    }

    /**
     * 获取纵向的
     *
     * @param x      第x行
     * @param y      第y列
     * @param
     * @return
     */
    private static double GY(int x, int y, int [] filterPixels,int width,int height) {
        double res = 1 * getPixel(x - 1, y - 1, filterPixels,width,height)
                + Math.sqrt(2) * getPixel(x, y - 1, filterPixels,width,height)
                + 1 * getPixel(x + 1, y - 1, filterPixels,width,height)
                + (-1) * getPixel(x - 1, y + 1, filterPixels,width,height)
                + (-Math.sqrt(2)) * getPixel(x, y + 1, filterPixels,width,height)
                + (-1) * getPixel(x + 1, y + 1, filterPixels,width,height);

        return res;
    }

    /**
     * 获取第x行第y列的色度
     *
     * @param x      第x行
     * @param y      第y列
     * @param
     * @return
     */
    private static double getPixel(int x, int y, int [] filterPixels,int width,int height) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        return filterPixels[y * width +x];
    }

}
