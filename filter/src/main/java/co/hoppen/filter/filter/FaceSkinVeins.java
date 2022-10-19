package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import co.hoppen.filter.FacePart;
import co.hoppen.filter.FilterDataType;
import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2022/3/7.
 */
public class FaceSkinVeins extends FaceFilter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
            Bitmap originalImage = getOriginalImage();
                Bitmap operateBitmap = getFaceAreaImage();

                int width = operateBitmap.getWidth();
                int height = operateBitmap.getHeight();
                int [] piecePixels = new int[width * height];
                operateBitmap.getPixels(piecePixels,0,width,0,0,width,height);

                int sumN = 0;
                int countN = 0;
                int avgN = 0;

                int minGray = 255;
                int maxGray = 0;

                for (int k = 0; k < piecePixels.length; k++) {
                    int pixel = piecePixels[k];
                    int alpha = Color.alpha(pixel);
                    if (alpha==0)continue;
                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);
                    int gray = (r + g + b) / 3;
                    if (minGray>gray){
                        minGray = gray;
                    }
                    if (maxGray<gray){
                        maxGray = gray;
                    }
                    sumN +=gray;
                    piecePixels[k] = Color.rgb(gray,gray,gray);
                    countN++;
                }
                avgN = sumN / countN;
                LogUtils.e(avgN);

                for (int i = 0; i < piecePixels.length; i++) {
                    int pixel = piecePixels[i];
                    int alpha = Color.alpha(pixel);
                    if (alpha==0)continue;
                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);
                    int gray = (r + g + b) / 3;
                    gray = (gray - minGray) * 255 / (maxGray - minGray);
                    int abs = Math.abs(gray - avgN);
//                    if (abs < avgN){
//                        abs = 0;
//                    }
                    piecePixels[i] = Color.rgb(abs,abs,abs);
                }

                operateBitmap.setPixels(piecePixels,0,width, 0, 0, width, height);


                Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888,true);
                Canvas canvas = new Canvas(resultBitmap);
                canvas.drawBitmap(operateBitmap,0,0,null);


                Mat resultMat = new Mat();

                Utils.bitmapToMat(resultBitmap,resultMat);

                double scale =0.3;

                Imgproc.resize(resultMat,resultMat,resultMat.size(),scale,scale,Imgproc.INTER_LINEAR);


                Utils.matToBitmap(resultMat,resultBitmap);

                LogUtils.e(resultBitmap.getWidth(),resultBitmap.getHeight());

                filterInfoResult.setFilterBitmap(resultBitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);

        } catch (Exception e) {
            LogUtils.e(e.toString());
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }

    @Override
    public FacePart[] getFacePart() {
        //FacePart.FACE_NOSE_LEFT_RIGHT  //FACE_SKIN
        return new FacePart[]{FacePart.FACE_LEFT_RIGHT_AREA};
    }

    @Override
    public FilterDataType getFilterDataType() {
        return FilterDataType.AREA;
    }

//    @Override
//    public FaceAreaInfo createAreaBitmap(Object... obj) {
//        return null;
//    }

    private class PieceInfo{
        private int x, y;

        private int [] piece;
        //绝对偏差总和
        private int avg;
        private int sumMinus;
        private int avgMinus;
        private float coarseness;

        public void createMinus(){
                for (int i = 0; i < piece.length; i++) {
                    int gray = Color.red(piece[i]);
                    gray = Math.abs(gray - avg);
                    sumMinus += gray;
                    piece[i] = Color.rgb(gray,gray,gray);
                }
                avgMinus = sumMinus /piece.length;
        }

        public Integer[] getPieceForInteger(){
            return ArrayUtils.toObject(piece);
        }

    }

}