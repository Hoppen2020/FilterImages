package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public class SkinOilSecretion extends Filter {


    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
            Bitmap originalImage = getOriginalImage();
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int count = width * height;

                int [] originalPixels = new int[count];
                int [] filterPixels = new int[count];

                originalImage.getPixels(originalPixels,0,width,0,0,width,height);
                int totalGray = 0;
                for (int i = 0; i <originalPixels.length ; i++) {
                    int originalPixel = originalPixels[i];
                    int R = Color.red(originalPixel);
                    int G = Color.green(originalPixel);
                    int B = Color.blue(originalPixel);
                    int gray = (int) (R * 0.3 + G * 0.59 + B * 0.11);
                    totalGray+=gray;
                    filterPixels[i] = gray;
                }
                int avgGray = totalGray / count;
                double totalCountPixels = 0;
                double totalWaterPixels = 0;
                double totalPercentPixels = 0;
                double hash = 0;
                for (int i = 0; i <originalPixels.length ; i++) {
                    int gray = filterPixels[i];
                    filterPixels[i] = originalPixels[i];
                    if (gray >= avgGray * 1.1 && gray <= 250) {
                        totalCountPixels++;
                    }
                    if (gray >= avgGray * 1.1 && gray > 250) {
                        totalWaterPixels++;
                    }

                    if (gray <avgGray * 1.2){

                    }else if (gray>90&&gray<=110){

                    }else if (gray>100){
                        totalPercentPixels++;
                        filterPixels[i]=Color.rgb(255, 255, 0);
                    }
                }
                totalCountPixels = totalWaterPixels * 60 + totalCountPixels * 2;
                hash = totalCountPixels / count;
                LogUtils.e(totalCountPixels,hash, totalPercentPixels,totalPercentPixels / count);
                int score = 0;
                score = (int) ((30)+ ((1-hash)*0.9) * 40);
                LogUtils.e(score);

                if (score<=50&&score>55){
                    score = (score-40) * 21 / 10 +40;
                }else if (score<=55){
                    score = (score-40) * 3 +25;
                }
                if (score > 80) {
                    score = 78;
                }
                if (score<35){
                    score =35;
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(score);
//                filterInfoResult.setRatio((totalPercentPixels * 100 / count));
//                filterInfoResult.setDepth(0);
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        return filterInfoResult;
    }
}
