package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceWrinkle5 extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap cacheBitmap =
//                        getFacePartImage();
                        originalImage.copy(Bitmap.Config.ARGB_8888,true);

                Mat oriMat = new Mat();
                Utils.bitmapToMat(cacheBitmap,oriMat);

//                Imgproc.cvtColor(oriMat,oriMat,COLOR_RGB2GRAY);

                int kernelSize = 7;
                double sigma = 2 * Math.PI;
                double lambd = 5;//Math.PI / 2;
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

//                Core.add(mats[0],mats[0],mats[0]);
                Core.add(mats[0], mats[1], mats[0]);
                Core.add(mats[2], mats[3], mats[2]);
                Core.add(mats[0], mats[2], mats[0]);

//                Mat dst = new Mat();
//                Core.convertScaleAbs(mats[0], dst, 0.2, 0);//0.2
//                Core.normalize(mats[0],mats[0],0,255,Core.NORM_MINMAX);
//                Core.convertScaleAbs(mats[0], dst);
//                Imgproc.cvtColor(mats[0],mats[0],COLOR_RGB2GRAY);
                //Imgproc.threshold(mats[0],mats[0],50,255,Imgproc.THRESH_BINARY);
//                Imgproc.adaptiveThreshold();
                Mat hsvMat = new Mat();

                Imgproc.cvtColor(mats[0],hsvMat,COLOR_RGB2HSV);

                Mat heightMat = new Mat();
                Mat lowMat = new Mat();
                Core.inRange(hsvMat,new Scalar(0,0,221),new Scalar(180,30,255),heightMat);
                Core.inRange(hsvMat,new Scalar(0,0,0),new Scalar(180,255,46),lowMat);

                Mat heightDstMat = new Mat();
                Mat heightSrcMat = new Mat(oriMat.rows(),oriMat.cols(),CvType.CV_8UC4,new Scalar(239,250,178,255));

                Core.bitwise_and(heightSrcMat,heightSrcMat,heightDstMat,heightMat);

                Mat lowDstMat = new Mat();
                Mat lowSrcMat = new Mat(oriMat.rows(),oriMat.cols(),CvType.CV_8UC4,new Scalar(67,149,174,255));

                Core.bitwise_and(lowSrcMat,lowSrcMat,lowDstMat,lowMat);


                LogUtils.e(heightDstMat.toString(),lowDstMat.toString());
                Mat mixMat = new Mat();
                Core.add(lowDstMat,heightDstMat,mixMat);

                //opencv mask
                Bitmap maskBitmap = Bitmap.createBitmap(cacheBitmap.getWidth(),cacheBitmap.getHeight(),cacheBitmap.getConfig());
                Utils.matToBitmap(mixMat,maskBitmap);

                //face mask
                Bitmap faceMaskBitmap = getFaceAreaImage();

                int width = cacheBitmap.getWidth();
                int height = cacheBitmap.getHeight();
                int count = width * height;
                int [] maskPixels = new int[count];
                int [] faceMaskPixels = new int[count];
                int [] oriPixels = new int[count];
                maskBitmap.getPixels(maskPixels,0,width,0,0,width,height);
                faceMaskBitmap.getPixels(faceMaskPixels,0,width,0,0,width,height);
                cacheBitmap.getPixels(oriPixels,0,width,0,0,width,height);

                for (int i = 0; i < oriPixels.length; i++) {
                    int alpha = Color.alpha(faceMaskPixels[i]);
                    if (alpha==0) continue;
//                    if (maskPixels[i] != Color.BLACK){
//                        oriPixels[i] = maskPixels[i];
//                        LogUtils.e(Color.red(oriPixels[i]),Color.green(oriPixels[i]),Color.blue(oriPixels[i]));
//                    }
                    if (Color.red(maskPixels[i])==0 && Color.green(maskPixels[i])==0 &&Color.blue(maskPixels[i])==0)continue;
                    oriPixels[i] = maskPixels[i];
                }

                cacheBitmap.setPixels(oriPixels,0,width, 0, 0, width, height);

//                Core.add(oriMat,mixMat,mixMat);
//                Core.bitwise_and(heightSrcMat,lowSrcMat,mixMat);

//                Imgproc.threshold(hsvMat,hsvMat,0,255, THRESH_BINARY_INV);
//                Core.bitwise_not(hsvMat,hsvMat);
//                Core.bitwise_not(hsvMat,hsvMat);
//                Core.addWeighted(heightSrcMat,0.5d,lowSrcMat);


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