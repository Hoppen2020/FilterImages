package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import co.hoppen.filter.FaceAreaInfo;
import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.utils.CutoutUtils;

/**
 * Created by YangJianHui on 2022/9/16.
 */
public abstract class FaceFilter extends Filter{

    public abstract FacePart[] getFacePart();

    public abstract FilterDataType getFilterDataType();
    //人脸定位 区域图
    private Bitmap faceAreaImage;
    /**
     *  人脸定位
     * @param analyzer 分析器引擎
     * @return
     */
    public boolean faceAreaPositioning(MLFaceAnalyzer analyzer){
        try {
            Bitmap oriBitmap = getOriginalImage();
            MLFace faceResult = null;
            MLFrame frame = MLFrame.fromBitmap(oriBitmap);
            SparseArray<MLFace> mlFaceSparseArray = analyzer.analyseFrame(frame);
            if (mlFaceSparseArray.size()>0){
                faceResult = mlFaceSparseArray.get(0);
                //记录上一次定位数据
                String face = GsonUtils.toJson(faceResult);
                SPUtils.getInstance().put("face",face);
            }else {
                faceResult = GsonUtils.fromJson(SPUtils.getInstance().getString("face"),GsonUtils.getType(MLFace.class));
            }
            analyzer.stop();
            if (faceResult!=null){
                faceAreaImage = CutoutUtils.cutoutPart(oriBitmap,getFacePart(),faceResult);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Bitmap getFaceAreaImage() {
        return faceAreaImage;
    }

    //资源回收
    @Override
    public void recycle() {
        super.recycle();
        if (faceAreaImage!=null&&!faceAreaImage.isRecycled()){
            faceAreaImage.recycle();
            faceAreaImage = null;
        }
    }


    public FaceAreaInfo createFaceAreaInfoByPoints(MatOfPoint contour,int width,int height){
        List<List<android.graphics.Point>> createPointList = new ArrayList<>();

            MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
            contour.convertTo(matOfPoint2f, CvType.CV_32F);
            //LogUtils.e("size " + Imgproc.minAreaRect(matOfPoint2f).size.area());
            double size = Imgproc.minAreaRect(matOfPoint2f).size.area();
            //LogUtils.e(size,(size<4000 * 5 ||size>=(width * height / 2)));
            //if (size<4000 * 5 ||size>=(width * height / 2))continue;

            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            //Path path = new Path();
            List<android.graphics.Point> pointList = new ArrayList<>();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
                Point hullPoint = hullPoints[i];
                float x = (float) hullPoint.x;
                float y = (float) hullPoint.y;
                pointList.add(new android.graphics.Point((int)x,(int)y));
            }
            createPointList.add(pointList);
        return FaceAreaInfo.createFaceAreaInfo(createPointList,width,height);
    }

    public FaceAreaInfo createFaceAreaInfo(Mat mat){
        return createFaceAreaInfo(mat,29);
    }
    /**
     *  通用转换区域
     * @param mat
     * @param kSize
     * @return
     */
    public FaceAreaInfo createFaceAreaInfo(Mat mat, int kSize){

        Imgproc.dilate(mat,mat,Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(kSize,kSize)));

        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGBA2GRAY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<List<android.graphics.Point>> createPointList = new ArrayList<>();

        int width = mat.cols();
        int height = mat.rows();

        for (MatOfPoint contour : contours) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
            contour.convertTo(matOfPoint2f, CvType.CV_32F);
            //LogUtils.e("size " + Imgproc.minAreaRect(matOfPoint2f).size.area());
            double size = Imgproc.minAreaRect(matOfPoint2f).size.area();
            //LogUtils.e(size,(size<4000 * 5 ||size>=(width * height / 2)));
            if (size<4000 * 5)continue;

            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            //Path path = new Path();
            List<android.graphics.Point> pointList = new ArrayList<>();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
                Point hullPoint = hullPoints[i];
                float x = (float) hullPoint.x;
                float y = (float) hullPoint.y;
                pointList.add(new android.graphics.Point((int)x,(int)y));
            }
            createPointList.add(pointList);
        }
        mat.release();
        hierarchy.release();
        //LogUtils.e(createPointList.size(),width,height);
        return FaceAreaInfo.createFaceAreaInfo(createPointList,width,height);
    }

}
