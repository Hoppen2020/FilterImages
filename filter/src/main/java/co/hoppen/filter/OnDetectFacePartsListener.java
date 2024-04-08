package co.hoppen.filter;

import android.graphics.PointF;

import java.util.List;
import java.util.Map;

/**
 * Created by YangJianHui on 2024/4/7.
 */
public interface OnDetectFacePartsListener {

   void onDetectSuccess(Map<DetectFaceParts,List<PointF>> detectMap);
   void onDetectFaceFailure();
}
