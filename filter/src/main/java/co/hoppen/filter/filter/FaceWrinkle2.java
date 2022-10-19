package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle2 extends Filter{

    private int w = 5;

    private float sigma = 0.1f;

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap cacheBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);

                Mat oriMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,oriMat);

                Mat grayMat = new Mat();
                Imgproc.cvtColor(oriMat,grayMat,Imgproc.COLOR_RGBA2GRAY);

                Mat result = new Mat();


                LogUtils.e(Math.toDegrees(90));

                Mat gaborKernel = Imgproc.getGaborKernel(new Size(5, 5), 10, 0 ,5, 0.5, 0, CvType.CV_32F);

                Imgproc.filter2D(grayMat,result,-1,gaborKernel,new Point(-1,-1),0,Core.BORDER_CONSTANT);

                //Imgproc.morphologyEx(result,result,Imgproc.MORPH_CLOSE ,gaborKernel,new Point(-1,-1));

                Imgproc.threshold(result,result,50,255, Imgproc.THRESH_BINARY);

                Utils.matToBitmap(result,cacheBitmap);



                filterInfoResult.setFilterBitmap(cacheBitmap);
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
        LogUtils.e("start----------");
        for (int i = 0; i <data.length; i++) {
            LogUtils.e(Arrays.toString(data[i]));
        }
        LogUtils.e("end----------");
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

}