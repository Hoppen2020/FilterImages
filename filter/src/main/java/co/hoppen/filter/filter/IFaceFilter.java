package co.hoppen.filter.filter;

import co.hoppen.filter.FaceAreaInfo;
import co.hoppen.filter.FacePart;


/**
 * Created by YangJianHui on 2022/4/7.
 */
public interface IFaceFilter {
    FacePart[] getFacePart();
    //创建区域图片
    FaceAreaInfo createAreaBitmap(Object ...obj);
}
