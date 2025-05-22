package com.example.tippy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    CardView daftarSupplierCard;
    // CardView stokBarangCard, pengirimanCard, keuanganCard; // Masih belum aktif

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        daftarSupplierCard = findViewById(R.id.cardDaftarSupplier);

        daftarSupplierCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // stokBarangCard = findViewById(R.id.cardStokBarang);
        // pengirimanCard = findViewById(R.id.cardPengiriman);
        // keuanganCard = findViewById(R.id.cardKeuangan);

        // stokBarangCard.setOnClickListener(v -> {
        //     // Fitur belum tersedia
        // });

        // pengirimanCard.setOnClickListener(v -> {
        //     // Fitur belum tersedia
        // });

        // keuanganCard.setOnClickListener(v -> {
        //     // Fitur belum tersedia
        // });
    }
}