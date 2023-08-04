package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

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
 * Created by YangJianHui on 2022/3/28.
 */
public class FaceDarkCircles5 extends FaceFilter {

    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat oriMat = new Mat();
        Utils.bitmapToMat(getFaceAreaImage(),oriMat);
        Imgproc.cvtColor(oriMat,oriMat,Imgproc.COLOR_RGBA2RGB);




        Bitmap resultBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(oriMat,resultBitmap);

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
        return FilterDataType.COLOR;
    }

    /**
     *
     * @param color
     * @return 0~255
     */
    private int colorDepth(int color){
        return (int) (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color));
    }


}
