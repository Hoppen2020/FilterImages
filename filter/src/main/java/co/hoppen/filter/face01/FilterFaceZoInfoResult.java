package co.hoppen.filter.face01;

import android.graphics.Bitmap;
import android.graphics.PointF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.hoppen.filter.DetectFaceParts;

/**
 * Created by YangJianHui on 2024/4/8.
 */
public class FilterFaceZoInfoResult {

    private int score;

    private float resistance;

    private FaceZoFilterType type;

    private String savePath;

    private Bitmap filterImage;

    private FilterStatus status = FilterStatus.NOT_CONVERTED;


    public FilterFaceZoInfoResult(float resistance, FaceZoFilterType type, String savePath) {
        this.resistance = resistance;
        this.type = type;
        this.savePath = savePath;
    }

    public FilterFaceZoInfoResult(float resistance, FaceZoFilterType type) {
        this.resistance = resistance;
        this.type = type;
    }
    private FilterFaceZoInfoResult(){
        setStatus(FilterStatus.FAILURE);
    }

    public static FilterFaceZoInfoResult CreateFailResult(){
        return new FilterFaceZoInfoResult();
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

    public FaceZoFilterType getType() {
        return type;
    }

    public String getSavePath() {
        return savePath;
    }

    public Bitmap getFilterImage() {
        return filterImage;
    }

    public void setFilterImage(Bitmap filterImage) {
        this.filterImage = filterImage;
    }

    public FilterStatus getStatus() {
        return status;
    }

    public void setStatus(FilterStatus status) {
        this.status = status;
    }

    public enum FilterStatus{
        NOT_CONVERTED,
        SUCCESS,
        FAILURE
    }

    @Override
    public String toString() {
        return "FilterFaceZoInfoResult{" +
                "score=" + score +
                ", resistance=" + resistance +
                ", type=" + type +
                ", savePath='" + savePath + '\'' +
                ", filterImage=" + filterImage +
                ", status=" + status +
                '}';
    }

}
