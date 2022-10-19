package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

import static co.hoppen.filter.FacePart.FACE_EYE_BOTTOM;
import static co.hoppen.filter.FacePart.FACE_FOREHEAD;
import static co.hoppen.filter.FacePart.FACE_LEFT_RIGHT_AREA;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle7 extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap cacheBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
                Mat oriMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,oriMat);
                Photo.detailEnhance(oriMat,oriMat,10,0.9f);
                Imgproc.cvtColor(oriMat,oriMat,COLOR_RGBA2GRAY);

                int kernelSize = 9;
                double sigma = 1.0;
                double lambd = Math.PI / 2;//Math.PI / 2;
                double gamma = 0.5;//0.5
                double psi = Math.PI  * 0.8;
                double [] theta = {
                        0,Math.PI * 0.25d,Math.PI * 0.5d, Math.PI * 0.75d
//                        0,45d,90d,135d
                };
                Mat [] mats = {new Mat(),new Mat(),new Mat(),new Mat()};

                for (int i = 0; i < theta.length; i++) {
                    Mat gaborKernel = Imgproc.getGaborKernel(new Size(kernelSize, kernelSize), sigma, theta[i], lambd, gamma, psi, CvType.CV_32F);
                    Imgproc.filter2D(oriMat,mats[i],-1,gaborKernel);
                }

                Core.add(mats[0], mats[1], mats[0]);
                Core.add(mats[2], mats[3], mats[2]);
                Core.add(mats[0], mats[2], mats[0]);
//
                Mat dst = new Mat();
                Core.convertScaleAbs(mats[0],dst,1,0);


                Imgproc.threshold(dst,dst,0,255,THRESH_BINARY_INV|THRESH_OTSU);


                Utils.matToBitmap(dst,cacheBitmap);
                filterInfoResult.setFilterBitmap(cacheBitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        } catch (Exception e) {
            LogUtils.e(e.toString());
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }


    @Override
    public FacePart[] getFacePart() {
        return new FacePart[]{FACE_LEFT_RIGHT_AREA,FACE_FOREHEAD,FACE_EYE_BOTTOM};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

}