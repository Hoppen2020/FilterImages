package co.hoppen.filter;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.service.media.MediaBrowserService;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YangJianHui on 2023/9/22.
 */
public class Test {

   public static void test(){
      try {
         String path = Environment.getExternalStorageDirectory().getPath() + "/test/face_parsing.onnx";

         //Net net = Dnn.readNetFromONNX(path);

         Net net = Dnn.readNetFromTorch(Environment.getExternalStorageDirectory().getPath() + "/test/face.pth");

//         Dnn.readNetFromTorch()

         Bitmap imageFromAssetsFile = getImageFromAssetsFile("116_ori.png");

         Mat src = new Mat();

         Utils.bitmapToMat(imageFromAssetsFile,src);

         Imgproc.cvtColor(src,src,Imgproc.COLOR_RGB2BGR);

         Mat blob = Dnn.blobFromImage(src);

         net.setInput(blob);

         Mat forward = net.forward();




         int count =  forward.size(0);

         int num = forward.size(1);

         int rows = forward.size(2);

         int cols = forward.size(3);

         int channels = forward.size(4);


//         int [] ids =  new int[]{count-1,num-1,rows-1,cols-1};
//         LogUtils.e(forward.dims(),Arrays.toString(ids));
//
//         double[] doubles = forward.get(ids);
//
//         LogUtils.e(doubles);

         for (int n = 0; n < num; n++) {
            List<Double> vList = new ArrayList<>();

            for (int h = 0; h < rows; h++) {
               for (int w = 0; w < cols; w++) {
                  vList.add(forward.get(new int[]{count-1, n, h, w})[0]);
               }
            }

            double [] d = new double[vList.size()];
            for (int i = 0; i < vList.size(); i++) {
               d [i] = vList.get(i);
            }
            Mat mat = new Mat(512,512,CvType.CV_32FC1);
            mat.put(0,0,d);

            mat.convertTo(mat,CvType.CV_8UC1,255);

            save(150+n,mat);



         }


      }catch (Exception e){
         LogUtils.e(e.toString());
      }
   }

   private static void save(int index,Mat mat){
      Bitmap imageFromAssetsFile = getImageFromAssetsFile("116_ori.png");

      Bitmap bitmap = imageFromAssetsFile.copy(Bitmap.Config.ARGB_8888,true);

      Utils.matToBitmap(mat,bitmap);

      ImageUtils.save(bitmap,Environment.getExternalStorageDirectory().getPath() + "/test/ori_"+index+".png", Bitmap.CompressFormat.PNG);

   }


   private static Bitmap getImageFromAssetsFile(String fileName) {
      Bitmap image = null;
      AssetManager am = com.blankj.utilcode.util.Utils.getApp().getResources().getAssets();
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
