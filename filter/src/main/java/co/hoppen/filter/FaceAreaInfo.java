package co.hoppen.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by YangJianHui on 2022/8/4.
 */
public class FaceAreaInfo {
   private List<Path> paths;
   private int width;
   private int height;
   private List<List<Point>> areaPointList;

   public static FaceAreaInfo createFaceAreaInfo(List<List<Point>> areaPointList,int width,int height){
      return new FaceAreaInfo(areaPointList,width,height);
   }

   private FaceAreaInfo(List<List<Point>> areaPointList,int width,int height){
      this.width = width;
      this.height =  height;
      this.areaPointList = areaPointList;
      this.paths = new ArrayList<>();

      for (int i = 0; i < areaPointList.size(); i++) {
         List<Point> points = areaPointList.get(i);
         Path path = new Path();
         for (int j = 0; j < points.size(); j++) {
            Point point = points.get(j);
            int x = point.x;
            int y = point.y;
            if (j==0){
               path.moveTo(x,y);
            }else {
               path.lineTo(x,y);
            }
         }
         path.close();
         paths.add(path);
      }
   }

//   public FaceAreaInfo(List<Path> paths, int width, int height) {
//      this.paths = paths;
//      this.width = width;
//      this.height = height;
//   }

   public Bitmap getFaceAreaBitmap() {
      if (paths!=null){
         Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap);
         Paint paint = new Paint();
         paint.setColor(Color.RED);
         paint.setStyle(Paint.Style.STROKE);
         Iterator<Path> iterator = paths.iterator();
         while (iterator.hasNext()){
            Path next = iterator.next();
            canvas.drawPath(next,paint);
         }
         return bitmap;
      }
      return null;
   }

   public List<Path> getPaths() {
      return paths;
   }

   public void setPaths(List<Path> paths) {
      this.paths = paths;
   }

   @Override
   public String toString() {
      return "FaceAreaInfo{" +
              "paths=" + paths +
              ", width=" + width +
              ", height=" + height +
              '}';
   }

   public int getWidth() {
      return width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public List<List<Point>> getAreaPointList() {
      return areaPointList;
   }

   public void setAreaPointList(List<List<Point>> areaPointList) {
      this.areaPointList = areaPointList;
   }

}
