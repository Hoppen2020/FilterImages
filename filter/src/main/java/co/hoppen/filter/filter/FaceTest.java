package co.hoppen.filter.filter;


import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/12.
 */
public class FaceTest extends Filter{

   @Override
   public FilterInfoResult onFilter() {
      return null;
   }

//毛发去除 method one
//   Mat oriMat = new Mat();
//            Utils.bitmapToMat(bitmap,oriMat);
//            Imgproc.cvtColor(oriMat,oriMat,Imgproc.COLOR_RGBA2RGB);
//
//   Mat grayMat = new Mat();
//            Imgproc.cvtColor(oriMat,grayMat,Imgproc.COLOR_RGB2GRAY);
//
//   Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(21,21));
//            Imgproc.morphologyEx(grayMat,grayMat,Imgproc.MORPH_BLACKHAT,kernel);
//
//            Imgproc.threshold(grayMat,grayMat,10,255,Imgproc.THRESH_BINARY);
//
//   Mat result = new Mat();
//            Photo.inpaint(oriMat,grayMat,result,1,Photo.INPAINT_TELEA);
//            Photo.detailEnhance(oriMat,oriMat);
//
//            Utils.matToBitmap(result,bitmap);

}
