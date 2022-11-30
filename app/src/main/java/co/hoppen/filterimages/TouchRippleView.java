package co.hoppen.filterimages;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ImageUtils;

import java.io.File;

import co.hoppen.filter.utils.BitmapUtils;

/**
 * Created by YangJianHui on 2022/10/24.
 */
public class TouchRippleView extends View {
    private final static float DEFAULT_RADIUS = 0f;
    private Paint paint;
    private Ripple ripple = null;
    private ChangeListener changeListener;

    public void setChangeListener(TouchRippleView.ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public static TouchRippleView create(View view){
        return new TouchRippleView(view.getContext());
    }

    public static TouchRippleView create(Context baseContext){
        return new TouchRippleView(baseContext);
    }

    private final AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (changeListener!=null)changeListener.onChange();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (ripple!=null){
                ripple.rootView.removeView(TouchRippleView.this);
                ripple.rootView = null;
                if (ripple.captureBitmap!=null && !ripple.captureBitmap.isRecycled()){
                    //-------------
                    //ImageUtils.save(ripple.captureBitmap,new File(Environment.getExternalStorageDirectory().getPath() + "/test/888.jpg"), Bitmap.CompressFormat.JPEG);
                    //-------------
                    ripple.captureBitmap.recycle();
                    ripple.captureBitmap = null;
                }
                ripple = null;
            }
        }
    };

    private TouchRippleView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        throw new RuntimeException("Activity not found!");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ripple!=null&&ripple.captureBitmap!=null){
            int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
            canvas.drawBitmap(ripple.captureBitmap,0,0,null);
            canvas.drawCircle(ripple.startX,ripple.startY,ripple.currentRadius,paint);
            canvas.restoreToCount(layer);
        }
    }

    public void start(float startX,float startY){
        if (ripple==null){
            ripple = new Ripple((ViewGroup) getActivityFromContext(getContext()).getWindow().getDecorView(),startX,startY);
            ripple.startAnimator();
        }
    }

    public class Ripple{
        Bitmap captureBitmap;
        float currentRadius = 0;
        float startX;
        float startY;
        ViewGroup rootView;
        ValueAnimator valueAnimator;

        public Ripple(ViewGroup rootView , float startX, float startY) {
            this.rootView = rootView;
            this.startX = startX;
            this.startY = startY;

            RectF leftTop = new RectF(0, 0, startX + DEFAULT_RADIUS, startY + DEFAULT_RADIUS);
            RectF rightTop = new RectF(leftTop.right, 0, rootView.getRight(), leftTop.bottom);
            RectF leftBottom = new RectF(0, leftTop.bottom, leftTop.right, rootView.getBottom());
            RectF rightBottom = new RectF(leftBottom.right, leftTop.bottom, rootView.getRight(), leftBottom.bottom);
            //分别获取对角线长度
            double leftTopHypotenuse = Math.sqrt(Math.pow(leftTop.width(), 2) + Math.pow(leftTop.height(), 2));
            double rightTopHypotenuse = Math.sqrt(Math.pow(rightTop.width(), 2) + Math.pow(rightTop.height(), 2));
            double leftBottomHypotenuse = Math.sqrt(Math.pow(leftBottom.width(), 2) + Math.pow(leftBottom.height(), 2));
            double rightBottomHypotenuse = Math.sqrt(Math.pow(rightBottom.width(), 2) + Math.pow(rightBottom.height(), 2));
            //取最大值
            int maxRadius = (int) Math.max(
                    Math.max(leftTopHypotenuse, rightTopHypotenuse),
                    Math.max(leftBottomHypotenuse, rightBottomHypotenuse));

            valueAnimator = ValueAnimator.ofFloat(0,maxRadius).setDuration(500);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentRadius = (float) animation.getAnimatedValue() + DEFAULT_RADIUS;
                    postInvalidate();
                }
            });
            valueAnimator.addListener(animatorListenerAdapter);
        }

        public void startAnimator(){
            rootView.measure(MeasureSpec.makeMeasureSpec(rootView.getLayoutParams().width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(rootView.getLayoutParams().height, MeasureSpec.EXACTLY));
            Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            rootView.draw(canvas);
            this.captureBitmap = bitmap;

            TouchRippleView.this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rootView.addView(TouchRippleView.this);
            if (valueAnimator!=null)valueAnimator.start();
        }
    }

    public interface ChangeListener{
        void onChange();
    }

}
