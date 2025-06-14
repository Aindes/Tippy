package com.example.tippy.Pengiriman;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tippy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditPengiriman extends AppCompatActivity {

    private ImageView backButton, uploadImageView;
    private EditText etItemName, etOrderQuantity, etArrivalDateInput;
    private RadioGroup shippingStatusGroup;
    private RadioButton statusNotSent, statusInTransit, statusDelivered;
    private Button saveButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String pengirimanId;
    private Uri newImageUri;
    private String existingImageUrl;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    newImageUri = result.getData().getData();
                    Glide.with(this).load(newImageUri).into(uploadImageView);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pengiriman);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        backButton = findViewById(R.id.backButton);
        uploadImageView = findViewById(R.id.uploadImageView);
        etItemName = findViewById(R.id.itemName);
        etOrderQuantity = findViewById(R.id.orderQuantity);
        etArrivalDateInput = findViewById(R.id.arrivalDateInput);
        shippingStatusGroup = findViewById(R.id.shippingStatusGroup);
        statusNotSent = findViewById(R.id.statusNotSent);
        statusInTransit = findViewById(R.id.statusInTransit);
        statusDelivered = findViewById(R.id.statusDelivered);
        saveButton = findViewById(R.id.saveButton);

        pengirimanId = getIntent().getStringExtra("PENGIRIMAN_ID_EDIT");

        if (pengirimanId == null || pengirimanId.isEmpty()) {
            Toast.makeText(this, "Error: Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadExistingData();

        backButton.setOnClickListener(v -> finish());
        uploadImageView.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> uploadImageAndSaveChanges());
    }

    private void loadExistingData() {
        db.collection("pengiriman").document(pengirimanId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pengiriman pengiriman = documentSnapshot.toObject(Pengiriman.class);
                        if (pengiriman != null) {
                            etItemName.setText(pengiriman.getNamaProduk());
                            etOrderQuantity.setText(pengiriman.getJumlahBarang());
                            etArrivalDateInput.setText(pengiriman.getEstimasiTiba());

                            setShippingStatusToRadioGroup(pengiriman.getStatusPengiriman());

                            existingImageUrl = pengiriman.getUrlBukti();
                            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(existingImageUrl)
                                        .placeholder(R.drawable.outline)
                                        .into(uploadImageView);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Data tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void setShippingStatusToRadioGroup(String status) {
        if (status == null) return;
        if (status.equalsIgnoreCase("Dikemas")) {
            statusNotSent.setChecked(true);
        } else if (status.equalsIgnoreCase("Dikirim")) {
            statusInTransit.setChecked(true);
        } else if (status.equalsIgnoreCase("Selesai")) {
            statusDelivered.setChecked(true);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageAndSaveChanges() {
        if (newImageUri != null) {
            StorageReference fileRef = storage.getReference().child("bukti_pengiriman/" + pengirimanId + "_" + System.currentTimeMillis());

            fileRef.putFile(newImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String newImageUrl = uri.toString();
                                saveChangesToFirestore(newImageUrl);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal upload gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveChangesToFirestore(existingImageUrl);
        }
    }

    private void saveChangesToFirestore(String imageUrl) {
        String namaProduk = etItemName.getText().toString().trim();
        String jumlahBarang = etOrderQuantity.getText().toString().trim();
        String estimasiTiba = etArrivalDateInput.getText().toString().trim();
        String statusPengiriman = getShippingStatusFromRadioGroup();

        if (TextUtils.isEmpty(namaProduk) || TextUtils.isEmpty(jumlahBarang) || TextUtils.isEmpty(estimasiTiba) || statusPengiriman.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("namaProduk", namaProduk);
        updates.put("jumlahBarang", jumlahBarang);
        updates.put("estimasiTiba", estimasiTiba);
        updates.put("statusPengiriman", statusPengiriman);
        updates.put("urlBukti", imageUrl != null ? imageUrl : "");

        db.collection("pengiriman").document(pengirimanId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditPengiriman.this, PengirimanActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan perubahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getShippingStatusFromRadioGroup() {
        int selectedId = shippingStatusGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.statusNotSent) {
            return "Dikemas";
        } else if (selectedId == R.id.statusInTransit) {
            return "Dikirim";
        } else if (selectedId == R.id.statusDelivered) {
            return "Selesai";
        }
        return "";
    }
}