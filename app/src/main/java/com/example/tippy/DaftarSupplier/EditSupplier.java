package com.example.tippy.DaftarSupplier;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.tippy.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditSupplier extends AppCompatActivity {
    private static final String TAG = "EditSupplierActivity";

    private TextInputEditText etNama, etKontak, etDeskripsi;
    private MaterialButton btnUploadImage;
    private ImageView imageSupplierPreview;
    private Uri selectedImageUri;
    private String currentImageUrl;
    private String supplierDocumentId;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_supplier);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_edit_supplier);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etNama = findViewById(R.id.et_nama_supplier);
        etKontak = findViewById(R.id.et_kontak_supplier);
        etDeskripsi = findViewById(R.id.et_deskripsi_supplier);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        imageSupplierPreview = findViewById(R.id.image_supplier_preview);

        Intent intent = getIntent();
        if (intent != null) {
            supplierDocumentId = intent.getStringExtra("SUPPLIER_DOCUMENT_ID");
            etNama.setText(intent.getStringExtra("NAMA"));
            etKontak.setText(intent.getStringExtra("KONTAK"));
            etDeskripsi.setText(intent.getStringExtra("DESKRIPSI"));
            currentImageUrl = intent.getStringExtra("IMAGE_URI");

            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentImageUrl)
                        .placeholder(R.drawable.ic_supplier)
                        .error(R.drawable.ic_supplier)
                        .into(imageSupplierPreview);
            } else {
                imageSupplierPreview.setImageResource(R.drawable.ic_supplier);
            }
        }

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
            }
        });

        findViewById(R.id.btn_update_supplier).setOnClickListener(v -> {
            updateSupplierInFirestore();
        });
    }

    private void updateSupplierInFirestore() {
        String updatedNama = etNama.getText().toString().trim();
        String updatedKontak = etKontak.getText().toString().trim();
        String updatedDeskripsi = etDeskripsi.getText().toString().trim();

        if (updatedNama.isEmpty() || updatedKontak.isEmpty() || updatedDeskripsi.isEmpty()) {
            Toast.makeText(EditSupplier.this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (supplierDocumentId == null || supplierDocumentId.isEmpty()) {
            Toast.makeText(this, "ID Supplier tidak ditemukan untuk pembaruan.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadNewImageAndUpdateSupplier(updatedNama, updatedKontak, updatedDeskripsi);
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", updatedNama);
            updates.put("email", updatedKontak);
            updates.put("description", updatedDeskripsi);
            performFirestoreUpdate(updates);
        }
    }

    private void uploadNewImageAndUpdateSupplier(String updatedNama, String updatedKontak, String updatedDeskripsi) {
        if (selectedImageUri == null) return;

        if (currentImageUrl != null && !currentImageUrl.isEmpty() && currentImageUrl.startsWith("gs://")) {
            try {
                StorageReference oldImageRef = storage.getReferenceFromUrl(currentImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Gambar lama berhasil dihapus dari Storage.");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal menghapus gambar lama dari Storage: " + e.getMessage());
                });
            } catch (Exception e) {
                Log.e(TAG, "Error creating StorageReference from old image URL: " + e.getMessage());
            }
        }

        String fileName = "suppliers/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference newImageRef = storage.getReference().child(fileName);

        newImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    newImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageUrl = uri.toString();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", updatedNama);
                        updates.put("email", updatedKontak);
                        updates.put("description", updatedDeskripsi);
                        updates.put("imageUri", newImageUrl);
                        performFirestoreUpdate(updates);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(EditSupplier.this, "Gagal mendapatkan URL gambar baru: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error getting new download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditSupplier.this, "Gagal mengunggah gambar baru: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error uploading new image", e);
                });
    }

    private void performFirestoreUpdate(Map<String, Object> updates) {
        db.collection("suppliers").document(supplierDocumentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditSupplier.this, "Supplier berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditSupplier.this, "Gagal memperbarui supplier: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error updating document", e);
                });
    }

    private void checkAndRequestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditSupplier.this, new String[]{permission}, REQUEST_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imageSupplierPreview.setImageURI(selectedImageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Izin dibutuhkan untuk memilih gambar.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}