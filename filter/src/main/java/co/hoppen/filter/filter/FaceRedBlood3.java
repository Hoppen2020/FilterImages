package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceRedBlood3 extends FaceFilter{


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat operateMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),operateMat);

//          Mat grayMat = new Mat();
//          Imgproc.cvtColor(operateMat,grayMat,Imgproc.COLOR_RGB2GRAY);

//          CLAHE clahe = Imgproc.createCLAHE(2, new Size(8, 8));
//          clahe.apply(grayMat,grayMat);
//
//          Imgproc.cvtColor(grayMat,operateMat,Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGB2HSV);
        List<Mat> splitList = new ArrayList<>();
        Core.split(operateMat,splitList);

        byte [] hBytes = new byte[splitList.get(0).channels() * splitList.get(0).cols()];
        byte [] sBytes = new byte[splitList.get(1).channels() * splitList.get(1).cols()];
        byte [] vBytes = new byte[splitList.get(2).channels() * splitList.get(2).cols()];

//        List<Integer> hlist = new ArrayList<>();
//        List<Integer> slist = new ArrayList<>();

        for (int h = 0; h < splitList.get(0).rows(); h++) {
            splitList.get(0).get(h,0,hBytes);
            splitList.get(1).get(h,0,sBytes);
            splitList.get(2).get(h,0,vBytes);
            for (int w = 0; w < splitList.get(0).cols(); w++) {
                int index = splitList.get(0).channels() * w;
                int hValue = hBytes[w] & 0xff;
                int sValue = sBytes[w] & 0xff;
                int vValue = sBytes[w] & 0xff;
                if (hValue <= 10 || hValue >= 150 && hValue <= 180){ //h
                    if (sValue>=43){ // s
                        if (vValue>=64){ //v
                            if (sValue>=149){
                                sValue = (int) (sValue + (sValue * 0.4f));
                                if (sValue>255)sValue = 255;
                            }else {
                                sValue  = (int) (sValue - (sValue * 0.1f));
                                if (sValue<20){
                                    sValue = 20;
                                }
                            }
                            sBytes[index] = (byte) sValue;
                        }
                    }
                }
            }
            splitList.get(1).put(h,0,sBytes);
        }

        //LogUtils.e(hlist.toString(),slist.toString());


        splitList.set(0,new Mat(operateMat.size(), CvType.CV_8UC1,new Scalar(0)));
        Imgproc.equalizeHist(splitList.get(1),splitList.get(1));
        //Imgproc.equalizeHist(splitList.get(2),splitList.get(2));

        //直方图均衡化
        CLAHE clahe = Imgproc.createCLAHE(2, new Size(2, 2));
        clahe.apply(splitList.get(1),splitList.get(1));

        Core.merge(splitList,operateMat);

        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_HSV2RGB);

        Mat maskMat = getFaceMask();

        byte [] maskByte = new byte[maskMat.channels() * maskMat.cols()];
        byte [] operateByte = new byte[operateMat.channels() * operateMat.cols()];

        float count = 0;
        float totalCount = 0;

        for (int h = 0; h < operateMat.rows(); h++) {
            operateMat.get(h,0,operateByte);
            maskMat.get(h,0,maskByte);
            for (int w = 0; w < operateMat.cols(); w++) {
                int index = operateMat.channels() * w;
                int maskGray = maskByte[w] & 0xff;
                if (maskGray!=0){
                    totalCount++;
                    int r = operateByte[index]&0xff;
                    if (r<=110){
                        count++;
                    }
                }else {
                    operateByte[index] = 0;
                    operateByte[index +1] = 0;
                    operateByte[index + 2] = 0;
                }
            }
            operateMat.put(h,0,operateByte);
        }

        float score = 85f;
        float percent = count * 100f /totalCount;

        LogUtils.e(totalCount,count,percent);
        //70 71 29 61 80 69
        //level1 0~30 level2 30~50 level3 50~60 level4 60~70 70~80 80~100
        if (percent<=30){
            //75~85
            score = ((1-(percent / 30f)) * 10f)  + 75f;
        }else if (percent>30 && percent<=50){
            //65~75
            score = ((1-((percent - 30f) / 20f)) * 10f)  + 65f;
        }else if (percent>50 && percent<=60){
            //55~65
            score = ((1-((percent - 50f) / 10f)) * 10f)  + 55f;
        }else if (percent>60 && percent<=70){
            //45~55
            score = ((1-((percent - 60f) / 10f)) * 10f)  + 45f;
        }else if (percent>70 && percent<=80){
            //35~45
            score = ((1-((percent - 70f) / 10f)) * 10f)  + 35f;
        }else if (percent>80 &&percent<=90){
            //20~35
            score = ((1-((percent - 80f) / 10f)) * 15f)  + 20f;
        }else {
            score = 20f;
        }
        filterInfoResult.setScore((int) score);
        filterInfoResult.setDataTypeString(getFilterDataType(),(double)percent);

        Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(operateMat,resultBitmap);

        Mat areaMat = new Mat();
        Utils.bitmapToMat(getFaceAreaImage(),areaMat);
        filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat,1));

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
