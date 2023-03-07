package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */

public class FaceNearRedLight extends FaceFilter{

    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat operateMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),operateMat);
        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);

        List<Mat> splitList = new ArrayList<>();
        Core.split(operateMat,splitList);
        splitList.set(0,new Mat(operateMat.size(), CvType.CV_8UC1,new Scalar(0)));

        float percent = 0f;

        Mat matS = splitList.get(1);

        byte[] p = new byte[matS.channels() * matS.cols()];

        for (int h = 0; h < matS.rows(); h++) {
            matS.get(h, 0, p);
            for (int w = 0; w < matS.cols(); w++) {
                int index = matS.channels() * w;
                int value = p[index] & 0xff;
                percent = value / 255f + 0.1f;
                int nValue = (int) (value + percent * value);
                if (nValue>=255) {
                    nValue = 255;
                }else if (nValue<=0){
                    nValue = 0;
                }
                p[index] = (byte) nValue;
            }
            matS.put(h, 0, p);
        }

        Core.merge(splitList,operateMat);
        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);

        double oriBrightness = Core.mean(operateMat).val[0];

        double brightness = oriBrightness + (oriBrightness * 0.5d);

//        LogUtils.e(oriBrightness,brightness);

        Core.add(operateMat,new Scalar(brightness - oriBrightness,brightness - oriBrightness,brightness - oriBrightness),operateMat);


        //RGB
        byte[] rgbMat = new byte[operateMat.channels() * operateMat.cols()];

        Mat faceMask = getFaceMask();
        byte[] maskP = new byte[faceMask.channels() * faceMask.cols()];


        float count = 0;
        float totalCount = 0;

        for (int h = 0; h < operateMat.rows(); h++) {
            operateMat.get(h, 0, rgbMat);
            faceMask.get(h,0,maskP);
            for (int w = 0; w < operateMat.cols(); w++) {
                int index = operateMat.channels() * w;
                int maskGray = maskP[w] & 0xff;
                if (maskGray!=0){
                    int r = rgbMat[index] & 0xff;
                    if (r<=110){
                        //r值越小越红
                        count++;
                    }
                    totalCount++;
                }else {
                    rgbMat[index] = 0;
                    rgbMat[index + 1] = 0;
                    rgbMat[index + 2] = 0;
                }
            }
            operateMat.put(h, 0, rgbMat);
        }
        //32 89 84 86 95 84
        LogUtils.e(count,totalCount,count * 100f / totalCount);
        float score = 85f;
        float areaPercent = count * 100f / totalCount;
        //level1 10~20 level2 20~ 30 level3 30~40 level4 40~50
        if (areaPercent<=20){
            //70~85
            score = ((1-(areaPercent / 20f)) * 15f)  + 70f;
        }else if (areaPercent>20 && areaPercent<=30){
            //60~70
            score = ((1-((areaPercent - 20f) / 10f)) * 10f)  + 60f;
        }else if (areaPercent>30 && areaPercent<=40){
            //50~60
            score = ((1-((areaPercent - 30f) / 10f)) * 10f)  + 50f;
        }else if (areaPercent>40 && areaPercent<=50){
            //30~50
            score = ((1-((areaPercent - 40f) / 10f)) * 20)  + 30f;
        }else {
            score = 20;
        }
        filterInfoResult.setScore((int) score);

        filterInfoResult.setDataTypeString(getFilterDataType(),(double)areaPercent);

        Mat areaMat = new Mat();
        Utils.bitmapToMat(getFaceAreaImage(),areaMat);
        filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));


        Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);

        //operateMat = getFilterFaceMask(operateMat);

        Utils.matToBitmap(operateMat,resultBitmap);
        filterInfoResult.setFilterBitmap(resultBitmap);

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

