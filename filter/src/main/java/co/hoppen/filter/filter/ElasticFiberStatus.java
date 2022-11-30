package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.hoppen.filter.FilterInfoResult;


/**
 * Created by YangJianHui on 2021/9/11.
 */
public class ElasticFiberStatus extends Filter{
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
                int totalGray = 0;
                double totalHsv = 0;
                double avgHsv = 0;
                int majorColor = Color.BLACK;
                int countRgb = 0;
                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    int R = Color.red(originalPixel);
                    int G = Color.green(originalPixel);
                    int B = Color.blue(originalPixel);
                    int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
                    totalGray+=gray;
                    filterPixels[i] = gray;

                    double hsv = rgb2Hsv(originalPixel);
                    totalHsv +=hsv;

                }
                avgHsv = totalHsv / count;
                List<Integer> thresholdList = new ArrayList<Integer>();
                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    double hsv = rgb2Hsv(originalPixel);
                    if (Math.abs(hsv - avgHsv) > 30) {//threshold = 30
                        thresholdList.add(originalPixel);
                    }
                }
                if (thresholdList.size()!=0){
                    int sumR = 0,sumG = 0, sumB = 0;
                    for (int rgb : thresholdList){
                        sumR += Color.red(rgb);
                        sumG += Color.green(rgb);
                        sumB += Color.blue(rgb);
                    }
                    majorColor = Color.rgb(sumR / thresholdList.size(), sumG / thresholdList.size(), sumB / thresholdList.size());
                }
                int verGray = totalGray / count;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int originalPixel = originalPixels[pos];
                        filterPixels[pos] = originalPixel;
                        int r = Color.red(originalPixel);
                        int g = Color.green(originalPixel);
                        int b = Color.blue(originalPixel);

                        double light = (float) 255 / verGray;
                        double criticalValueRG = (Math.sqrt(69.625 * 69.625 + 4 * 11.375 * verGray) + 69.625) / (2 * 11.375);
                        if ((float) r / g > ((verGray * light / 30 / criticalValueRG)) && ((float) r / b > (verGray * light / 30 / criticalValueRG))) {
                            if ((float) g / b < 0.9||(float) r / b < 0.9) {
                                countRgb++;
                                filterPixels[pos] = Color.rgb(255, g, b);
                                if (y>=2&&x>=2){
                                    filterPixels[(y-1) * width + (x-1)] = Color.rgb(b, g, b);
                                    filterPixels[(y-2) * width + (x-2)] = Color.rgb(b, g, b);
                                }

                            }
                        }

                        light = verGray / 255.0;
                        double xishudou = 255.0 / 90.0;
                        double xishugenRG = (Math.sqrt(21.49 * 21.49 - 4 * 0.4371 * verGray) + 21.49) / (2 * 0.4371);
                        double xishugenGR = xishugenRG - 20;

                        int RjianG = (Math.abs(Color.red(majorColor) - Color.red(originalPixel)) - (Math.abs(Color.green(majorColor) - Color.green(originalPixel))));
                        int RjianB = (Math.abs(Color.red(majorColor) - Color.red(originalPixel)) - (Math.abs(Color.blue(majorColor) - Color.blue(originalPixel))));

                        if ((Color.red(originalPixel) - Color.blue(originalPixel) > 0 || (Color.green(originalPixel) - Color.blue(originalPixel) > 0)) && ((Color.green(originalPixel) - Color.red(originalPixel)) < xishugenGR && (Color.red(originalPixel) - Color.green(originalPixel)) < xishugenRG))// || float_R / float_B > 1.2{
                            if (((Math.abs(Color.red(majorColor) - Color.red(originalPixel)) > 35 * light * xishudou) || (Math.abs(Color.green(majorColor) - Color.green(originalPixel)) > 30 * light * xishudou) && RjianG < 10) ||
                                    ((Math.abs(Color.red(majorColor) - Color.red(originalPixel)) > 35 * light * xishudou) || (Math.abs(Color.blue(majorColor) - Color.blue(originalPixel)) > 30 * light * xishudou) && RjianB < 10)) {
                                int RColor = Color.red(originalPixel) * 2 <= 255 ? Color.red(originalPixel) * 2 : 255;
                                int GColor = Color.green(originalPixel) * 2 <= 255 ? Color.green(originalPixel) * 2 : 255;
                                filterPixels[pos] = Color.rgb(RColor, GColor, Color.blue(originalPixel));
                                if (y>=2&&x>=2){
                                    filterPixels[(y-1) * width + (x-1)] = Color.rgb(b, g, b);
                                    filterPixels[(y-2) * width + (x-2)] = Color.rgb(b, g, b);
                                }
                                countRgb++;
                            }
                        }

                    }

                int score = 0;
                if (countRgb < (float)count / 40.0) {
                    score = (int) (70 + 10 * (1.0 - (countRgb * 40.0) / (count)));
                } else if (countRgb > count / 40.0 && countRgb < count / 20.0) {
                    score  = (int) (60 + 10 * (1.0 - ((countRgb - (count / 40.0)) * 40.0 / (count))));
                } else if (countRgb > count / 20.0 && countRgb < count / 10.0) {
                    score  = (int) (50 + 10 * (1.0 - ((countRgb - (count / 20.0)) * 20.0 / (count))));
                } else if (countRgb > count / 10.0) {
                    if(countRgb > count / 10.0 && countRgb <= count / 4.0) {
                        score = (int) (40 + 10 * (1.0 - ((countRgb - (count) * 0.1) * 20.0 / 3.0 / (count))));
                    } else {
                        score = 40 + new Random().nextInt(3);
                    }
                }
                if (score < 25) {
                    Random random=new Random();
                    score = 25+random.nextInt(5);
                }
                if (score > 85) {
                    Random random=new Random();
                    score = 80+random.nextInt(5);
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(score);
//                filterInfoResult.setRatio((countRgb * 100 / count));
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        return filterInfoResult;
    }


    public double rgb2Hsv(int rgb) {
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        double h = 0;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        if (max == min) {
            h = 0;
        } else {
            if (r == max)
                h = (double)(g - b) / (max - min);
            else if (g == max)
                h = 2 + (double)(b - r) / (max - min);
            else
                h = 4 + (double)(r - g) / (max - min);
        }
        h = h * 60;
        if (h < 0)
            h = h + 360;
        return h;
    }

}
