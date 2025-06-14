package com.example.tippy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.tippy.DaftarSupplier.DaftarSupplier;
import com.example.tippy.Pengiriman.PengirimanActivity;
import com.example.tippy.stokBarang.StokActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    CardView daftarSupplierCard, stokBarangCard, pengirimanCard;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.pink_primary));

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        daftarSupplierCard = findViewById(R.id.cardDaftarSupplier);
        stokBarangCard = findViewById(R.id.cardStokBarang);
        pengirimanCard = findViewById(R.id.cardPengiriman);
        daftarSupplierCard.setOnClickListener(v -> {
        });

        daftarSupplierCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DaftarSupplier.class);
            startActivity(intent);
        });

        stokBarangCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StokActivity.class);
            startActivity(intent);
        });

        pengirimanCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PengirimanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}