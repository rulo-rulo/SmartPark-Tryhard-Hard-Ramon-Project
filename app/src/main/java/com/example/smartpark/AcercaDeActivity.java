package com.example.smartpark;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;



import android.os.Bundle;


public class AcercaDeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.acerca);
    }
}