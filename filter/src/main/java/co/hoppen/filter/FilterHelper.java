package co.hoppen.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;

import org.opencv.android.OpenCVLoader;

import java.lang.reflect.Constructor;
import java.util.List;

import co.hoppen.filter.filter.FaceFilter;
import co.hoppen.filter.filter.Filter;

/**
 * Created by YangJianHui on 2021/9/11.
 */
public class FilterHelper {
    private MLFaceAnalyzer analyzer;

    public FilterHelper(){
        //每次创建 清除face缓存
        SPUtils.getInstance().remove("face");
        if (!OpenCVLoader.initDebug()) {
            LogUtils.e("Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            LogUtils.e("OpenCV library found inside package. Using it!");
        }
    }


    /**
     * 异步
     * @param type 检测类型
     * @param bitmap 检测原图，
     * @param resistance 电阻值
     * @param dstFilterPath 输出滤镜地址
     * @param onFilterListener 滤镜回调
     * @throws Exception
     */
    public void execute(FilterType type, Bitmap bitmap , float resistance,String dstFilterPath,OnFilterListener onFilterListener)throws Exception{
        try {
//            LogUtils.e(type);
            executeResult(createFilter(type, bitmap, resistance,dstFilterPath),onFilterListener);
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    //异步执行算法
    private void executeResult(Filter filter,OnFilterListener onFilterListener){
        ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<FilterInfoResult>() {
            @Override
            public FilterInfoResult doInBackground() throws Throwable {
                FilterInfoResult filterInfoResult = null;
                if (filter instanceof FaceFilter){
                    //人脸算法
                    boolean finish = ((FaceFilter) filter).faceAreaPositioning(createAnalyzer());
                    if (finish)filterInfoResult =  filter.onFilter();
                }else {
                    //局部算法
                    filterInfoResult = filter.onFilter();
                }
                filter.recycle();
                return filterInfoResult;
            }

            @Override
            public void onSuccess(FilterInfoResult result) {
                if (result!=null){
                    onFilterListener.onFilter(result);
                }
            }
        });
    }

    /**
     *  反射生成类型对象
     * @param type 检测类型
     * @param bitmap 检测原图
     * @param resistance 电阻值
     * @param dstBitmapPath 输出滤镜地址
     * @param <F>
     * @return
     * @throws Exception
     */
    private <F extends Filter> F createFilter(FilterType type, Bitmap bitmap , float resistance,String dstBitmapPath)throws Exception{
        Class<? extends Filter> filterClass = type.getType();
        F f = null;
        if (filterClass!=null){
            Constructor<? extends Filter> declaredConstructor = filterClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            f = (F) declaredConstructor.newInstance();
            //处理完原图 会 recycle
            Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            f.setOriginalImage(copy)
                    .setResistance(resistance)
                    .createFilterResult(type,dstBitmapPath);
        }
        return f;
    }

    /**
     *  生成MLkit套件
     * @return
     */
    private MLFaceAnalyzer createAnalyzer(){
        if (analyzer==null){
            analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(new MLFaceAnalyzerSetting.Factory()
                    // 设置是否检测人脸关键点。
                    .setKeyPointType(MLFaceAnalyzerSetting.TYPE_KEYPOINTS)
                    // 设置是否检测人脸特征和表情。
                    .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                    // 设置仅启用人脸表情检测和性别检测。
                    .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURE_EMOTION | MLFaceAnalyzerSetting.TYPE_FEATURE_GENDAR)
                    // 设置是否检测人脸轮廓点。
                    .setShapeType(MLFaceAnalyzerSetting.TYPE_SHAPES)
                    // 设置是否开启人脸追踪并指定快捷追踪模式。
                    .setTracingAllowed(false, MLFaceAnalyzerSetting.MODE_TRACING_FAST)
                    // 设置检测器速度/精度模式。
                    .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
                    // 设置是否开启Pose检测（默认开启）。
                    .setPoseDisabled(false)
                    .create());
        }
        return analyzer;
    }

    /**
     * 单独检测人脸
     * @param bitmap
     * @param onDetectFaceListener
     */
    public void detectFace(Bitmap bitmap,OnDetectFaceListener onDetectFaceListener){
        try {
            MLFaceAnalyzer analyzer = createAnalyzer();
            MLFrame frame = MLFrame.fromBitmap(bitmap);
            analyzer.asyncAnalyseFrame(frame).addOnSuccessListener(new OnSuccessListener<List<MLFace>>() {
                @Override
                public void onSuccess(List<MLFace> mlFaces) {
                    if (onDetectFaceListener!=null && mlFaces.size()==1){
                        onDetectFaceListener.onDetectSuccess();
                    }else {
                        if (onDetectFaceListener!=null)onDetectFaceListener.onDetectFaceFailure();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (onDetectFaceListener!=null)onDetectFaceListener.onDetectFaceFailure();
                }
            });
        } catch (Exception e) {
            if (onDetectFaceListener!=null)onDetectFaceListener.onDetectFaceFailure();
            e.printStackTrace();
        }
    }

    /**
     * 通过rgb光检测人脸，保存人脸皮肤面积
     * @param bitmap
     * @param onDetectFaceListener
     */
    public void detectFaceByRgbLight(Bitmap bitmap,OnDetectFaceListener onDetectFaceListener){
        detectFace(bitmap, new OnDetectFaceListener() {
            @Override
            public void onDetectSuccess() {

                if (onDetectFaceListener!=null)onDetectFaceListener.onDetectSuccess();
            }

            @Override
            public void onDetectFaceFailure() {
                if (onDetectFaceListener!=null)onDetectFaceListener.onDetectFaceFailure();
            }
        });
    }

}
