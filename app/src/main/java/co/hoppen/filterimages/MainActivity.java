package co.hoppen.filterimages;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.hoppen.filter.FilterHelper;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.FilterType;
import co.hoppen.filter.OnDetectFaceListener;
import co.hoppen.filter.OnFilterListener;
import co.hoppen.filter.Test;

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

      Bitmap firstBitmap = getImageFromAssetsFile("a2.jpg");

      Bitmap imageFromAssetsFile = getImageFromAssetsFile("a1.jpg");
      oriView.setImageBitmap(imageFromAssetsFile);

      File file = new File(Environment.getExternalStorageDirectory().getPath() + "/test");
      if (!file.exists()){
         file.mkdirs();
      }

      FilterHelper filterHelper = new FilterHelper();
      try {

//         filterHelper.execute(FilterType.FACE_DARK_CIRCLES, imageFromAssetsFile, 0, file.getPath() + "/2.jpg", new OnFilterListener() {
//            @Override
//            public void onFilter(FilterInfoResult filterInfoResult) {
//               LogUtils.e(filterInfoResult.toString());
//               String filterImagePath = filterInfoResult.getFilterImagePath();
//               //LogUtils.e(filterImagePath);
//               Bitmap bitmap = ImageUtils.getBitmap(filterImagePath);
//               if (bitmap!=null)filterView.setImageBitmap(bitmap);
//               filterView.setVisibility(View.VISIBLE);
//            }
//         });

         //--------------------------------------👇这个参数 记得改
         filterHelper.detectFace(firstBitmap, true, new OnDetectFaceListener() {
            @Override
            public void onDetectSuccess(List<PointF> partPoint) {
               try {
                  LogUtils.e(partPoint.toString());
                  //"/"+new Random().nextInt(100000) +".jpg"
                  filterHelper.execute(FilterType.FACE_DARK_CIRCLES, imageFromAssetsFile, 0, file.getPath() + "/2.jpg", new OnFilterListener() {
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
               LogUtils.e("onDetectFaceFailure");
            }
         });

      } catch (Exception e) {
         e.printStackTrace();
      }

      //Test.test();

   }

   private void batchList(List<String> assetsFileList,int index){
      String name = assetsFileList.get(index);
      Bitmap firstBitmap = getImageFromAssetsFile(name);

      Bitmap imageFromAssetsFile = getImageFromAssetsFile(name);
      oriView.setImageBitmap(imageFromAssetsFile);

      File file = new File(Environment.getExternalStorageDirectory().getPath() + "/test");
      if (!file.exists()){
         file.mkdirs();
      }

      FilterHelper filterHelper = new FilterHelper();
      try {
         filterHelper.detectFace(firstBitmap, true, new OnDetectFaceListener() {
            @Override
            public void onDetectSuccess(List<PointF> partPoint) {
               try {
                  //LogUtils.e(partPoint.toString());
                  //"/"+new Random().nextInt(100000) +".jpg"
                  filterHelper.execute(FilterType.FACE_RED_BLOOD, imageFromAssetsFile, 0, file.getPath() + "/"+System.currentTimeMillis()+".jpg", new OnFilterListener() {
                     @Override
                     public void onFilter(FilterInfoResult filterInfoResult) {
                        LogUtils.e(filterInfoResult.toString());
                        String filterImagePath = filterInfoResult.getFilterImagePath();
                        //LogUtils.e(filterImagePath);
                        Bitmap bitmap = ImageUtils.getBitmap(filterImagePath);
                        if (bitmap!=null)filterView.setImageBitmap(bitmap);
                        //filterView.setVisibility(View.VISIBLE);
                        if (index<assetsFileList.size()){
                           batchList(assetsFileList,index+1);
                        }
                     }
                  });
               }catch (Exception e){
               }
            }
            @Override
            public void onDetectFaceFailure() {
               LogUtils.e("onDetectFaceFailure");
            }
         });

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void detectFaceAdnExecute(){


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


   private List<String> getAssetsFileList(){
      List<String> list = new ArrayList<>();
      for (int i = 0; i < 17; i++) {
         list.add("test_red/"+(i+1)+".jpg");
      }
      return list;
   }


}
