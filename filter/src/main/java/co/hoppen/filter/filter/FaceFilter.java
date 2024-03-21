package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.SparseArray;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.hoppen.filter.FaceAreaInfo;
import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterCacheConfig;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.utils.CutoutUtils;

import static com.huawei.hms.mlsdk.face.MLFaceShape.TYPE_BOTTOM_OF_LOWER_LIP;
import static com.huawei.hms.mlsdk.face.MLFaceShape.TYPE_BOTTOM_OF_UPPER_LIP;
import static com.huawei.hms.mlsdk.face.MLFaceShape.TYPE_FACE;
import static com.huawei.hms.mlsdk.face.MLFaceShape.TYPE_TOP_OF_UPPER_LIP;

/**
 * Created by YangJianHui on 2022/9/16.
 */
public abstract class FaceFilter extends Filter{

    public abstract FacePart[] getFacePart();

    public abstract FilterDataType getFilterDataType();

    private List<PointF> mouthPoints;

    //人脸定位部分关键点
    private List<MLPosition> facePoints = null;
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
            //LogUtils.e(mlFaceSparseArray.size());
            if (mlFaceSparseArray.size()>0){
                faceResult = mlFaceSparseArray.get(0);
                //记录定位数据
                String face = GsonUtils.toJson(faceResult);
                SPUtils.getInstance().put(FilterCacheConfig.CACHE_FACE,face);
            }else {
                //因uv、伍氏光，无法识别人脸，保底使用上次缓存
                faceResult = GsonUtils.fromJson(
                        SPUtils.getInstance()
                                .getString(FilterCacheConfig.CACHE_FACE),GsonUtils.getType(MLFace.class));
            }
            analyzer.stop();
            if (faceResult!=null){
                mouthPoints = new ArrayList<>();
                mouthPoints.addAll(Arrays.asList(faceResult.getFaceShape(TYPE_TOP_OF_UPPER_LIP).getCoordinatePoints()));
                mouthPoints.addAll(Arrays.asList(faceResult.getFaceShape(TYPE_BOTTOM_OF_LOWER_LIP).getCoordinatePoints()));
                facePoints = faceResult.getFaceShape(TYPE_FACE).getPoints();
                faceAreaImage = CutoutUtils.cutoutPart(oriBitmap,getFacePart(),faceResult);
                return true;
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public Bitmap getMouthAreaImage(){
        if (mouthPoints!=null){
            return CutoutUtils.cutoutMouth(getOriginalImage(),mouthPoints);
        }else return null;
    }

    public Bitmap getFaceAreaImage() {
        return faceAreaImage;
    }

    public Mat getFilterFaceMask(Mat operateMat){
        Mat faceMask = getFaceMask();
//        LogUtils.e(faceMask==null);
        if (faceMask!=null){
            Mat frontDst = new Mat(); //frontAndBack
            operateMat.copyTo(frontDst,faceMask);
//            Core.bitwise_not(faceMask,faceMask);
//            Mat backgroundMat = new Mat();
//            Utils.bitmapToMat(getOriginalImage(),backgroundMat);
//            Imgproc.cvtColor(backgroundMat,backgroundMat,Imgproc.COLOR_RGBA2RGB);
//            Mat backDst = new Mat(); //frontAndBack
//            backgroundMat.copyTo(backDst,faceMask);
//            Mat dstMat = new Mat();
//            Core.add(frontDst,backDst,dstMat);
            return frontDst;
        }
        return operateMat;
    }

    public Mat getFaceMask(){
        try {
            if (facePoints!=null){
                Bitmap drawBitmap = Bitmap.createBitmap(getOriginalImage().getWidth(),getOriginalImage().getHeight(),getOriginalImage().getConfig());
                Canvas canvas = new Canvas(drawBitmap);
                Path path = new Path();
                for (int i = 0; i < facePoints.size(); i++) {
                    MLPosition mlPosition = facePoints.get(i);
                    if (i==0){
                        path.moveTo(mlPosition.getX(),mlPosition.getY());
                    }else {
                        path.lineTo(mlPosition.getX(),mlPosition.getY());
                    }
                }
                path.close();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                canvas.drawPath(path,paint);
                Mat mat = new Mat();
                Utils.bitmapToMat(drawBitmap,mat);
                List<Mat> rgbList = new ArrayList<>();
                Core.split(mat,rgbList);
                rgbList.get(1).release();
                rgbList.get(2).release();
                if (!drawBitmap.isRecycled())drawBitmap.recycle();
                return rgbList.get(0);
            }
        }catch (Exception e){
            LogUtils.e(e.toString());
        }
        return null;
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

    public int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
