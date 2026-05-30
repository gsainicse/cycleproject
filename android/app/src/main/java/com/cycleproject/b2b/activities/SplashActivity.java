package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent intent;
            if (session.isLoggedIn()) {
                if (session.isAdmin()) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, CustomerDashboardActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
