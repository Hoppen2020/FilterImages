package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles extends FaceFilter {

   @Override
   public void onFilter(FilterInfoResult filterInfoResult) {
            Bitmap originalImage = getOriginalImage();
            Bitmap filterBitmap = getFaceAreaImage();

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //计算面积容器
            int [] countArea = new int[width * height];
            filterBitmap.getPixels(countArea, 0, width, 0, 0, width, height);

            //Bitmap t = filterBitmap.copy(filterBitmap.getConfig(),false);

            Mat filterMat = new Mat();
            Utils.bitmapToMat(filterBitmap,filterMat);

            Mat oriMat = new Mat();
            Utils.bitmapToMat(filterBitmap,oriMat);

            Imgproc.cvtColor(filterMat,filterMat,Imgproc.COLOR_RGB2HSV);
            //Imgproc.cvtColor(filterMat,filterMat,Imgproc.COLOR_RGB2HSV);

            Mat dst = new Mat();

            Core.inRange(filterMat,new Scalar(35,43,46),
                    new Scalar(77,255,255),dst);
//
            Core.bitwise_not(dst,dst);
//
            Mat and = new Mat();

            Core.bitwise_and(oriMat,oriMat,and,dst);
//
            Mat yCrCb = new Mat();
//
            Imgproc.cvtColor(and,yCrCb,Imgproc.COLOR_RGB2HSV);
            Imgproc.cvtColor(yCrCb,yCrCb,Imgproc.COLOR_RGB2HSV);
//
            Core.inRange(yCrCb,new Scalar(78,43,46),
                    new Scalar(99,255,255),yCrCb);

            Utils.matToBitmap(yCrCb,filterBitmap);



            int [] originalPixels = new int[width * height];
            int [] filterPixels = new int[width*height];
            int [] resultPixels = new int[width*height];
            int [] areaPixels = new int[width * height];

            originalImage.getPixels(originalPixels, 0, width, 0, 0, width, height);
            filterBitmap.getPixels(filterPixels, 0, width, 0, 0, width, height);

            Bitmap areaBitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), Bitmap.Config.ARGB_8888);
            areaBitmap.setPixels(areaPixels, 0, width, 0, 0, width, height);


            float totalCount = 0;
            float count = 0;
            double totalColor = 0;

            int totalR = 0;
            int totalG = 0;
            int totalB = 0;

            for (int i = 0; i < filterPixels.length; i++) {
               if (countArea[i]!=0){
                   totalCount++;
               }
               areaPixels[i] = Color.BLACK;
               if (filterPixels[i]==Color.BLACK){
                  resultPixels[i] = originalPixels[i];
               }else {
                  areaPixels[i] = Color.WHITE;
                  double [] lab = new double[3];
                  ColorUtils.colorToLAB(originalPixels[i],lab);
                  lab[0] = lab[0] - (lab[0] * 0.2f);
                  resultPixels[i] = ColorUtils.LABToColor(lab[0],lab[1],lab[2]);
                  totalColor += colorDepth(originalPixels[i]);
                  count++;
                  totalR += Color.red(originalPixels[i]);
                  totalG += Color.green(originalPixels[i]);
                  totalB += Color.blue(originalPixels[i]);
               }
            }
            totalR = (int) (totalR / count);
            totalG = (int) (totalG / count);
            totalB = (int) (totalB / count);


            LogUtils.e(totalG,totalG,totalB);
            int color = Color.rgb(totalR,totalG,totalB);
            LogUtils.e(color);
            String rgbString = com.blankj.utilcode.util.ColorUtils.int2RgbString(color);

            //0.38  0.15 0.28
            //面积占比：level1——0.05 level2——0.05~0.15 level3——0.15~0.25 level4——0.25↑
            // 86 63 85
            //颜色平均深度：level1——255~127 level2——127~100 level3——100~60 level4——60~0
            //total:55  6:4
            float proportion = count / totalCount * 100f;
            LogUtils.e(proportion , count,totalCount);
            float score1 = 33;//39
            if (proportion<=5f){
                //30-33
                score1 = ((1 - (proportion / 5f)) * 3f) + 30;
            }else if (proportion>5f && proportion<=20f){
                //25-30
                score1 = ((1 - ((proportion - 5f) / 15f)) * 5f) + 25;
            }else if (proportion>20f && proportion<=30f){
                //20-25
                score1 = ((1 - ((proportion - 20f) / 10f)) * 5f) + 20;
            }else if (proportion>30f && proportion<=40f){
                //15-20
                score1 = ((1 - ((proportion - 30f) / 10f)) * 5f) + 15;
            }else if (proportion>40f && proportion<=50f){
                //5-15
                score1 = ((1 - ((proportion - 40f) / 10f)) * 10f) + 5f;
            }else if (proportion>50f){
                //5
                score1 = 5f;
            }

            float avgLight = (float) (totalColor / count);
            LogUtils.e(avgLight , totalColor,count);
            float score2 = 22;//6.5
            if (avgLight<=60){//level4
                //17-22
                score2 = (avgLight / 60 * 7f);
            }else if (avgLight>60 && avgLight<=70){
                //12-17
                score2 = (((avgLight - 60) / 10) * 5f) + 7f;
            }else if (avgLight>70 && avgLight<=127){
                //7-12
                score2 = (((avgLight - 70) / 57) * 5f) + 12f;
            }else if (avgLight>127 && avgLight<=255){
                //0-7
                score2 = (((avgLight - 127) / 128) * 5f) + 17f;
            }

            filterInfoResult.setScore((int) (30 + score1 + score2));
            LogUtils.e(score1,score2);
            //LogUtils.e(totalColor,count,totalColor/count,totalCount);

            filterBitmap.setPixels(resultPixels,0,width, 0, 0, width, height);
            areaBitmap.setPixels(areaPixels,0,width, 0, 0, width, height);

            filterMat.release();
            oriMat.release();
            dst.release();
            and.release();
            yCrCb.release();

            Mat areaMat = new Mat();
            Utils.bitmapToMat(areaBitmap,areaMat);
            filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
            filterInfoResult.setDataTypeString(getFilterDataType(),rgbString);
            filterInfoResult.setFilterBitmap(filterBitmap);
            filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
   }

   @Override
   public FacePart[] getFacePart() {
      return new FacePart[]{FacePart.FACE_EYE_BOTTOM};
   }

   @Override
   public FilterDataType getFilterDataType() {
      return FilterDataType.COLOR;
   }

    /**
     *
     * @param color
     * @return 0~255
     */
   private int colorDepth(int color){
       return (int) (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color));
   }


}
