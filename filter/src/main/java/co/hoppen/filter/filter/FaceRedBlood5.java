package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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
public class FaceRedBlood5 extends FaceFilter{


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat operateMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),operateMat);
        Imgproc.cvtColor(operateMat,operateMat,Imgproc.COLOR_RGBA2RGB);


        Mat faceMat = new Mat();
        operateMat.copyTo(faceMat,getFaceMask());
        operateMat =  faceMat.clone();


        List<Mat> rgbList = new ArrayList<>();
        Core.split(operateMat,rgbList);

        CLAHE clahe = Imgproc.createCLAHE(8.0, new Size(8, 8));

        for (int i = 0; i < rgbList.size(); i++) {
            clahe.apply(rgbList.get(i),rgbList.get(i));
        }

        Mat mergeMat = new Mat();
        Core.merge(rgbList,mergeMat);

        Mat bilateral = new Mat();
        //双边滤波
        Imgproc.bilateralFilter(mergeMat,bilateral,25,10.0,10.0);

        Mat hsv = new Mat();
        Imgproc.cvtColor(bilateral,hsv,Imgproc.COLOR_RGB2HSV);

        Mat mask = new Mat();

        Core.inRange(hsv,new Scalar(120,76,50),new Scalar(300,255,230),mask);

        Mat kernel =  Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(9,9));

        Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_OPEN,kernel);

        Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_DILATE,kernel);

        Mat mouthMask = new Mat();
        Utils.bitmapToMat(getMouthAreaImage(),mouthMask);
        Imgproc.cvtColor(mouthMask,mouthMask,Imgproc.COLOR_RGBA2GRAY);
        Core.bitwise_not(mouthMask,mouthMask);
        Mat newMask = new Mat();
        mask.copyTo(newMask,mouthMask);


        Mat coutoursMat = newMask.clone();
        Imgproc.GaussianBlur(coutoursMat,coutoursMat,new Size(9,9),2.0,2.0);

        List<MatOfPoint> list = new ArrayList<>();

        Mat hierarchy = new Mat();

        Imgproc.findContours(coutoursMat,list,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);



        //Mat resultMat = faceMat;

        LogUtils.e(faceMat.toString());

        Mat deepen = new Mat(faceMat.rows(), faceMat.cols(), CvType.CV_8UC3, new Scalar(0, 0, 0));

        LogUtils.e(deepen.toString());

        Core.add(faceMat,deepen,faceMat,newMask);


        for (int i = 0; i < list.size(); i++) {
            MatOfPoint matOfPoint = list.get(i);
            MatOfPoint2f result = new MatOfPoint2f();
            MatOfPoint2f source = new MatOfPoint2f();
            source.fromList(matOfPoint.toList());
            Imgproc.approxPolyDP(source,result,4.0d,true);

            Point[] points = result.toArray();

            for (int j = 0; j < points.length; j++) {
                Imgproc.line(faceMat,points[j %points.length],points[(j+1)%points.length],new Scalar(0,255,0),2);
                //Imgproc.arrowedLine();
//                Imgproc.rline
            }
        }

        Bitmap resultBitmap = getOriginalImage().copy(Bitmap.Config.ARGB_8888,true);

        Utils.matToBitmap(faceMat,resultBitmap);

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
