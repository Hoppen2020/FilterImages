package co.hoppen.filter.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.blankj.utilcode.util.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by YangJianHui on 2022/3/1.
 */
public class BitmapUtils {
   public static Bitmap getImageFromAssetsFile(String fileName) {
      Bitmap image = null;
      AssetManager am = Utils.getApp().getResources().getAssets();
      try {
         InputStream is = am.open(fileName);
         image = BitmapFactory.decodeStream(is);
         is.close();
      }catch (IOException e) {
         e.printStackTrace();
      }
      return image;
   }
}
