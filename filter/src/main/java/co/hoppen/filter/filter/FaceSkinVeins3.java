package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceSkinVeins3 extends FaceFilter{

    /**
     * 增加眼角
     * 额头30 眼袋眼角50 脸下左右10/10
     */

    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
            Bitmap facePartBitmap = getFaceAreaImage();
                Bitmap operateBitmap = facePartBitmap.copy(facePartBitmap.getConfig(),true);

                Mat oriMat = new Mat();
                Utils.bitmapToMat(operateBitmap,oriMat);
                Imgproc.cvtColor(oriMat,oriMat,Imgproc.COLOR_RGBA2GRAY);
                //Imgproc.adaptiveThreshold(oriMat,oriMat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,19,0);

                Mat scharrYMat = new Mat();
                Imgproc.Scharr(oriMat,scharrYMat,-1,0,1);
                Mat scharrXMat = new Mat();
                Imgproc.Scharr(oriMat,scharrXMat,-1,1,0);

                Mat maxY = new Mat();

                Core.convertScaleAbs(scharrYMat,maxY);

                Mat maxX = new Mat();

                Core.convertScaleAbs(scharrXMat,maxX);

                Mat dstMat = new Mat();

                Core.addWeighted(maxY,0.5,maxX,0.5,0,dstMat);

                Imgproc.threshold(dstMat,dstMat,0,255,Imgproc.THRESH_BINARY);

//                Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//                Imgproc.erode(dstMat,dstMat,structuringElement);
//                Imgproc.dilate(dstMat,dstMat,structuringElement);

                int width = facePartBitmap.getWidth();
                int height = facePartBitmap.getHeight();

                int []partPixels = new int[width * height];
                int []mixPixels = new int[width * height];
                int []resultPixels = new int[width * height];
                int []oriPixels = new int[width * height];

                int [] areaPixels = new int[width * height];


                Utils.matToBitmap(dstMat,operateBitmap);

                facePartBitmap.getPixels(partPixels,0,width,0,0,width,height);
                operateBitmap.getPixels(mixPixels,0,width,0,0,width,height);
                getOriginalImage().getPixels(oriPixels,0,width,0,0,width,height);


                // peak valley
                float peakCount = 0;
                float valleyCount = 0;
//                float maxL = 0;
//                float minL = 255;


                //areaPixels[i] = Color.WHITE; 区域颜色
                for (int i = 0; i < partPixels.length; i++) {
                    int oriPixel = partPixels[i];
                    if (Color.alpha(oriPixel)!=0){
                        int mixPixel = mixPixels[i];
                        if (mixPixel==Color.WHITE){
                            int gray = color2Gray(oriPixel);
                            int color = oriPixels[i];
                            if (gray>=127){
//                                color = Color.RED;
                                float [] hsl = new float[3];
                                ColorUtils.colorToHSL(Color.YELLOW,hsl);
//                                if (minL>gray){
//                                    minL = gray;
//                                }
                                hsl[1] = gray / 255f;
                                color = ColorUtils.HSLToColor(hsl);
                                areaPixels[i] = Color.WHITE;
                                valleyCount ++;
                            }else color = oriPixels[i];
                            resultPixels[i] = color;
                        }else {
                            int color = Color.parseColor("#1e69ff");
                            float [] hsl = new float[3];
                            ColorUtils.colorToHSL(oriPixel,hsl);
                            if (hsl[2]>=0.5f){
                                ColorUtils.colorToHSL(color,hsl);
                                int gray = color2Gray(oriPixel);
                                hsl[1] = gray / 255f;
                                if (hsl[1]>=0.3){
                                    //峰
                                    color = ColorUtils.HSLToColor(hsl);
                                    areaPixels[i] = Color.WHITE;
                                    peakCount++;
//                                    if (maxL<gray){
//                                        maxL = gray;
//                                    }
                                }else {
                                    color = oriPixels[i];
                                }
                            }else if (hsl[2]<0.3f){
                                color = oriPixels[i];
                            }else {
                                //color = Color.YELLOW;
                                ColorUtils.colorToHSL(color,hsl);
                                int gray = color2Gray(oriPixel);
                                color = Color.rgb(gray,gray,gray);
//                                if (gray>=120){
//                                    color = Color.RED;
//                                }else color = Color.rgb(gray,gray,gray);
                            }
                            resultPixels[i] = color;
                        }
                    }else resultPixels[i] = oriPixels[i];
                }
                //1、(0.2601649,0.7398351) 2、(0.2655076,0.73449236) 3、(0.2463235,0.7536765)
                LogUtils.e(peakCount,valleyCount,peakCount + valleyCount,peakCount / (peakCount + valleyCount),valleyCount / (peakCount + valleyCount));

                float peakPercent = peakCount * 100f / (peakCount + valleyCount);
                float score = 85;
                //level1 0~20 level2 20~25 level3 25~30 level4 30↑
                if (peakPercent<=20){
                    //75~85
                    score = ((1 - (peakPercent / 20)) * 10f) + 75f;
                }else if (peakPercent>20 && peakPercent<=25){
                    //60~75
                    score = ((1 - ((peakPercent - 20f) / 5f)) * 15f) + 60f;
                }else if (peakPercent>25 && peakPercent<=30){
                    //40~60
                    score = ((1 - ((peakPercent - 25f) / 5f)) * 20f) + 40f;
                }else if (peakPercent>30 && peakPercent<=35){
                    //20~40
                    score = ((1 - ((peakPercent - 30) / 5f)) * 20f) + 20f;
                }else score = 20f;

                filterInfoResult.setScore((int) score);
                filterInfoResult.setDataTypeString(getFilterDataType(),(double)peakPercent);


                Bitmap areaBitmap = Bitmap.createBitmap(operateBitmap.getWidth(),operateBitmap.getHeight(),operateBitmap.getConfig());
                areaBitmap.setPixels(areaPixels,0,width,0,0,width,height);
                Mat areaMat = new Mat();
                Utils.bitmapToMat(areaBitmap,areaMat);
                filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
                areaBitmap.recycle();

                Bitmap result = operateBitmap.copy(operateBitmap.getConfig(),true);
                result.setPixels(resultPixels,0,width, 0, 0, width, height);

                filterInfoResult.setFilterBitmap(result);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
    }

    private int color2Gray(int color){
        return (int) (Color.red(color) * 0.3 + Color.green(color) * 0.59 + Color.blue(color) * 0.11);
    }

    @Override
    public FacePart[] getFacePart() {
        //FacePart.FACE_NOSE_LEFT_RIGHT  //FACE_SKIN
        return new FacePart[]{FacePart.FACE_LEFT_RIGHT_AREA};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

//    @Override
//    public FaceAreaInfo createAreaBitmap(Object... obj) {
//        Bitmap bitmap = (Bitmap) obj[0];
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap,mat);
//        return createFaceArea(mat);
//    }

}