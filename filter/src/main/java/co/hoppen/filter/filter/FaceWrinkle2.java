package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

import static co.hoppen.filter.FacePart.FACE_FOREHEAD;
import static co.hoppen.filter.FacePart.FACE_LEFT_RIGHT_AREA;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.LSD_REFINE_ADV;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle2 extends FaceFilter {
    /**
     *
     *
     */
    @Override
    public void onFilter(FilterInfoResult filterInfoResult) {
        Mat operateMat = new Mat();
        Utils.bitmapToMat(getOriginalImage(),operateMat);

        Imgproc.cvtColor(operateMat,operateMat,COLOR_RGB2GRAY);

        Mat resultImage = new Mat(operateMat.size(), CvType.CV_32F);

        int kernelSize = 15;//31 15
        double sigma = 2.0;//4.0
        double lambda = 5.0;//5.0
        double gamma = 0.5;//0.5
        double psi = 0.0;//0.0

        List<Mat> cache = new ArrayList<>();

        Mat cacheF = null;

        double thetaStep = 360.0 / 16;


        // 定义16个不同方向的Gabor滤波器并应用于图像
        for (int theta = 0; theta < 16; theta++) {
            //double angle = theta * (Math.PI / 16.0); // 计算角度
            double angle = theta * thetaStep;

            Mat gaborKernel = Imgproc.getGaborKernel(new Size(kernelSize, kernelSize), sigma, angle, lambda, gamma, psi, CvType.CV_32F);

            Mat filteredImage = new Mat(operateMat.size(), CvType.CV_32F);
            Imgproc.filter2D(operateMat, filteredImage, operateMat.depth(), gaborKernel);

            if (cacheF==null){
                cacheF = filteredImage;
            }else {
                Core.max(cacheF,filteredImage,cacheF);
            }
            cache.add(cacheF.clone());

            Core.normalize(filteredImage, filteredImage, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

            Bitmap bitmap = getOriginalImage().copy(Bitmap.Config.ARGB_8888,true);

            Utils.matToBitmap(filteredImage,bitmap);

            ImageUtils.save(bitmap,Environment.getExternalStorageDirectory().getPath() + "/test/"+theta+".png", Bitmap.CompressFormat.PNG);

        }

        // 归一化并保存结果图像
        Core.normalize(cacheF, cacheF, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

        Bitmap bitmap = getOriginalImage().copy(Bitmap.Config.ARGB_8888,true);

        Utils.matToBitmap(cacheF,bitmap);


        filterInfoResult.setFilterBitmap(bitmap);
        filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

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