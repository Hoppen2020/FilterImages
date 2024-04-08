package co.hoppen.filterimages;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.hoppen.filter.DetectFaceParts;
import co.hoppen.filter.FilterHelper;
import co.hoppen.filter.OnDetectFaceListener;
import co.hoppen.filter.OnDetectFacePartsListener;

/**
 * Created by YangJianHui on 2024/4/7.
 */
public class Face01Activity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_01_filter);

      FilterHelper filterHelper = new FilterHelper();

      ImageView filterView = findViewById(R.id.filter);

      Bitmap bitmap = getImageFromAssetsFile("face_01.jpg");

      findViewById(R.id.parent).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            filterView.setImageBitmap(null);
            ThreadUtils.runOnUiThreadDelayed(new Runnable() {
               @Override
               public void run() {
                  filterHelper.detectFace(bitmap, new OnDetectFacePartsListener() {
                     @Override
                     public void onDetectSuccess(Map<DetectFaceParts, List<PointF>> detectMap) {

                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setFilterBitmap(true);
                        paint.setColor(Color.RED);

                        Bitmap drawBitmap = bitmap.copy(bitmap.getConfig(),true);

                        Canvas canvas = new Canvas(drawBitmap);


                        for (Map.Entry<DetectFaceParts, List<PointF>> next : detectMap.entrySet()) {
                           List<PointF> partPoint = next.getValue();
                           Path path = new Path();

                           for (int i = 0; i < partPoint.size(); i++) {
                              PointF pointF = partPoint.get(i);
                              paint.setStyle(Paint.Style.FILL);
                              canvas.drawCircle(pointF.x, pointF.y, 10, paint);
                              paint.setStyle(Paint.Style.STROKE);
                              canvas.drawCircle(pointF.x, pointF.y, 15, paint);
                              if (i == 0) {
                                 path.moveTo(pointF.x, pointF.y);
                              } else path.lineTo(pointF.x, pointF.y);
                           }
                           path.close();
                           canvas.drawPath(path, paint);
                        }
                        filterView.setImageBitmap(drawBitmap);
                     }

                     @Override
                     public void onDetectFaceFailure() {

                     }
                  });
               }
            },1000);
         }
      });

   }

   private Bitmap getImageFromAssetsFile(String fileName) {
      Bitmap image = null;
      AssetManager am = getResources().getAssets();
      try {
         InputStream is = am.open(fileName);
         image = BitmapFactory.decodeStream(is);
         is.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return image;
   }



}
