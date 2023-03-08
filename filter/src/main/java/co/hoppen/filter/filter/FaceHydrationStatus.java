package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Random;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * Created by YangJianHui on 2022/3/3.
 */
public class FaceHydrationStatus extends FaceFilter{
    /**
     *  计算分值占比:脸颊部分75-80 额头25-20
     *
     */

    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {

                Bitmap originalImage = getOriginalImage();
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int count = width * height;

                int [] originalPixels = new int[count];
                int [] filterPixels = new int[count];
                int [] areaPixels = new int[count];

                originalImage.getPixels(originalPixels,0,width,0,0,width,height);
                int totalGray = 0;

                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    int R = Color.red(originalPixel);
                    int G = Color.green(originalPixel);
                    int B = Color.blue(originalPixel);
                    int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
                    totalGray+=gray;
                    filterPixels[i] = gray;
                    //if (gray>10)areaCount++;
                }
                int avgGray = totalGray / count;
                double totalCountPixels = 0;
                double totalWaterPixels = 0;
                float totalPercentPixels = 0;
                double hash = 0;
                for (int i = 0; i <originalPixels.length ; i++) {
                    int gray = filterPixels[i];
                    filterPixels[i] = 0x00ffffff;
                    if (gray >= avgGray * 1.2 && gray <= 250) {
                        totalCountPixels++;
                    }

                    if (gray >= avgGray * 1.2 && gray > 250) {
                        totalWaterPixels++;
                    }

                    if (gray <avgGray * 1.2){

                    } else if (gray>90 && gray<=110){

                    }else if (gray>=150){
                        totalPercentPixels++;
                        filterPixels[i]=0X801259FF;
                        areaPixels[i] = Color.WHITE;
                    }
                }

                float score = 85;

                //获取记录的皮肤区域大小
                float skinArea = FaceSkinUtils.getSkinArea();
                //LogUtils.e(skinArea);
                if (skinArea==0){
                    skinArea = count * 0.4f;
                }
                    if (totalPercentPixels<skinArea){
                        float percent = totalPercentPixels * 100f / skinArea;
                        //LogUtils.e(percent);
                        //level1 = 50↑ level2 = 40 - 50 level3 = 25 - 40 level4 = 0 - 25
                        if (percent>0 && percent<=10){
                            //35-40
                            score = ((percent / 10f) * 20f)  + 20f;
                        }else if (percent>10 && percent<=30){
                            //45-60
                            score = (((percent-10f) / 20f) * 20f)  + 40f;
                        }else if (percent>30 && percent<=50){
                            //60-65
                            score = (((percent-30f) / 20f) * 10f)  + 60f;
                        }else if (percent>50 &&percent<=100){
                            //65-85
                            score = (((percent - 50f) / 50f) * 15f)  + 70f;
                        }
                    }else score = 65f;


                filterInfoResult.setScore((int) score);
//                totalCountPixels = totalWaterPixels * 60 + totalCountPixels * 2;
//                hash = totalCountPixels / count;


                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);

                Bitmap areaBitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                areaBitmap.setPixels(areaPixels,0,width, 0, 0, width, height);

                Mat areaMat = new Mat();
                Utils.bitmapToMat(areaBitmap,areaMat);
                areaBitmap.recycle();


                //int skinArea = FaceSkinUtils.getSkinArea();
                if (skinArea>0 && skinArea>totalPercentPixels){
                    double data =  totalPercentPixels * 100d / skinArea;
                    filterInfoResult.setDataTypeString(getFilterDataType(),data);
                }

                Bitmap resultBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(resultBitmap);
                canvas.drawBitmap(originalImage,0,0,null);
                canvas.drawBitmap(bitmap,0,0,null);

                filterInfoResult.setFilterBitmap(resultBitmap);
                filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
    }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_SKIN};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }
}
