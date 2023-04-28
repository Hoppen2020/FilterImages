package co.hoppen.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.GsonUtils;
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
import com.huawei.hms.mlsdk.faceverify.MLFaceTemplateResult;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationAnalyzer;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationAnalyzerFactory;

import org.opencv.android.OpenCVLoader;

import java.lang.reflect.Constructor;
import java.util.List;

import co.hoppen.filter.filter.FaceFilter;
import co.hoppen.filter.filter.Filter;
import co.hoppen.filter.utils.FaceSkinUtils;

/**
 * Created by YangJianHui on 2021/9/11.
 */
public class FilterHelper {
    private MLFaceAnalyzer analyzer;

    public FilterHelper(){
        //每次创建 清除face缓存
        clearFaceCache();
        FaceSkinUtils.clearSkinArea();
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
            executeResult(createFilter(type, bitmap, resistance),new FilterInfoResult(type,dstFilterPath,resistance),onFilterListener);
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     *异步执行算法
     * @param filter
     * @param filterInfoResult
     * @param onFilterListener
     */
    private void executeResult(Filter filter,FilterInfoResult filterInfoResult,OnFilterListener onFilterListener){
        ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<FilterInfoResult>() {
            @Override
            public FilterInfoResult doInBackground() throws Throwable {
                if (filter instanceof FaceFilter){ //人脸算法
                    //人脸区域定位
                    boolean finish = ((FaceFilter) filter).faceAreaPositioning(createAnalyzer());
                    LogUtils.e(finish);
                    if (finish)filter.onFilter(filterInfoResult);
                }else { //局部算法
                    filter.onFilter(filterInfoResult);
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
     * @param <F>
     * @return
     * @throws Exception
     */
    private <F extends Filter> F createFilter(FilterType type, Bitmap bitmap , float resistance)throws Exception{
        Class<? extends Filter> filterClass = type.getType();
        F f = null;
        if (filterClass!=null){
            Constructor<? extends Filter> declaredConstructor = filterClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            f = (F) declaredConstructor.newInstance();
            //处理完原图 会 recycle
            f.setResistance(resistance).setOriginalImage(bitmap.copy(Bitmap.Config.ARGB_8888, true));
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
     * 确保数据准确性，正常流程 1、拍照 2、检查是否符合人脸 3、保存人脸数据
     * @param bitmap
     * @param onDetectFaceListener
     */
    public void detectFace(Bitmap bitmap,boolean rgbLight,OnDetectFaceListener onDetectFaceListener){
        try {
            MLFaceAnalyzer analyzer = createAnalyzer();
            MLFrame frame = MLFrame.fromBitmap(bitmap);
            analyzer.asyncAnalyseFrame(frame).addOnSuccessListener(new OnSuccessListener<List<MLFace>>() {
                @Override
                public void onSuccess(List<MLFace> mlFaces) {
                    if (onDetectFaceListener!=null && mlFaces.size()==1){
                        if (rgbLight){
                            FaceSkinUtils.saveFaceSkinArea(bitmap);
                            String face = GsonUtils.toJson(mlFaces.get(0));
                            SPUtils.getInstance().put(FilterCacheConfig.CACHE_FACE,face);
                        }
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

//            MLFaceVerificationAnalyzer verificationAnalyzer = MLFaceVerificationAnalyzerFactory.getInstance().getFaceVerificationAnalyzer();
//            List<MLFaceTemplateResult> mlFaceTemplateResults = verificationAnalyzer.setTemplateFace(frame);
//            for (int i = 0; i < mlFaceTemplateResults.size(); i++) {
//                MLFaceTemplateResult mlFaceTemplateResult = mlFaceTemplateResults.get(i);
//                LogUtils.e(mlFaceTemplateResult.getFaceInfo(),mlFaceTemplateResult.getTemplateId());
//            }

        } catch (Exception e) {
            if (onDetectFaceListener!=null)onDetectFaceListener.onDetectFaceFailure();
            e.printStackTrace();
        }
    }

    public void clearFaceCache(){
        SPUtils.getInstance().remove(FilterCacheConfig.CACHE_FACE);
    }

}
