package co.hoppen.filterimages;

import android.graphics.Color;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SnackbarUtils;

import java.util.Random;

/**
 * Created by YangJianHui on 2022/10/26.
 */
public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goneForNavBar();

        setContentView(R.layout.activity_view);

        TouchRippleView touchRippleView = TouchRippleView.create(this);

        touchRippleView.setChangeListener(new TouchRippleView.ChangeListener() {
            @Override
            public void onChange() {
                ((CardView)findViewById(R.id.ll_parent))
                        .setCardBackgroundColor(ColorUtils.getRandomColor());
//                findViewById(R.id.ll_parent).setBackgroundColor(ColorUtils.getRandomColor());
            }
        });

        findViewById(R.id.ll_parent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    touchRippleView.start(event.getX(), event.getY());
//                    SnackbarUtils.with(view)
//                            .setMessage(getString(R.string.app_name))
//                            .setMessageColor(Color.WHITE)
//                            .setDuration(SnackbarUtils.LENGTH_SHORT)
//                            .show();
//                    UsbRequest usbRequest = new UsbRequest();
                    return true;
                }
                return false;
            }
        });
    }

    //全浸式的activity
    private void goneForNavBar(){
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            int changeOptions = uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(changeOptions);
        });
    }

}
