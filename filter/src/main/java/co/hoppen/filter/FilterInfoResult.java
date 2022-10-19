package co.hoppen.filter;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public class FilterInfoResult {
    private FilterType type;

    private String filterImagePath;

    private FaceAreaInfo faceAreaInfo;

    private int score;

    private float resistance;

    private String dataTypeString="";

    private Status status = Status.NOT_CONVERTED;

    public FilterInfoResult(String filterImagePath){
        this.filterImagePath = filterImagePath;
    }

    public FilterInfoResult(FilterType type,String filterImagePath){
        this.type = type;
        this.filterImagePath = filterImagePath;
    }

    public String getFilterImagePath() {
        return filterImagePath;
    }

    public void setFilterBitmap(Bitmap filterBitmap) {
        if (!StringUtils.isEmpty(filterImagePath)){
            boolean save = ImageUtils.save(filterBitmap, filterImagePath, Bitmap.CompressFormat.JPEG);
//            LogUtils.e("filter_save: "+save+"  "+filterImagePath);
            if (filterBitmap!=null&&!filterBitmap.isRecycled())filterBitmap.recycle();
        }
    }

    public FaceAreaInfo getFaceAreaInfo() {
        return faceAreaInfo;
    }

    public void setFaceAreaInfo(FaceAreaInfo faceAreaInfo) {
        this.faceAreaInfo = faceAreaInfo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getResistance() {
        return resistance;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public FilterType getType() {
        return type;
    }

    public String getDataTypeString() {
        return dataTypeString;
    }

    public void setDataTypeString(String dataTypeString){
        this.dataTypeString = dataTypeString;
    }

    public void setDataTypeString(FilterDataType type,Object obj) {
        String dataTypeString = "";
        switch (type){
            case AREA:
                double d = (double) obj;
                BigDecimal bigDecimal = new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
                dataTypeString = bigDecimal.doubleValue() + "%";
                break;
            case COLOR:

                break;
            case COUNT:
                dataTypeString = (int)obj + StringUtils.getString(R.string.count);
                break;
        }

        this.dataTypeString = dataTypeString;
    }

    @Override
    public String toString() {
        return "FilterInfoResult{" +
                "type=" + type +
                ", filterImagePath='" + filterImagePath + '\'' +
                ", faceAreaInfo=" + faceAreaInfo +
                ", score=" + score +
                ", resistance=" + resistance +
                ", status=" + status +
                '}';
    }

    public enum Status{
        NOT_CONVERTED,
        SUCCESS,
        FAILURE
    }

}
