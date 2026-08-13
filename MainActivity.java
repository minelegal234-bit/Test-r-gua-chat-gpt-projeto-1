package com.example.reguaflutuante;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40,40,40,40);

        TextView title = new TextView(this);
        title.setText("Régua Flutuante");
        title.setTextSize(26);
        title.setTextColor(Color.BLACK);
        box.addView(title);

        TextView info = new TextView(this);
        info.setText("\nUma régua redimensionável que pode permanecer sobre outros aplicativos.\n\n" +
                "1. Permita 'Exibir sobre outros apps'.\n" +
                "2. Toque em Iniciar régua.\n" +
                "3. Arraste a régua pelo centro.\n" +
                "4. Use os controles para girar e redimensionar.");
        info.setTextSize(16);
        box.addView(info);

        Button permission = new Button(this);
        permission.setText("Permitir sobre outros apps");
        permission.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        box.addView(permission);

        Button start = new Button(this);
        start.setText("Iniciar régua");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Primeiro permita a sobreposição.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(this, OverlayService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
            Toast.makeText(this, "Régua iniciada.", Toast.LENGTH_SHORT).show();
        });
        box.addView(start);

        Button stop = new Button(this);
        stop.setText("Parar régua");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        box.addView(stop);

        setContentView(box);
    }
}
