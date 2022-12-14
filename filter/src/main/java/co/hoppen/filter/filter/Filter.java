package co.hoppen.filter.filter;

import android.graphics.Bitmap;

import co.hoppen.filter.FilterInfoResult;
import co.hoppen.filter.FilterType;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public abstract class Filter {
    //原图
    private Bitmap originalImage;
    //电阻值
    private float resistance;

    private FilterInfoResult filterInfoResult;

    Filter(){

    }

    //算法逻辑
    public abstract FilterInfoResult onFilter() throws Exception;

    public void createFilterResult(FilterType type,String filterImagePath) {
        filterInfoResult = new FilterInfoResult(type,filterImagePath);
        filterInfoResult.setResistance(resistance);
    }

    public FilterInfoResult getFilterInfoResult() {
        return filterInfoResult;
    }

    public Filter setOriginalImage(Bitmap originalImage) {
        this.originalImage = originalImage;
        return this;
    }

    public Filter setResistance(float resistance) {
        this.resistance = resistance;
        return this;
    }

    protected Bitmap getOriginalImage() {
        return originalImage;
    }

    protected float getResistance() {
        return resistance;
    }

    public void recycle(){
        if (originalImage!=null&&!originalImage.isRecycled()){
            originalImage.recycle();
            originalImage = null;
        }
    }

}
