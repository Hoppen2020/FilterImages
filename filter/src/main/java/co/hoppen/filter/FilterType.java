package co.hoppen.filter;

import co.hoppen.filter.filter.CollagenStatus;
import co.hoppen.filter.filter.ElasticFiberStatus;
import co.hoppen.filter.filter.FaceAcne;
import co.hoppen.filter.filter.FaceBlackHeads;
import co.hoppen.filter.filter.FaceBrownArea2;
import co.hoppen.filter.filter.FaceDarkCircles;
import co.hoppen.filter.filter.FaceEpidermisSpots;
import co.hoppen.filter.filter.FaceFollicleCleanDegree;
import co.hoppen.filter.filter.FaceHornyPlug;
import co.hoppen.filter.filter.FaceHydrationStatus;
import co.hoppen.filter.filter.FaceNearRedLight;
import co.hoppen.filter.filter.FaceOilSecretion;
import co.hoppen.filter.filter.FacePorphyrin;
import co.hoppen.filter.filter.FaceRedBlock;
import co.hoppen.filter.filter.FaceRedBlood2;
import co.hoppen.filter.filter.FaceSkinVeins3;
import co.hoppen.filter.filter.FaceSuperficialPlaque;
import co.hoppen.filter.filter.FaceTest;
import co.hoppen.filter.filter.FaceUvSpot;
import co.hoppen.filter.filter.FaceWrinkle;
import co.hoppen.filter.filter.FaceWrinkle8;
import co.hoppen.filter.filter.FaceWrinkle9;
import co.hoppen.filter.filter.Filter;
import co.hoppen.filter.filter.FollicleCleanDegree;
import co.hoppen.filter.filter.SkinHydrationStatus;
import co.hoppen.filter.filter.SkinOilSecretion;
import co.hoppen.filter.filter.SkinPigmentStatus;
import co.hoppen.filter.filter.SkinRedBloodStatus;
import co.hoppen.filter.filter.SkinTest;
import co.hoppen.filter.filter.TestFilter;

/**
 * Created by YangJianHui on 2021/9/10.
 */
public enum FilterType {
    SKIN_HYDRATION_STATUS(SkinHydrationStatus.class),
    SKIN_OIL_SECRETION(SkinOilSecretion.class),
    SKIN_PIGMENT_STATUS(SkinPigmentStatus.class),
    SKIN_RED_BLOOD_STATUS(SkinRedBloodStatus.class),
    FOLLICLE_CLEAN_DEGREE(FollicleCleanDegree.class),
    ELASTIC_FIBER_STATUS(ElasticFiberStatus.class),
    COLLAGEN_STATUS(CollagenStatus.class),

    SKIN_TEST(SkinTest.class),


    //全脸类别
    //--------------水油----------------------
    //全脸——水（RGB）
    FACE_HYDRATION_STATUS(FaceHydrationStatus.class),
    //全脸——油份含量（平行PL）
    FACE_OIL_SECRETION(FaceOilSecretion.class),
    //--------------皱纹---------------------
    //全脸——皱纹
    FACE_WRINKLE(FaceWrinkle.class),
    //全脸——皮肤纹理
    FACE_SKIN_VEINS(FaceSkinVeins3.class),
    //--------------眼圈---------------------
    //全脸——黑眼圈（RGB）
    FACE_DARK_CIRCLES(FaceDarkCircles.class),
    //--------------毛囊---------------------
    //全脸——毛孔
    FACE_FOLLICLE_CLEAN_DEGREE(FaceFollicleCleanDegree.class),
    //黑头
    FACE_BLACK_HEADS(FaceBlackHeads.class),
    //全脸——角质栓（UV）
    FACE_HORNY_PLUG(FaceHornyPlug.class),
    //--------------痤疮---------------------
    //全脸——痤疮(RGB)
    FACE_ACNE(FaceAcne.class),
    //全脸——卟啉（伍氏）
    FACE_PORPHYRIN(FacePorphyrin.class),
    //--------------敏感---------------------
    //全脸——红血丝(交叉PL)×分数
    FACE_RED_BLOOD(FaceRedBlood2.class),
    //全脸——敏感(交叉PL)
    FACE_NEAR_RED_LIGHT(FaceNearRedLight.class),
    //红色区
    FACE_RED_BLOCK(FaceRedBlock.class),
    //--------------色斑---------------------
    //全脸——表层斑(RGB)
    FACE_EPIDERMIS_SPOTS(FaceEpidermisSpots.class),
    //全脸——色素沉着（交叉pl）
    FACE_SUPERFICIAL_PLAQUE(FaceSuperficialPlaque.class),
    //棕色区
    FACE_BROWN_AREA(FaceBrownArea2.class),
    //紫外线斑
    FACE_UV_SPOT(FaceUvSpot.class),

    //全脸——测试
    FACE_TEST(FaceTest.class),

    TEST(TestFilter.class);

    private Class<? extends Filter> type;

    FilterType(Class<? extends Filter> type){
        this.type = type;
    }

    public Class<? extends Filter> getType() {
        return type;
    }
}
