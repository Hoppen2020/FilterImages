package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * 表面斑 RGB light
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceEpidermisSpots extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
                Bitmap originalImage = getOriginalImage();
                Bitmap cacheBitmap = getFaceAreaImage();

                Mat resultMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,resultMat);

                Mat frameMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,frameMat);

                Imgproc.cvtColor(frameMat,frameMat,Imgproc.COLOR_RGBA2GRAY);

                Imgproc.equalizeHist(frameMat,frameMat);

                Imgproc.GaussianBlur(frameMat,frameMat,new Size(3,3),0);

                Imgproc.adaptiveThreshold(frameMat,frameMat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,99,10);

                Imgproc.morphologyEx(frameMat,frameMat,Imgproc.MORPH_OPEN,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));

                List<MatOfPoint> list = new ArrayList<>();

                Mat hierarchy = new Mat();

                Imgproc.findContours(frameMat,list,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

//                Mat areaMat = new Mat(resultMat.size(),CvType.CV_8UC3);

                int count = 0;

                for (int i = 0; i < list.size(); i++) {
                    MatOfPoint point = list.get(i);
                    if (point.size().area()>5 && point.size().area()<=200){
                        count++;
                        Imgproc.drawContours(resultMat,list,i,new Scalar(255,0,0,255));
//                        Imgproc.drawContours(areaMat,list,i,new Scalar(255,0,0,255));
                    }
                }

                Utils.matToBitmap(resultMat,cacheBitmap);

                //resultMat.release();
                frameMat.release();
                hierarchy.release();

                Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
                //Bitmap areaBitmap = Bitmap.createBitmap(originalImage.getWidth(),originalImage.getHeight(),originalImage.getConfig());
                Canvas canvas = new Canvas(resultBitmap);
                canvas.drawBitmap(cacheBitmap,0,0,null);
                if (!cacheBitmap.isRecycled())cacheBitmap.recycle();
                filterInfoResult.setFilterBitmap(resultBitmap);
                filterInfoResult.setFaceAreaInfo(createFaceAreaInfo(resultMat,1));
                filterInfoResult.setDataTypeString(getFilterDataType(),count);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        } catch (Exception e) {
            LogUtils.e(e.toString());
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_FOREHEAD,FacePart.FACE_LEFT_RIGHT_AREA};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.COUNT;
    }

}