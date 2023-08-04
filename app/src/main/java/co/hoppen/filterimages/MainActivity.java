package co.hoppen.filterimages;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import co.hoppen.filter.FilterHelper;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.FilterType;
import co.hoppen.filter.OnDetectFaceListener;
import co.hoppen.filter.OnFilterListener;

/**
 * Created by YangJianHui on 2022/10/18.
 */
public class MainActivity extends AppCompatActivity {

   private AppCompatImageView filterView,oriView;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      oriView = findViewById(R.id.iv_ori);
      filterView = findViewById(R.id.iv_filter);
      Bitmap firstBitmap = getImageFromAssetsFile("11111.jpg");

      Bitmap imageFromAssetsFile = getImageFromAssetsFile("22.jpg");
      oriView.setImageBitmap(imageFromAssetsFile);

      File file = new File(Environment.getExternalStorageDirectory().getPath() + "/test");
      if (!file.exists()){
         file.mkdirs();
      }

      FilterHelper filterHelper = new FilterHelper();
      try {

         filterHelper.detectFace(firstBitmap, true, new OnDetectFaceListener() {
            @Override
            public void onDetectSuccess() {
               try {
                  filterHelper.execute(FilterType.FACE_RED_BLOOD, imageFromAssetsFile, 0, file.getPath() + "/1.jpg", new OnFilterListener() {
                     @Override
                     public void onFilter(FilterInfoResult filterInfoResult) {
                        LogUtils.e(filterInfoResult.toString());
                        String filterImagePath = filterInfoResult.getFilterImagePath();
                        //LogUtils.e(filterImagePath);
                        Bitmap bitmap = ImageUtils.getBitmap(filterImagePath);
                        if (bitmap!=null)filterView.setImageBitmap(bitmap);
                        filterView.setVisibility(View.VISIBLE);
                     }
                  });
               }catch (Exception e){
               }
            }
            @Override
            public void onDetectFaceFailure() {

            }
         });

      } catch (Exception e) {
         e.printStackTrace();
      }
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

   public void show(View view){
      if (view.getId()==R.id.iv_ori){
         filterView.setVisibility(View.VISIBLE);
      }else view.setVisibility(View.GONE);
   }


}
