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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class TambahSupplier extends AppCompatActivity {
    private static final String TAG = "TambahSupplierActivity";

    private TextInputEditText etNama, etKontak, etDeskripsi;
    private MaterialButton btnUploadImage;
    private ImageView imageSupplierPreview;
    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_supplier);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_tambah_supplier);
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

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
            }
        });

        findViewById(R.id.btn_simpan_supplier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSupplierToFirestore();
            }
        });
    }

    private void saveSupplierToFirestore() {
        String nama = etNama.getText().toString().trim();
        String kontak = etKontak.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (nama.isEmpty() || kontak.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(TambahSupplier.this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(nama, kontak, deskripsi);
        } else {
            Supplier newSupplier = new Supplier(nama, kontak, deskripsi, null);
            addSupplierToFirestore(newSupplier);
        }
    }

    private void uploadImageToFirebaseStorage(String nama, String kontak, String deskripsi) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Tidak ada gambar yang dipilih.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "suppliers/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Supplier newSupplier = new Supplier(nama, kontak, deskripsi, imageUrl);
                        addSupplierToFirestore(newSupplier);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(TambahSupplier.this, "Gagal mendapatkan URL gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error getting download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TambahSupplier.this, "Gagal mengunggah gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error uploading image", e);
                });
    }

    private void addSupplierToFirestore(Supplier supplier) {
        db.collection("suppliers")
                .add(supplier)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TambahSupplier.this, "Supplier berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TambahSupplier.this, "Gagal menambahkan supplier: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error adding document", e);
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
            ActivityCompat.requestPermissions(TambahSupplier.this, new String[]{permission}, REQUEST_PERMISSION);
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
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
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