package com.example.reguaflutuante;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {
    WindowManager wm;
    View ruler;
    WindowManager.LayoutParams params;
    float downX, downY;
    int baseX, baseY;
    float rotation = 0f;
    int widthPx = 700;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(7, notification());

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(12,8,12,8);
        container.setBackgroundColor(0x99000000);

        LinearLayout controls = new LinearLayout(this);
        Button minus = btn("−5°");
        Button plus = btn("+5°");
        Button smaller = btn("−");
        Button bigger = btn("+");
        Button close = btn("×");

        controls.addView(minus); controls.addView(plus);
        controls.addView(smaller); controls.addView(bigger); controls.addView(close);
        container.addView(controls);

        RulerView line = new RulerView(this);
        container.addView(line, new LinearLayout.LayoutParams(widthPx, 55));

        ruler = container;
        int type = Build.VERSION.SDK_INT >= 26 ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                widthPx, 105, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 40; params.y = 250;
        wm.addView(ruler, params);

        View.OnClickListener redraw = v -> { line.invalidate(); };
        minus.setOnClickListener(v -> { rotation -= 5; ruler.setRotation(rotation); });
        plus.setOnClickListener(v -> { rotation += 5; ruler.setRotation(rotation); });
        smaller.setOnClickListener(v -> resize(-80, line));
        bigger.setOnClickListener(v -> resize(80, line));
        close.setOnClickListener(v -> stopSelf());

        container.setOnTouchListener((v,e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                downX=e.getRawX(); downY=e.getRawY();
                baseX=params.x; baseY=params.y; return true;
            }
            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                params.x=baseX+(int)(e.getRawX()-downX);
                params.y=baseY+(int)(e.getRawY()-downY);
                wm.updateViewLayout(ruler,params); return true;
            }
            return true;
        });
    }

    void resize(int delta, View line) {
        widthPx=Math.max(250,Math.min(1400,widthPx+delta));
        params.width=widthPx;
        ruler.getLayoutParams().width=widthPx;
        ruler.requestLayout();
        wm.updateViewLayout(ruler,params);
    }

    Button btn(String s) {
        Button b=new Button(this); b.setText(s); b.setTextSize(12);
        b.setMinWidth(0); b.setPadding(8,0,8,0);
        return b;
    }

    Notification notification() {
        return new Notification.Builder(this,"ruler")
                .setContentTitle("Régua Flutuante")
                .setContentText("Régua ativa sobre outros aplicativos")
                .setSmallIcon(android.R.drawable.ic_menu_crop)
                .build();
    }

    void createChannel() {
        if (Build.VERSION.SDK_INT>=26)
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(new NotificationChannel("ruler","Régua",
                    NotificationManager.IMPORTANCE_LOW));
    }

    @Override public IBinder onBind(Intent i){ return null; }

    @Override public void onDestroy(){
        if(wm!=null && ruler!=null) wm.removeView(ruler);
        super.onDestroy();
    }

    class RulerView extends View {
        android.graphics.Paint p=new android.graphics.Paint(1);
        RulerView(Context c){super(c); p.setColor(Color.WHITE); p.setStrokeWidth(5);}
        protected void onDraw(android.graphics.Canvas c){
            super.onDraw(c);
            float y=getHeight()/2f;
            c.drawLine(10,y,getWidth()-10,y,p);
            p.setStrokeWidth(2);
            for(int x=20;x<getWidth();x+=40)
                c.drawLine(x,y-10,x,y+10,p);
            p.setStrokeWidth(5);
            c.drawCircle(getWidth()/2f,y,7,p);
        }
    }
}
