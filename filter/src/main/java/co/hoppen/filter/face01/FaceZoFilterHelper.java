package co.hoppen.filter.face01;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;

import org.opencv.android.OpenCVLoader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.hoppen.filter.DetectFaceParts;
import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.FilterType;
import co.hoppen.filter.OnFilterListener;
import co.hoppen.filter.filter.FaceFilter;
import co.hoppen.filter.filter.Filter;
import co.hoppen.filter.filter.IFaceFilter;

/**
 * Created by YangJianHui on 2024/4/8.
 */
public class FaceZoFilterHelper {

   /**
    * 初始化opencv
    */
   public FaceZoFilterHelper(){
      if (!OpenCVLoader.initDebug()) {
         LogUtils.e("Internal OpenCV library not found. Using OpenCV Manager for initialization");
      } else {
         LogUtils.e("OpenCV library found inside package. Using it!");
      }
   }

   public void execute(FaceZoFilterType type,
                       Bitmap bitmap,
                       float resistance,
                       Map<DetectFaceParts, List<PointF>> detectMap,
                       String saveFilterPath,
                       OnFaceZoFilterListener onFaceZoFilterListener){
      try {
         executeResult(type,bitmap,resistance,detectMap,saveFilterPath,onFaceZoFilterListener);
      } catch (Exception e) {
         LogUtils.e(e.toString());
         e.printStackTrace();
      }
   }

   private void executeResult(FaceZoFilterType type,
                              Bitmap bitmap,
                              float resistance,
                              Map<DetectFaceParts, List<PointF>> detectMap,
                              String saveFilterPath,
                              OnFaceZoFilterListener onFaceZoFilterListener){
      ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<FilterFaceZoInfoResult>() {
         @Override
         public FilterFaceZoInfoResult doInBackground() throws Throwable {
            FilterFaceZoInfoResult result = null;
            try {
               Bitmap oriBitmap = bitmap.copy(bitmap.getConfig(),true);
               boolean justFace = type==FaceZoFilterType.ZO_FACE_RED_BLOOD
                       ||type==FaceZoFilterType.ZO_FACE_UV_SPOT
                       ||type==FaceZoFilterType.ZO_FACE_BROWN_AREA
                       ||type==FaceZoFilterType.ZO_FACE_RED_BLOCK;
               Bitmap cutOriBitmap = cutoutOriImage(oriBitmap, detectMap,justFace);
               result = saveFilterPath==null?
                               new FilterFaceZoInfoResult(resistance,type):
                               new FilterFaceZoInfoResult(resistance,type,saveFilterPath);

               FaceZoFilter filter = createFilter(type,oriBitmap,cutOriBitmap);

               filter.onFilter(result);
               if (!StringUtils.isEmpty(result.getSavePath())){
                  Bitmap filterImage = result.getFilterImage();
                  boolean save = ImageUtils.save(filterImage, result.getSavePath(), Bitmap.CompressFormat.JPEG);
                  LogUtils.e("save filter"+ " "+save);
               }
               filter.recycleImages();
            }catch (Exception e){
               result = FilterFaceZoInfoResult.CreateFailResult();
            }
            return result;
         }

         @Override
         public void onSuccess(FilterFaceZoInfoResult result) {
            if (result!=null){
               result.setStatus(FilterFaceZoInfoResult.FilterStatus.SUCCESS);
               onFaceZoFilterListener.onFilterResult(result);
            }
         }
      });
   }

   private <F extends FaceZoFilter> F createFilter(FaceZoFilterType type,Bitmap oriImage,Bitmap partsImage)throws Exception{
      Class<? extends FaceZoFilter> filterClass = type.getType();
      F f = null;
      if (filterClass!=null){
         Constructor<? extends FaceZoFilter> declaredConstructor = filterClass.getDeclaredConstructor();
         declaredConstructor.setAccessible(true);
         f = (F) declaredConstructor.newInstance();
         f.setImage(oriImage,partsImage);
      }
      return f;
   }

   private Bitmap cutoutOriImage(Bitmap oriImage,Map<DetectFaceParts, List<PointF>> detectMap,boolean justFace){

      Bitmap canvasBitmap = Bitmap.createBitmap(oriImage.getWidth(),oriImage.getHeight(),oriImage.getConfig());
      Canvas canvas = new Canvas(canvasBitmap);
      Paint paint = new Paint();
      paint.setAntiAlias(true);
      paint.setFilterBitmap(true);
      paint.setDither(true);
      paint.setStyle(Paint.Style.FILL);

      List<Map.Entry<DetectFaceParts,List<PointF>>> list = new ArrayList<>(detectMap.entrySet());
      //升序排序
      Collections.sort(list, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

      for(Map.Entry<DetectFaceParts,List<PointF>> mapping:list){
         List<PointF> pointFList = mapping.getValue();
         Path path = null;
         if (mapping.getKey() == DetectFaceParts.FACE) {
            path = new Path();
            paint.setColor(Color.RED);
            for (int i = 0; i < pointFList.size(); i++) {
               PointF pointF = pointFList.get(i);
               if (i == 0) {
                  path.moveTo(pointF.x, pointF.y);
               } else {
                  path.lineTo(pointF.x, pointF.y);
               }
            }
            path.close();
            canvas.drawPath(path, paint);
         } else {
            if (!justFace){
               path = new Path();
               paint.setColor(Color.YELLOW);
               for (int i = 0; i < pointFList.size(); i++) {
                  PointF pointF = pointFList.get(i);
                  if (i == 0) {
                     path.moveTo(pointF.x, pointF.y);
                  } else {
                     path.lineTo(pointF.x, pointF.y);
                  }
               }
               path.close();
               canvas.drawPath(path, paint);
            }
         }
      }

      int width = oriImage.getWidth();
      int height = oriImage.getHeight();

      int [] srcOriginalPixels = new int[width * height];
      int [] canvasPixels = new int[width * height];
      int [] resultPixels = new int[width * height];
      oriImage.getPixels(srcOriginalPixels,0,width,0,0,width,height);
      canvasBitmap.getPixels(canvasPixels,0,width,0,0,width,height);

      for (int i = 0; i < canvasPixels.length; i++) {
         int pixel =  canvasPixels[i];
         int alpha = Color.alpha(pixel);
         if (alpha==0 ||pixel==Color.YELLOW) {
            resultPixels[i] = 0;
         }else {
            resultPixels[i] = srcOriginalPixels[i];
         }
      }
      canvasBitmap.setPixels(resultPixels,0,width, 0, 0, width, height);
      return canvasBitmap;
   }

}
