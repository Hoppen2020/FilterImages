package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle extends FaceFilter {

    private int w = 5;

    private float sigma = 0.1f;

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult =getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap createBitmap = getFaceAreaImage();

                Mat oriMat =new Mat();
                Utils.bitmapToMat(createBitmap,oriMat);

                Mat filterMat = new Mat();
                Utils.bitmapToMat(createBitmap,filterMat);

                List<Mat> rgbList = new ArrayList<>();

                Core.split(filterMat,rgbList);

                Mat gMat = rgbList.get(1);

                Imgproc.equalizeHist(gMat,filterMat);

                Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));

                Imgproc.morphologyEx(filterMat,filterMat,Imgproc.MORPH_BLACKHAT,structuringElement);

                Core.inRange(filterMat,new Scalar(30,30,30),new Scalar(255,255,255),filterMat);

                Utils.matToBitmap(filterMat,createBitmap);

                oriMat.release();
                filterMat.release();
                gMat.release();
                structuringElement.release();

//                Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
//                Canvas canvas = new Canvas(resultBitmap);
//                canvas.drawBitmap(createBitmap,0,0,null);
//                if (!createBitmap.isRecycled())createBitmap.recycle();

                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int count = width * height;
                int [] originalPixels = new int[count];
                int [] filterPixels = new int[count];
                int [] result = new int[count];
                originalImage.getPixels(originalPixels,0,width,0,0,width,height);
                createBitmap.getPixels(filterPixels,0,width,0,0,width,height);

                for (int i = 0; i < filterPixels.length; i++) {
                    if (filterPixels[i]==Color.BLACK){
                        result[i] = originalPixels[i];
                    }else {
                        result[i] = Color.rgb(255,238,43);
                    }
                }
                createBitmap = Bitmap.createBitmap(result,width,height, Bitmap.Config.ARGB_8888);

                filterInfoResult.setFilterBitmap(createBitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

        } catch (Exception e) {
            LogUtils.e(e.toString());
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }

    private float [][] createKernel(){
        //LogUtils.e((2 * w + 1) * (2 * w + 1),(2 * w + 1));
        return new float[(2 * w + 1)][(2* w + 1)];
    }

    private void printlnArray(float[][] data){
        for (int i = 0; i <data.length; i++) {
            LogUtils.e(Arrays.toString(data[i]));
        }
    }

    private float[] change(float[][] data){
        List<Float> list = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            float c [] =  data[i];
            for (int j = 0; j < c.length; j++) {
                float d =  c[j];
                list.add(d);
            }
        }
        float [] re = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            re[i] = list.get(i);
        }
        LogUtils.e(Arrays.toString(re));
        return re;
    }

    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FacePart.FACE_FOREHEAD,FacePart.FACE_NOSE_LEFT_RIGHT,FacePart.FACE_EYE_BOTTOM};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

}