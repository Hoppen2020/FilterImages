package co.hoppen.filterimages;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;

import co.hoppen.filter.FilterHelper;
import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.FilterType;
import co.hoppen.filter.OnFilterListener;

/**
 * Created by YangJianHui on 2024/3/21.
 */
public class HairDetectActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_hair);

      FilterHelper filterHelper = new FilterHelper();

//      getImageFromAssetsFile("h1.jpg")?.let {
//         filterHelper.execute(FilterType.HAIR_THICKNESS,it){
//            LogUtils.e(it)
//            findViewById<AppCompatImageView>(R.id.image).setImageBitmap(it.hairFilterBitmap)
//         }
//      }

      Bitmap bitmap = getImageFromAssetsFile("h1.jpg");

      try {
         filterHelper.execute(FilterType.HAIR_DENSITY, bitmap, new OnFilterListener() {
            @Override
            public void onFilter(FilterInfoResult filterInfoResult) {
               LogUtils.e(filterInfoResult.toString());
               ((AppCompatImageView)findViewById(R.id.image)).setImageBitmap(filterInfoResult.getHairFilterBitmap());
            }
         });
      }catch (Exception e){
         LogUtils.e(e.toString());
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

}
