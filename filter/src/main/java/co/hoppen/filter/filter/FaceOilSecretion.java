package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public class FaceOilSecretion extends FaceFilter{

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
                Bitmap originalImage = getOriginalImage();
                Bitmap bitmap =  getFaceAreaImage();
                Mat yuvMat = new Mat();
                Utils.bitmapToMat(bitmap,yuvMat);
                Imgproc.cvtColor(yuvMat,yuvMat,Imgproc.COLOR_RGB2GRAY);
                Mat detect = new Mat();
                List<Mat> channels = new ArrayList<>();
                Core.split(yuvMat,channels);
                Mat outputMark = channels.get(0);
                Imgproc.threshold(outputMark,outputMark,0,255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
                yuvMat.copyTo(detect,outputMark);
                Utils.matToBitmap(detect,bitmap);

                yuvMat.release();
                detect.release();
                outputMark.release();

                int width = originalImage.getWidth();
                int height = originalImage.getHeight();

                int [] pixels = new int[width * height];
                int [] dst = new int[width * height];
                int [] original = new int[width * height];

                int [] area = new int[width * height];

                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                originalImage.getPixels(original, 0, width, 0, 0, width, height);

                int totalGray = 0;
                int count = 0;

                for (int i = 0; i < pixels.length; i++) {
                    int r = Color.red(pixels[i]);
                    int g = Color.green(pixels[i]);
                    int b = Color.blue(pixels[i]);
                    int color = Color.rgb(r,g,b);
                    if (color!=Color.BLACK){
                        int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
                        totalGray +=gray;
                        count ++;
                    }
                }

                int avgGray = (int) (totalGray/count + (totalGray/count * 0.17f));

                int areaCount = 0;

                for (int i = 0; i < pixels.length; i++) {
                    int r = Color.red(pixels[i]);
                    int g = Color.green(pixels[i]);
                    int b = Color.blue(pixels[i]);
                    int color = Color.rgb(r,g,b);
                    if (color==Color.BLACK) {
                        dst[i] = 0x00FFFFFF;
                        continue;
                    }else {
                        int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
                        if (gray>avgGray){
                            dst[i] = Color.parseColor("#ffff00");
                            area[i] = Color.WHITE;
                            areaCount++;
                        }else {
                            dst[i] = 0x00FFFFFF;
                        }
                    }
                }
                Bitmap topBitmap = Bitmap.createBitmap(dst,width,height, Bitmap.Config.ARGB_8888);
                Mat top = new Mat();
                Utils.bitmapToMat(topBitmap,top);
                Mat oriMat = new Mat();
                Utils.bitmapToMat(getOriginalImage(),oriMat);
                Core.addWeighted(oriMat,1,top,0.2,0,oriMat);
                Utils.matToBitmap(oriMat,topBitmap);
                top.release();
                oriMat.release();

                //区域绘制
                Mat mat = new Mat();
                Utils.bitmapToMat(Bitmap.createBitmap(area,width,height, Bitmap.Config.ARGB_8888),mat);
                filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(mat));

                int skinArea = FaceSkinUtils.getSkinArea();
                if (skinArea>0 && skinArea>areaCount){
                    double data =  areaCount * 100d / skinArea;
                    filterInfoResult.setDataTypeString(getFilterDataType(),data);
                }
                filterInfoResult.setFilterBitmap(topBitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        }catch (Exception e){
                filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_T,FacePart.FACE_LEFT_RIGHT_AREA};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

}
