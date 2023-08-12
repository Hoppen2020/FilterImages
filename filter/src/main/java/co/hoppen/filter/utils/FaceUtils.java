package co.hoppen.filter.utils;

import android.graphics.PointF;

import com.blankj.utilcode.util.LogUtils;
import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangJianHui on 2023/8/11.
 */
public class FaceUtils {
   public static List<PointF> faceToPartPoint(MLFace mlFace){
      List<MLPosition> allPoints = mlFace.getAllPoints();
      List<PointF> pointFList = new ArrayList<>();

      float foreheadX = (allPoints.get(234).getX() + allPoints.get(164).getX()) / 2;
      float foreheadY = allPoints.get(166).getY();
      pointFList.add(new PointF(foreheadX,foreheadY));

      float leftFaceX = (allPoints.get(53).getX() + allPoints.get(290).getX()) / 2 ;
      float leftFaceY = (allPoints.get(853).getY() + allPoints.get(845).getY()) / 2 ;
      pointFList.add(new PointF(leftFaceX,leftFaceY));

      float rightFaceX = (allPoints.get(357).getX() + allPoints.get(117).getX()) / 2;
      float rightFaceY = (allPoints.get(854).getY() + allPoints.get(848).getY()) / 2;
      pointFList.add(new PointF(rightFaceX,rightFaceY));

      float noseX = allPoints.get(440).getX();
      float noseY = allPoints.get(440).getY();
      pointFList.add(new PointF(noseX,noseY));

      float chinX = allPoints.get(0).getX();
      float chinY = allPoints.get(78).getY();
      pointFList.add(new PointF(chinX,chinY));

      return pointFList;
   }

   public static List<PointF> faceToPartViewPoint(MLFace mlFace,int srcWidth,int srcHeight,int dstWidth,int dstHeight){
      List<PointF> pointFList = faceToPartPoint(mlFace);
      float ratioW = srcWidth /dstWidth;
      float ratioH = srcHeight /dstHeight;
      for (int i = 0; i < pointFList.size(); i++) {
         PointF pointF = pointFList.get(i);
         pointFList.set(i,new PointF(pointF.x * ratioW,pointF.y * ratioH));
      }
      return pointFList;
   }

}
