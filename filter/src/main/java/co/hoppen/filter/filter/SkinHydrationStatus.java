package co.hoppen.filter.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

import co.hoppen.filter.FilterInfoResult;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public class SkinHydrationStatus extends Filter {

    @Override
    public FilterInfoResult onFilter() {
        FilterInfoResult filterInfoResult = getFilterInfoResult();
        try {
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
                    if (gray >= avgGray * 1.2 && gray <= 250) {
                        totalCountPixels++;
                    }
                    if (gray >= avgGray * 1.2 && gray > 250) {
                        totalWaterPixels++;
                    }

                    if (gray <avgGray * 1.2){

                    } else if (gray>90&&gray<=110){

                    }else if (gray>100){
                        totalPercentPixels++;
                        filterPixels[i]=Color.rgb(17, 17, 255);
                    }
                }
                totalCountPixels = totalWaterPixels * 60 + totalCountPixels * 2;
                hash = totalCountPixels / count;
                int score = 0;

                if (hash<=0){
                    score = new Random().nextInt(5) + 40;
                }else score = (int) (40 + hash * 40);

                if (getResistance()!=0){
                    if (getResistance()<=10){
                        score = 20 +new Random().nextInt(3);
                    }else {
                        int x = (int) ((getResistance()-10) * 7 / 9 +20);
                        if (x>=85){
                            score =85+new Random().nextInt(3);
                        }else{
                            score = x;
                        }
                    }
                    if (score<=19){
                        score = new Random().nextInt(10) + 30;
                    } else if (score>=20&&score<=29){
                        score=(score-20)  * 25/9 +20;
                    }else if (score>=30&&score<=35){
                        score=(score-30)  * 15/5 +45;
                    }else if (score>=36&&score<=39){
                        score=(score-36)  * 15/3 +50;
                    }else if (score>=40&&score<=75){
                        score=(score-40)  * 10/25 +60;
                    }else if (score>=76&&score<=80){
                        score=(score-76)  * 9/5 +71;
                    }else if (score>=81&&score<=89){
                        score=(score-80)  * 14/9 +75;
                    }else if (score<=90){
                        score= new Random().nextInt(5)+80;
                    }
                }else {
                    if (score > 80) {
                        score = 78+new Random().nextInt(3);
                    }
                    if (score<40){
                        score =38+new Random().nextInt(3);
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
                bitmap.setPixels(filterPixels,0,width, 0, 0, width, height);
                filterInfoResult.setResistance(getResistance());
                filterInfoResult.setScore(score);
//                filterInfoResult.setRatio((totalPercentPixels * 100 / count));
//                filterInfoResult.setDepth(0);
                filterInfoResult.setFilterBitmap(bitmap);
                filterInfoResult.setStatus(FilterInfoResult.Status.SUCCESS);
        }catch (Exception e){
            filterInfoResult.setStatus(FilterInfoResult.Status.FAILURE);
        }
        return filterInfoResult;
    }

}
