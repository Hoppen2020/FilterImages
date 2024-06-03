package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles4 extends FaceFilter {

    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat oriMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),oriMat);
//        Imgproc.cvtColor(oriMat,oriMat,Imgproc.COLOR_RGBA2RGB);
//        LogUtils.e(oriMat.channels());


        Mat hsvImage = new Mat();
        Imgproc.cvtColor(oriMat, hsvImage, Imgproc.COLOR_RGB2HSV);
        // 分离通道
        List<Mat> channels = new ArrayList<>();
        Core.split(hsvImage, channels);
        // 增加亮度
        double alpha = 1.5; // 增加的常数
        Core.multiply(channels.get(2), new Scalar(alpha), channels.get(2));
        // 合并通道
        Core.merge(channels, hsvImage);

//        Imgproc.equalizeHist(grayMat,grayMat);

//        Imgproc.threshold(grayMat,grayMat,0,255,Imgproc.THRESH_OTSU);

        byte [] p = new byte[hsvImage.channels()*hsvImage.cols()];

        for (int h = 0; h < hsvImage.rows(); h++) {
            hsvImage.get(h,0,p);
            for (int w = 0; w < hsvImage.cols(); w++) {
                int index = hsvImage.channels() * w;
                int brightness = p[index+2] & 0xff;
                if (brightness<110){
                    brightness = 255;
//                    if (brightness<=70){
//                        brightness = 200;
//                    }
//                    int H = p[index] & 0xff;
//                    int S = p[index+1] & 0xff;
                    p[index] = (byte) 180;
                    p[index + 1] = (byte) 0;
                }
                p[index+2] = (byte) brightness;
            }
            hsvImage.put(h,0,p);
        }


        // 转换回 RGB 色彩空间
        Mat rgbMat = new Mat();
        Imgproc.cvtColor(hsvImage, rgbMat, Imgproc.COLOR_HSV2RGB);


        Mat grayMat = new Mat();
        Imgproc.cvtColor(rgbMat,grayMat,Imgproc.COLOR_RGB2GRAY);

//        Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
//        Imgproc.dilate(grayMat,grayMat,structuringElement);
//        Imgproc.erode(grayMat,grayMat,structuringElement);

//        Mat mask = new Mat();
//        Utils.bitmapToMat(getFaceAreaImage(),mask);
//        Imgproc.cvtColor(mask,mask,Imgproc.COLOR_RGBA2GRAY);

//        Core.bitwise_not(oriMat,oriMat,mask);

        //灰度程度阶梯
        float cs0=0.00f;
        float cs1=15.00f;//15
        float cs2=115.00f;
        float cs3=190.00f;
        float cs4=255.00f;

        //H值
        float a0=0.00f;
        float a1=10.00f/180.00f;
        float a2=14.00f/180.00f;
        float a3=10.00f/180.00f;
        float a4=0.00f;

        //S值
        float b0=0f;
        float b1=0.01f;
        float b2=0.5f;
        float b3=0.99f;
        float b4=1f;

        //V值
        float c0=1.00f;
        float c1=240.00f/255.00f;
        float c2=125.00f/255.00f;
        float c3=100.00f/255.00f;
        float c4=96.00f/255.00f;

//        p = new byte[grayMat.channels() * grayMat.cols()];
//        for (int h = 0; h < grayMat.rows(); h++) {
//            grayMat.get(h, 0, p);
//            for (int w = 0; w < grayMat.cols(); w++) {
//                int index = grayMat.channels() * w;
//                float percent = 0.2f;
//                int G = (int)p[index] & 0xff;
//                G = (int) (percent * G + 255 * (1 - percent));
//                p[index] = (byte) G;
//            }
//            grayMat.put(h, 0, p);
//        }

        Mat mixHMat = new Mat(oriMat.size(), CvType.CV_32FC3);
        List<Mat> hsvSplit = new ArrayList<>();
        Core.split(mixHMat,hsvSplit);
        Mat matH = hsvSplit.get(0);
        matH.convertTo(matH,CvType.CV_32FC1,1.0/180d);
        Mat matS = hsvSplit.get(1);
        matS.convertTo(matS,CvType.CV_32FC1,1.0/255d);
        Mat matV = hsvSplit.get(2);
        matV.convertTo(matV,CvType.CV_32FC1,1.0/255d);

        float[] hsvHP = new float[matH.channels() * grayMat.cols()];
        float[] hsvSP = new float[matS.channels() * grayMat.cols()];
        float[] hsvVP = new float[matV.channels() * grayMat.cols()];

        for (int h = 0; h < grayMat.rows(); h++) {
            grayMat.get(h, 0, p);
            matH.get(h,0,hsvHP);
            matS.get(h,0,hsvSP);
            matV.get(h,0,hsvVP);
            for (int w = 0; w < grayMat.cols(); w++) {
                int index = grayMat.channels() * w;
                int grayP = p[index] & 0xff;
                float hsvH = 0;
                float hsvS = 0;
                float hsvV = 0;
                    if (grayP<cs1){
                        //0~15
//                        hsvH = a0 - (((a1-a0)*cs0) / (cs1-cs0)) + (((a1-a0) * grayP)/(cs1-cs0));
//                        hsvS = b0 - (((b1-b0)*cs0) /(cs1-cs0)) + (((b1-b0) * grayP)/(cs1-cs0));
//                        hsvV = c0 - (((c1-c0)*cs0) /(cs1-cs0)) + (((c1-c0) * grayP)/(cs1-cs0));

                    }else if (grayP>=cs1&&grayP<cs2){
                        //15~115
                        float percent =  (grayP - cs1) / (cs2 - cs1);
                        grayP = (int) ((cs3 - cs2) * (1-percent) + cs2);

                        hsvH = a2 -(((a3-a2)*cs2)/(cs3-cs2)) + (((a3-a2) * grayP) / (cs3-cs2));
                        hsvS = b2 -(((b3-b2)*cs2)/(cs3-cs2)) + (((b3-b2) * grayP * 0.9f) / (cs3-cs2));
                        hsvV = c1 -(((c3-c1)*cs1)/(cs3-cs1)) + (((c3-c1) * grayP) / 255f);

                    }else if (grayP>=cs2&&grayP<cs3){
                        //115~190
//                        hsvH = a1 - (((a2-a1)*cs1)/(cs2-cs1)) + (((a2-a1) * grayP) / (cs2-cs1));
//                        hsvS = b1 - (((b2-b1)*cs1)/(cs2-cs1)) + (((b2-b1) * grayP) / (cs2-cs1));
//                        hsvV = c1 - (((c2-c1)*cs1)/(cs2-cs1)) + (((c2-c1) * grayP) / 255f);

                    }else if (grayP>=cs3&&grayP<cs4){
                        //190~255
//                        hsvH = a3 - (((a4-a3)*cs3) / (cs4-cs3)) + (((a4-a3)*grayP) / (cs4-cs3));
//                        hsvS = b3 - (((b4-b3)*cs3) / (cs4-cs3)) + (((b4-b3)*grayP) / (cs4-cs3));
//                        hsvV = c3 - (((c4-c3)*cs1) / (cs4-cs3)) + (((c4-c3)*grayP) / 255f);

                    }
                int hsvIndex = matH.channels() * w;
                hsvHP[hsvIndex] = hsvH;
                hsvSP[hsvIndex] = hsvS;
                hsvVP[hsvIndex] = hsvV;
            }
            matH.put(h,0,hsvHP);
            matS.put(h,0,hsvSP);
            matV.put(h,0,hsvVP);
            grayMat.put(h, 0, p);
        }

        //LogUtils.e("MAX "+maxH+" "+maxH+" "+maxV,"MIN "+minH+" "+minS+" "+minV);

        Mat brownMat = new Mat();
        hsvSplit.clear();
        matH.convertTo(matH,CvType.CV_8UC1,180.0d);
        matS.convertTo(matS,CvType.CV_8UC1,255.0d);
        matV.convertTo(matV,CvType.CV_8UC1,255.0d);

        hsvSplit.add(matH);
        hsvSplit.add(matS);
        hsvSplit.add(matV);
        Core.merge(hsvSplit,brownMat);

        Imgproc.cvtColor(brownMat,brownMat,Imgproc.COLOR_HSV2RGB);

        Mat maskMat = new Mat();
        Utils.bitmapToMat(getFaceAreaImage(),maskMat);
        Imgproc.cvtColor(maskMat,maskMat,Imgproc.COLOR_RGBA2GRAY);

        p = new byte[oriMat.channels() * oriMat.cols()];

        byte[] maskP = new byte[maskMat.channels() * maskMat.cols()];
        byte[] brownP = new byte[brownMat.channels() * brownMat.cols()];

//        List<Integer> list = new ArrayList<>();

//        int maxGray = 0;
//        int minGray = 255;
//        double totalGray = 0;
        int count = 0;
        int totalCount = 0;

        for (int h = 0; h < oriMat.rows(); h++) {
            oriMat.get(h,0,p);
            maskMat.get(h,0,maskP);
            brownMat.get(h,0,brownP);
            for (int w = 0; w < oriMat.cols(); w++) {
                int maskIndex = maskMat.channels() * w;
                int maskGray = maskP[maskIndex] & 0xff;
                if (maskGray!=0){
                    int brownIndex = brownMat.channels() * w;
                    int index = oriMat.channels() * w;
                    int brownR = brownP[brownIndex] & 0xff;
                    int brownG = brownP[brownIndex + 1] & 0xff;
                    int brownB = brownP[brownIndex + 2] & 0xff;
                    if (brownR==0 && brownG==0 && brownB==0){
//                        maskGray = 255 - maskGray;
//                        p[index] = (byte) maskGray;
//                        p[index+1]= (byte) maskGray;
//                        p[index+2]= (byte) maskGray;
                    }else {

                        int resultR =Math.min(brownR + (p[index]& 0xff),255);
                        int resultG = Math.min(brownG + (p[index + 1]& 0xff),255);
                        int resultB = Math.min(brownB + (p[index + 2]& 0xff),255);


//                        int AR = (int) (0.393 * resultR + 0.769 * resultG + 0.189 * resultB);
//                        int AG = (int) (0.349 * resultR + 0.686 * resultG + 0.168 * resultB);
                        int AB = (int) (0.272 * resultR + 0.534 * resultG + 0.131 * resultB);
                        int gray = (int) ( 0.299* resultR + 0.587* resultG + 0.114* resultB);
                        totalCount++;
//                        maxGray = Math.max(gray,maxGray);
//                        minGray = Math.min(gray,minGray);

//                        AR = (AR > 255 ? 255 : (Math.max(AR, 0)));
//                        AG = (AG > 255 ? 255 : (Math.max(AG, 0)));
                        AB = (AB > 255 ? 255 : (Math.max(AB, 0)));

                        if (AB<175){

                            int oriR = (int) ((p[index]& 0xff) *.6);
                            int oriG = (int) ((p[index + 1]& 0xff));
                            int oriB = (int) ((p[index + 2]& 0xff)*.9);

                            p[index] = (byte) 160;//160
                            p[index+1] =(byte) 141;//141
                            p[index+2] = (byte) 0;//0
                            count++;
                        }
                    }
                }
            }
            oriMat.put(h,0,p);
        }

        float percent = count * 100f / totalCount;
        float score = 85;

        if (percent<=20){
            //75~85
            score = ((1 - (percent / 20)) * 10f) + 75f;
        }else if (percent>20 && percent<=25){
            //60~75
            score = ((1 - ((percent - 20f) / 5f)) * 15f) + 60f;
        }else if (percent>25 && percent<=65){
            //40~60
            score = ((1 - ((percent - 25f) / 40f)) * 20f) + 40f;
        }else if (percent>65 && percent<=100){
            //20~40
            score = ((1 - ((percent - 65) / 35f)) * 20f) + 20f;
        }else score = 20f;

        filterInfoResult.setScore((int) score);
        filterInfoResult.setDataTypeString(getFilterDataType(),(double)percent);

        //LogUtils.e(totalCount,count,count * 100 /totalCount);
//        LogUtils.e(list.toString());


        Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(oriMat,resultBitmap,true);

       // Bitmap topBitmap = Bitmap.createBitmap(dst,width,height, Bitmap.Config.ARGB_8888);
//        Mat top = new Mat();
//        Utils.bitmapToMat(topBitmap,top);
//        Mat oriMat = new Mat();
        Mat o = new Mat();
        Utils.bitmapToMat(getOriginalImage(),o);
        Core.addWeighted(o,0.8,oriMat,0.2,0,o);
        Utils.matToBitmap(o,resultBitmap);
        o.release();
        oriMat.release();




        Mat areaMat = new Mat();
        Utils.bitmapToMat(getFaceAreaImage(),areaMat);
        filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(areaMat));
        areaMat.release();
        filterInfoResult.setFilterBitmap(resultBitmap);
        filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
    }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_EYE_BOTTOM};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

    /**
     *
     * @param color
     * @return 0~255
     */
    private int colorDepth(int color){
        return (int) (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color));
    }

//    private int lightenColor(int color ,double factor){
//        return (int) Math.min(color + (255 - color) * factor, 255);
//    }

}
