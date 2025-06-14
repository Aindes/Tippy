package com.example.tippy.Pengiriman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tippy.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailPengiriman extends AppCompatActivity {

    private ImageView backButton, proofImageView;
    private TextView itemNameDetail, quantityValue, arrivalDateValue, shippingStatusValue, noProofTextView;
    private Button editButton;
    private FirebaseFirestore db;
    private String pengirimanId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengiriman);

        initViews();
        initFirebase();

        pengirimanId = getIntent().getStringExtra("PENGIRIMAN_ID");

        if (pengirimanId == null || pengirimanId.isEmpty()) {
            Toast.makeText(this, "Error: Data pengiriman tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetailData();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        itemNameDetail = findViewById(R.id.itemNameDetail);
        quantityValue = findViewById(R.id.quantityValue);
        arrivalDateValue = findViewById(R.id.arrivalDateValue);
        shippingStatusValue = findViewById(R.id.shippingStatusValue);
        proofImageView = findViewById(R.id.proofImageView);
        noProofTextView = findViewById(R.id.noProofTextView);
        editButton = findViewById(R.id.editButton);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPengiriman.this, EditPengiriman.class);
            intent.putExtra("PENGIRIMAN_ID_EDIT", pengirimanId);
            startActivity(intent);
        });
    }

    private void loadDetailData() {
        DocumentReference docRef = db.collection("pengiriman").document(pengirimanId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Pengiriman pengiriman = documentSnapshot.toObject(Pengiriman.class);
                if (pengiriman != null) {
                    populateUI(pengiriman);
                }
            } else {
                Toast.makeText(this, "Data tidak ditemukan.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void populateUI(Pengiriman pengiriman) {
        itemNameDetail.setText(pengiriman.getNamaProduk());
        quantityValue.setText(pengiriman.getJumlahBarang());
        arrivalDateValue.setText(pengiriman.getEstimasiTiba());
        shippingStatusValue.setText(pengiriman.getStatusPengiriman());

        String status = pengiriman.getStatusPengiriman();

        if ("Selesai".equalsIgnoreCase(status)) {
            String urlBukti = pengiriman.getUrlBukti();
            if (urlBukti != null && !urlBukti.isEmpty()) {
                proofImageView.setVisibility(View.VISIBLE);
                noProofTextView.setVisibility(View.GONE);

                Glide.with(this)
                        .load(urlBukti)
                        .placeholder(R.drawable.outline)
                        .error(R.drawable.ic_back)
                        .into(proofImageView);
            } else {
                proofImageView.setVisibility(View.GONE);
                noProofTextView.setVisibility(View.VISIBLE);
            }
        } else {
            proofImageView.setVisibility(View.GONE);
            noProofTextView.setVisibility(View.VISIBLE);
        }
    }
}