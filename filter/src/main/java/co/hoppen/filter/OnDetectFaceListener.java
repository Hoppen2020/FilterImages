package co.hoppen.filter;

import android.graphics.PointF;

import java.util.List;

/**
 * Created by YangJianHui on 2022/8/31.
 */
public interface OnDetectFaceListener {
   void onDetectSuccess(List<PointF> partPoint);
   void onDetectFaceFailure();
}
