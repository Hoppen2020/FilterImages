package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/10/18.
 */
public class SkinTest extends Filter{
    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
            Bitmap originalImage = getOriginalImage();

            Mat src = new Mat();
            Utils.bitmapToMat(originalImage,src);
            Imgproc.cvtColor(src,src,Imgproc.COLOR_RGBA2RGB);

            //Mat dst = Mat.zeros(src.size(), CvType.CV_8UC1);

            int width = src.cols();
            int height = src.rows();
            int dims = src.channels();

            int clusterCount = 4;

            Mat points = new Mat(width * height, dims, CvType.CV_32F, new Scalar(0));
            Mat centers = new Mat(clusterCount, dims, CvType.CV_32F);
            Mat labels = new Mat(width * height, 1, CvType.CV_32S);

            //    points
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int index = row * width + col;
                    double[] s_data = src.get(row, col);

                    for (int channel = 0; channel < 3; channel++) {
                        float[] f_buff = new float[1];
                        f_buff[0] = (float) s_data[channel];

                        points.put(index, channel, f_buff);
                    }
                }
            }

            //  knn ?
            TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 0.1);
            Core.kmeans(points, clusterCount, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers);

            //  ??? label index
            Map<Integer, Integer> tmp = new TreeMap<>();
            for (int i = 0; i < clusterCount; i++) {
                int sum = 0;
                for (int j = 0; j < dims; j++) {
                    sum += centers.get(i, j)[0];
                }
                while (tmp.containsKey(sum))
                    sum++;
                tmp.put(sum, i);
            }

            int count = 0;
            int[] label_order = new int[clusterCount];
            List<Mat> masks = new ArrayList<>();
            for (Map.Entry<Integer, Integer> iter : tmp.entrySet()) {
                label_order[count++] = iter.getValue();
//                LogUtils.e(iter.getValue());
                masks.add(Mat.zeros(src.size(), CvType.CV_8UC1));
            }

            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int index = row * width + col;
                    int label = (int) labels.get(index, 0)[0];
                    //LogUtils.e("label:"+label);
                    byte[] d_buff = new byte[1];
                    d_buff[0] = (byte) 255;
                    Mat dst = null;
                    if (label == label_order[0]) {
                        dst = masks.get(0);
                    }else if (label == label_order[1]){
                        dst = masks.get(1);
                    }else if (label == label_order[2]){
                        dst = masks.get(2);
                    }else if (label == label_order[3]){
                        dst = masks.get(3);
                    }
                    if (dst!=null)dst.put(row, col, d_buff);
                }
            }

            Iterator<Mat> iterator = masks.iterator();
            Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
            while (iterator.hasNext()){
                Mat next = iterator.next();
                Imgproc.erode(next,next,structuringElement);
                Imgproc.dilate(next,next,structuringElement);
            }

            draw(src,masks.get(0),new Scalar(255,0,0));

            draw(src,masks.get(1),new Scalar(0,0,255));

            Bitmap bitmap = Bitmap.createBitmap(originalImage.getWidth(),originalImage.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(src,bitmap);
            filterInfoResult.setFilterBitmap(bitmap);

        return filterInfoResult;
    }

    private void draw(Mat src,Mat mask,Scalar scalar){

        List<MatOfPoint> list = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask,list,hierarchy, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);

        for (int i = 0; i < list.size(); i++) {
//            MatOfPoint point = list.get(i);
            Imgproc.drawContours(src,list,i,scalar);
        }


    }

}
