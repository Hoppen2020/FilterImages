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

import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle6 extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap cacheBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);

                Mat oriMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,oriMat);

//                Mat grayMat = new Mat();
//                Imgproc.cvtColor(oriMat,grayMat,Imgproc.COLOR_RGBA2GRAY);

//                int width = grayMat.width();
//                int height = grayMat.height();
//
//                Mat outImage = new Mat(width,height,CvType.CV_8UC1,new Scalar(0));
//
//                int w = 5;
//                double sigma = 0.1;
//                Mat xxGauKernel = new Mat(2 * w + 1 ,2 * w + 1,CvType.CV_32FC1,new Scalar(0));
//                Mat xyGauKernel = new Mat(2 * w + 1 ,2 * w + 1,CvType.CV_32FC1,new Scalar(0));
//                Mat yyGauKernel = new Mat(2 * w + 1 ,2 * w + 1 ,CvType.CV_32FC1,new Scalar(0));
//
//                //构建高斯二阶偏导数模板
//                for (int i = -w; i <= w;i++) {
//                    for (int j = -w; j <= w; j++) {
//                        double part =  exp(-1 * (i * i + j * j) / (2 * sigma * sigma));
//                        double xx = ((1 - (i * i) / (sigma * sigma)) * part * (-1 / (2 * PI * pow(sigma, 4))));
//                        double yy =  ((1 - (j * j) / (sigma * sigma)) * part * (-1 / (2 * PI * pow(sigma, 4))));
//                        double xy =  (((i * j)) * part * (1 / (2 * PI * pow(sigma, 6))));
//                        double[] doubles1 = xxGauKernel.get(i + w, j + w);
//                        double[] doubles2 = xyGauKernel.get(i + w, j + w);
//                        double[] doubles3 = yyGauKernel.get(i + w, j + w);
//                        doubles1[0] = xx;
//                        doubles2[0] = yy;
//                        doubles3[0] = xy;
//                        xxGauKernel.put(i + w, j + w,doubles1);
//                        xyGauKernel.put(i + w, j + w,doubles2);
//                        yyGauKernel.put(i + w, j + w,doubles3);
//                    }
//                }
//
//
//                Mat xxDerivae = new Mat(width,height,CvType.CV_32FC1,new Scalar(0));
//                Mat yyDerivae = new Mat(width,height,CvType.CV_32FC1,new Scalar(0));
//                Mat xyDerivae = new Mat(width,height,CvType.CV_32FC1,new Scalar(0));
//
//                Imgproc.filter2D(oriMat,xxDerivae,xxDerivae.depth(),xxGauKernel);
//                Imgproc.filter2D(oriMat,yyDerivae,yyDerivae.depth(),yyGauKernel);
//                Imgproc.filter2D(oriMat,xyDerivae,xyDerivae.depth(),xyGauKernel);

//                for (int i = 0; i < height; i++) {
//                    for (int j = 0; j < width; j++) {
////                        Mat arrayMat = new Mat(2,2,CvType.CV_32FC1,)
//                        double[] doubles = xxDerivae.get(i, j);
//                        double array[][] = {{},{}};
//                        Mat amat = new Mat();
//                        amat.put(0,0,array);
//                    }
//                }

                Photo.detailEnhance(oriMat,oriMat,10,0.9f);
                //oriMat.convertTo(oriMat,CvType.CV_32F);

                int kernelSize = 9;
                double sigma = 1.0;
                double lambd = Math.PI / 2;//Math.PI / 2;
                double gamma = 0.5;//0.5
                double psi = 0;
                double [] theta = {
                        0,Math.PI/4,Math.PI/2, Math.PI - Math.PI/4
                };
                Mat [] mats = {new Mat(),new Mat(),new Mat(),new Mat()};

                for (int i = 0; i < theta.length; i++) {
                    Mat gaborKernel = Imgproc.getGaborKernel(new Size(kernelSize, kernelSize), sigma, theta[i], lambd, gamma, psi, CvType.CV_32F);
                    Imgproc.filter2D(oriMat,mats[i],-1,gaborKernel);
                }


                Core.add(mats[0], mats[1], mats[0]);
                Core.add(mats[2], mats[3], mats[2]);
                Core.add(mats[0], mats[2], mats[0]);

                Mat dst = new Mat();
                Core.convertScaleAbs(mats[0],dst,1,0);
                Mat binary = new Mat();
                Mat gray = new Mat();
//
                Imgproc.cvtColor(dst,gray,COLOR_RGB2GRAY);
//
                Imgproc.threshold(gray,binary,0,255,THRESH_OTSU);
                //Imgproc.adaptiveThreshold(gray,gray,255,ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,21,5);

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
        return new FacePart[]{FacePart.FACE_LEFT_RIGHT_AREA};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }
}