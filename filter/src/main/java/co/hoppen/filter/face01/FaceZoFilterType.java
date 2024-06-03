package co.hoppen.filter.face01;

import co.hoppen.filter.face01.filter.ZoFaceBlackHeads;
import co.hoppen.filter.face01.filter.ZoFaceBrownArea;
import co.hoppen.filter.face01.filter.ZoFaceFollicleCleanDegree;
import co.hoppen.filter.face01.filter.ZoFaceHydrationStatus;
import co.hoppen.filter.face01.filter.ZoFaceOilSecretion;
import co.hoppen.filter.face01.filter.ZoFaceRedBlock;
import co.hoppen.filter.face01.filter.ZoFaceRedBlood;
import co.hoppen.filter.face01.filter.ZoFaceSuperficialPlaque;
import co.hoppen.filter.face01.filter.ZoFaceUvSpot;
import co.hoppen.filter.face01.filter.ZoFaceWrinkle;
import co.hoppen.filter.filter.Filter;

/**
 * Created by YangJianHui on 2024/4/8.
 */
public enum FaceZoFilterType {

    //水分含量
    ZO_FACE_HYDRATION_STATUS(ZoFaceHydrationStatus.class),
    //油脂程度
    ZO_FACE_OIL_SECRETION(ZoFaceOilSecretion.class),
    //皮肤皱纹
    ZO_FACE_WRINKLE(ZoFaceWrinkle.class),
    //色素沉着
    ZO_FACE_SUPERFICIAL_PLAQUE(ZoFaceSuperficialPlaque.class),
    //红血丝
    ZO_FACE_RED_BLOOD(ZoFaceRedBlood.class),
    //毛孔状态
    ZO_FACE_FOLLICLE_CLEAN_DEGREE(ZoFaceFollicleCleanDegree.class),
    //粉刺状态
    ZO_FACE_BLACK_HEADS(ZoFaceBlackHeads.class),
    //紫外线斑
    ZO_FACE_UV_SPOT(ZoFaceUvSpot.class),
    //棕色区
    ZO_FACE_BROWN_AREA(ZoFaceBrownArea.class),
    //红色区
    ZO_FACE_RED_BLOCK(ZoFaceRedBlock.class);

    private Class<? extends FaceZoFilter> type;

    FaceZoFilterType(Class<? extends FaceZoFilter> type){
        this.type = type;
    }

    public Class<? extends FaceZoFilter> getType() {
        return type;
    }

}
