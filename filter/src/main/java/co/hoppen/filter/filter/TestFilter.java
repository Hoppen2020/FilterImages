package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/14.
 */
public class TestFilter extends Filter{
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
                Bitmap darkBitmap = toGray(true);
                double [] tmap =new double[count];

                darkBitmap.getPixels(originalPixels,0,width,0,0,width,height);

                double max = Double.MIN_VALUE;
                for (int i = 0; i <width ; i++) {
                    for (int j = 0; j < height; j++) {
                        double gx = GX(i, j, darkBitmap);
                        double gy = GY(i, j, darkBitmap);
                        double sqrt = Math.sqrt(gx * gx + gy * gy);
                        tmap[j * width + i] = sqrt;
                        if (max <tmap[j * width + i]){
                            max = tmap[j * width + i];
                        }
                    }
                }

                double top = max * 0.06;
                LogUtils.e("top{  "+top);
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (tmap[j * width + i] > top) {
                            filterPixels[j * width + i] = originalPixels[j * width + i];
                        } else {
                            filterPixels[j * width + i] = Color.WHITE;
                        }
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(0);
//                filterInfoResult.setRatio(0);
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

        return filterInfoResult;
    }

    private  double GX(int x, int y, Bitmap bitmap) {
        double res = (-1) * getPixel(x - 1, y - 1, bitmap)
                + 1 * getPixel(x + 1, y - 1, bitmap)
                + (-Math.sqrt(2)) * getPixel(x - 1, y, bitmap)
                + Math.sqrt(2) * getPixel(x + 1, y, bitmap)
                + (-1) * getPixel(x - 1, y + 1, bitmap)
                + 1 * getPixel(x + 1, y + 1, bitmap);

        return res;
    }

    private double GY(int x, int y, Bitmap bitmap) {
        double res = 1 * getPixel(x - 1, y - 1, bitmap)
                + Math.sqrt(2) * getPixel(x, y - 1, bitmap)
                + 1 * getPixel(x + 1, y - 1, bitmap)
                + (-1) * getPixel(x - 1, y + 1, bitmap)
                + (-Math.sqrt(2)) * getPixel(x, y + 1, bitmap)
                + (-1) * getPixel(x + 1, y + 1, bitmap);

        return res;
    }

    private double getPixel(int x, int y, Bitmap bitmap) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
            return 0;
        }
        return bitmap.getPixel(x, y);
    }

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

    protected Bitmap toGray(boolean dark){
        Bitmap originalImage = getOriginalImage();
        if (originalImage==null)return null;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int count = width * height;
        int [] originalPixels = new int[count];
        int [] filterPixels = new int[count];
        originalImage.getPixels(originalPixels,0,width,0,0,width,height);

        for (int i = 0; i <originalPixels.length ; i++) {
            int originalPixel = originalPixels[i];
            int R = Color.red(originalPixel);
            int G = Color.green(originalPixel);
            int B = Color.blue(originalPixel);
            int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
            int rgb = Color.rgb(gray, gray, gray);
            int color = (int) (rgb + (rgb-128) * (1.0f+0f) /255);
            filterPixels[i] = dark?color:rgb;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
        return bitmap;
    }
}
