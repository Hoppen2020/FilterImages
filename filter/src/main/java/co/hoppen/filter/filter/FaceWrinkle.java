package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

import static co.hoppen.filter.FacePart.FACE_FOREHEAD;
import static co.hoppen.filter.FacePart.FACE_LEFT_RIGHT_AREA;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.LSD_REFINE_ADV;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        Mat operateMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),operateMat);
        Imgproc.cvtColor(operateMat,operateMat,COLOR_RGB2GRAY);
        Imgproc.equalizeHist(operateMat,operateMat);
        CLAHE clahe = Imgproc.createCLAHE(4.0d, new Size(8, 8));
        clahe.apply(operateMat,operateMat);

        Imgproc.adaptiveThreshold(operateMat,operateMat,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY_INV,9,8);

        Mat fullFaceMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),fullFaceMat);
        Core.add(fullFaceMat,fullFaceMat,fullFaceMat,operateMat);

        Imgproc.cvtColor(fullFaceMat,fullFaceMat,COLOR_RGB2GRAY);

        LineSegmentDetector lineSegmentDetector = Imgproc.createLineSegmentDetector(LSD_REFINE_ADV);
        Mat lineMat = new Mat();
        lineSegmentDetector.detect(fullFaceMat,lineMat);

        //LogUtils.e(fullFaceMat.toString(),lineMat.toString());

        Mat oriMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),oriMat);
        Imgproc.cvtColor(oriMat,oriMat,COLOR_RGBA2RGB);


        lineSegmentDetector.drawSegments(oriMat,lineMat);
//                Imgproc.cvtColor(oriMat,oriMat,COLOR_RGB2GRAY);
        //Core.add(oriMat,oriMat,oriMat);

        Bitmap allFaceBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(oriMat,allFaceBitmap);


        Bitmap faceAreaBitmap = getFaceAreaImage();
        Bitmap originalImage = getOriginalImage();

        int width = getOriginalImage().getWidth();
        int height = getOriginalImage().getHeight();
        int [] faceAreaPixels = new int[width * height];
        int [] originalPixels = new int[width * height];
        int [] allFacePixels = new int[width * height];
        int [] operatePixels = new int[width * height];

        faceAreaBitmap.getPixels(faceAreaPixels,0,width,0,0,width,height);
        originalImage.getPixels(originalPixels,0,width,0,0,width,height);
        allFaceBitmap.getPixels(allFacePixels,0,width,0,0,width,height);

        for (int i = 0; i < faceAreaPixels.length; i++) {
            int oriPixel = faceAreaPixels[i];
            operatePixels[i]= Color.alpha(oriPixel)==0?originalPixels[i]:allFacePixels[i];
        }
        allFaceBitmap.setPixels(operatePixels,0,width, 0, 0, width, height);


//        Bitmap allFaceBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(operateMat,allFaceBitmap);


        filterInfoResult.setFilterBitmap(allFaceBitmap);
        filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

        return filterInfoResult;
    }


    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FACE_LEFT_RIGHT_AREA,FACE_FOREHEAD};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

}