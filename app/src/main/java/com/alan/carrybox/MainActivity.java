package com.alan.carrybox;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        HashMap<Integer, Class<? extends Activity>> hashMap = new HashMap<>() ;
        hashMap.put(R.id.perm, AppPermissionSettingsActivity.class);
        hashMap.put(R.id.button_show, DynamicButtonActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hashMap.forEach((k,v) -> {
                Button button = root.findViewById(k);
                button.setOnClickListener((ob) -> {
                    Intent intent = new Intent(this.getApplicationContext(), v);
                    this.startActivity(intent);
                });
            });
        }
    }
}