package co.hoppen.filter.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.LogUtils;
import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;

import java.util.List;

import co.hoppen.filter.FacePart;

import static com.huawei.hms.mlsdk.face.MLFaceShape.TYPE_FACE;

/**
 * Created by YangJianHui on 2022/4/7.
 */
public class CutoutUtils {

    /**
     *
     * @param src 原图
     * @param faceParts 需要的部位
     * @param face
     * @return
     */
    public static Bitmap cutoutPart(Bitmap src, FacePart[] faceParts, MLFace face){
        Bitmap canvasBitmap = Bitmap.createBitmap(src.getWidth(),src.getHeight(),src.getConfig());
        Canvas canvas = new Canvas(canvasBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        for (int i = 0; i < faceParts.length; i++) {
            FacePart facePart = faceParts[i];
            switch (facePart){
                case FACE_T:
                    cutoutT(face,canvas,paint);
                    break;
                case FACE_EYE_BOTTOM:
                    cutoutEyeBottom(face,canvas,paint);
                    break;
                case FACE_LEFT_RIGHT_AREA:
                    cutoutLeftRightFace(face,canvas,paint);
                    break;
                case FACE_NOSE:
                    cutoutNose(face,canvas,paint);
                    break;
                case FACE_NOSE_LEFT_RIGHT:
                    cutoutNoseLeftRight(face,canvas,paint);
                    break;
                case FACE_FOREHEAD:
                    cutoutForehead(face,canvas,paint);
                    break;
                case FACE_MIDDLE:
                    cutoutMiddle(face,canvas,paint);
                    break;
                case FACE_SKIN:
                    cutoutFaceSkin(face, canvas,paint);
                    break;
            }
        }
        int width = src.getWidth();
        int height = src.getHeight();

        int [] srcOriginalPixels = new int[width * height];
        int [] canvasPixels = new int[width * height];
        int [] resultPixels = new int[width * height];
        src.getPixels(srcOriginalPixels,0,width,0,0,width,height);
        canvasBitmap.getPixels(canvasPixels,0,width,0,0,width,height);

        for (int i = 0; i < canvasPixels.length; i++) {
            if (ArrayUtils.contains(faceParts,FacePart.FACE_SKIN)){
                resultPixels[i] = canvasPixels[i];
            }else {
                int pixel =  canvasPixels[i];
                int alpha = Color.alpha(pixel);
                if (alpha==0) {
                    resultPixels[i] = 0;
                }else {
                    resultPixels[i] = srcOriginalPixels[i];
                }
            }
        }
        canvasBitmap.setPixels(resultPixels,0,width, 0, 0, width, height);
        return canvasBitmap;
    }


    public static Bitmap cutoutMouth(Bitmap src, List<PointF> pointFs){
        Bitmap canvasBitmap = Bitmap.createBitmap(src.getWidth(),src.getHeight(),src.getConfig());
        Canvas canvas = new Canvas(canvasBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        Path path = null;
        for (int i = 0; i < pointFs.size(); i++) {
            PointF pointF =  pointFs.get(i);
            if (path == null){
                path = new Path();
                path.moveTo(pointF.x,pointF.y);
            }else {
                path.lineTo(pointF.x,pointF.y);
            }
        }
        path.close();
        canvas.drawPath(path,paint);
        return canvasBitmap;
    }

    private static void cutoutFaceSkin(MLFace face, Canvas canvas, Paint paint) {

        List<MLPosition> points = face.getFaceShape(TYPE_FACE).getPoints();

        Path path = null;
        for (int i = 0; i < points.size(); i++) {
            MLPosition mlPosition = points.get(i);
            if (i==0){
                path = pathMoveTo(mlPosition);
            }else {
                pathLineTo(path,mlPosition);
            }
        }
        if (path!=null){
            path.close();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path,paint);
        }
    }

    private static Path pathMoveTo(MLPosition mlPosition){
        Path path = new Path();
        path.moveTo(mlPosition.getX(),mlPosition.getY());
        return path;
    }

    private static void pathQuadTo(Path path,float x , float y,MLPosition mlPositionS){
        path.quadTo(x,y,mlPositionS.getX(),mlPositionS.getY());
    }

    private static void pathLineTo(Path path, MLPosition mlPosition){
        path.lineTo(mlPosition.getX(),mlPosition.getY());
    }

    private static void cutoutNoseLeftRight(MLFace face, Canvas canvas, Paint paint) {
        Bitmap facePartLeft = BitmapUtils.getImageFromAssetsFile("FacePartLeft.png");
        RectF facePartLeftRectF = new RectF();
        facePartLeftRectF.left = face.getAllPoints().get(47).getX();
        facePartLeftRectF.top = face.getAllPoints().get(51).getY();
        facePartLeftRectF.right = face.getAllPoints().get(311).getX();
        facePartLeftRectF.bottom = face.getAllPoints().get(40).getY();
        canvas.drawBitmap(facePartLeft,null,facePartLeftRectF,paint);

        Bitmap facePartRight = BitmapUtils.getImageFromAssetsFile("FacePartRight.png");
        RectF facePartRightRectF = new RectF();
        facePartRightRectF.left = face.getAllPoints().get(375).getX();
        facePartRightRectF.top = face.getAllPoints().get(113).getY();
        facePartRightRectF.right = face.getAllPoints().get(109).getX();
        facePartRightRectF.bottom = face.getAllPoints().get(102).getY();
        canvas.drawBitmap(facePartRight,null,facePartRightRectF,paint);
    }

    private static void cutoutEyeBottom(MLFace face, Canvas canvas, Paint paint,int oriWidth) {

        MLPosition left1P = face.getAllPoints().get(845);
        MLPosition left2P = face.getAllPoints().get(846);
        float leftCenterX = (left1P.getX()+left2P.getX()) / 2;
        float leftCenterY = (left1P.getY() + left2P.getY())/ 2;
        float leftRadius = Math.abs(left2P.getX()-left1P.getX()) * 0.5f;

        MLPosition right1P = face.getAllPoints().get(847);
        MLPosition right2P = face.getAllPoints().get(848);
        float rightCenterX = (right1P.getX()+right2P.getX()) / 2;
        float rightCenterY = (right1P.getY() + right2P.getY())/ 2;
        float rightRadius = Math.abs(right2P.getX()-right1P.getX()) * 0.5f;

        float leftRect1 = left1P.getX();
        float rightRect1 = left2P.getX() + leftRadius * 0.3f;
        float topRect1 = leftCenterY - leftRadius;//face.getAllPoints().get(252).getY();//leftCenterY - leftRadius;
        float bottomRect1 = face.getAllPoints().get(297).getY();

        float leftWidth = leftRadius * 0.8f;
        leftRect1 = leftRect1 - leftWidth<0?0:leftRect1 - leftWidth;

        float leftRect2 = right1P.getX() - rightRadius * 0.3f;
        float rightRect2 = right2P.getX();
        float topRect2 = rightCenterY - rightRadius;//face.getAllPoints().get(146).getY();//rightCenterY - rightRadius;
        float bottomRect2 = face.getAllPoints().get(361).getY();

        float rightWidth = rightRadius * 0.8f;
        rightRect2 = Math.min(rightRect2 + rightWidth, oriWidth);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawOval(leftRect1,topRect1,rightRect1,bottomRect1,paint);
        canvas.drawOval(leftRect2,topRect2,rightRect2,bottomRect2,paint);
//        canvas.drawCircle(leftCenterX,leftCenterY,leftRadius,paint);
//        canvas.drawCircle(rightCenterX,rightCenterY,rightRadius,paint);


    }

    private static void cutoutEyeBottom(MLFace face, Canvas canvas, Paint paint) {
        RectF eyeRectF = new RectF();
        eyeRectF.left = face.getAllPoints().get(254).getX();
        eyeRectF.top = face.getAllPoints().get(405).getY();//251
        eyeRectF.right = face.getAllPoints().get(144).getX();
        eyeRectF.bottom = face.getAllPoints().get(132).getY();
//        canvas.drawBitmap(middle,null,eyeRectF,paint);
        canvas.drawRect(eyeRectF,paint);

    }

    private static void cutoutMiddle(MLFace face, Canvas canvas, Paint paint) {
        Bitmap middle = BitmapUtils.getImageFromAssetsFile("Middle.png");
        RectF middleRectF = new RectF();
        middleRectF.left = face.getAllPoints().get(48).getX();
        middleRectF.top = face.getAllPoints().get(424).getY();
        middleRectF.right = face.getAllPoints().get(110).getX();
        middleRectF.bottom = face.getAllPoints().get(450).getY();
        canvas.drawBitmap(middle,null,middleRectF,paint);
    }

    private static void cutoutForehead(MLFace face,Canvas canvas,Paint paint){
        Bitmap forehead = BitmapUtils.getImageFromAssetsFile("Forehead.png");
        RectF foreheadRectF = new RectF();
        foreheadRectF.left = face.getAllPoints().get(240).getX();
        foreheadRectF.top = face.getAllPoints().get(200).getY();
        foreheadRectF.right = face.getAllPoints().get(158).getX();
        foreheadRectF.bottom = face.getAllPoints().get(404).getY();
        canvas.drawBitmap(forehead,null,foreheadRectF,paint);
    }


    private static void cutoutT(MLFace face,Canvas canvas,Paint paint){
        Bitmap tForehead = BitmapUtils.getImageFromAssetsFile("TForehead+.png");
        RectF foreheadRectF = new RectF();
        foreheadRectF.left = face.getAllPoints().get(240).getX();
        foreheadRectF.top = face.getAllPoints().get(200).getY();
        foreheadRectF.right = face.getAllPoints().get(158).getX();
        foreheadRectF.bottom = face.getAllPoints().get(403).getY();//399
        Bitmap tNose = BitmapUtils.getImageFromAssetsFile("TNose+.png");
        RectF noseRectF = new RectF();
        noseRectF.left = face.getAllPoints().get(312).getX();
        noseRectF.top = face.getAllPoints().get(399).getY();//401
        noseRectF.right = face.getAllPoints().get(376).getX();
        noseRectF.bottom = face.getAllPoints().get(455).getY();
        canvas.drawBitmap(tForehead,null,foreheadRectF,paint);
        canvas.drawBitmap(tNose,null,noseRectF,paint);
    }

    private static void cutoutLeftRightFace(MLFace face,Canvas canvas,Paint paint){
        Bitmap leftFace = BitmapUtils.getImageFromAssetsFile("LeftFace+.png");
        RectF leftRectF = new RectF();
        leftRectF.left = face.getAllPoints().get(264).getX();
        leftRectF.right = face.getAllPoints().get(846).getX();
        leftRectF.top = face.getAllPoints().get(264).getY();
        leftRectF.bottom = face.getAllPoints().get(35).getY();
        Bitmap rightFace = BitmapUtils.getImageFromAssetsFile("RightFace+.png");
        RectF rightRectF = new RectF();
        rightRectF.right = face.getAllPoints().get(135).getX();
        rightRectF.left = face.getAllPoints().get(847).getX();
        rightRectF.top = face.getAllPoints().get(135).getY();
        rightRectF.bottom = face.getAllPoints().get(97).getY();
        canvas.drawBitmap(rightFace,null,rightRectF,paint);
        canvas.drawBitmap(leftFace,null,leftRectF,paint);
    }

    private static void cutoutNose(MLFace face,Canvas canvas,Paint paint){
        Bitmap tNose = BitmapUtils.getImageFromAssetsFile("Nose.png");
        RectF noseRectF = new RectF();
        noseRectF.left = face.getAllPoints().get(312).getX();
        noseRectF.top = face.getAllPoints().get(418).getY();
        noseRectF.right = face.getAllPoints().get(376).getX();
        noseRectF.bottom = face.getAllPoints().get(455).getY();
        canvas.drawBitmap(tNose,null,noseRectF,paint);
    }

}
