package com.example.tippy.stokBarang;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tippy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TambahBarangActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etProductName, etProductPrice, etStock;
    private ImageView imgProduct, btnBack;
    private TextView txtTitle;
    private Button btnSave;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri imageUri;
    private String currentImageUrl;
    private boolean isEdit = false;
    private Product currentProduct;
    private int productPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_barang);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.pink));
        }

        initViews();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        checkIntent();
        setupListeners();
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        btnSave = findViewById(R.id.btnSave);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etStock = findViewById(R.id.etStock);
        btnBack = findViewById(R.id.btnBack);
        imgProduct = findViewById(R.id.imgProduct);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("isEdit")) {
            this.isEdit = true;
            this.productPosition = intent.getIntExtra("position", -1);
            this.currentProduct = (Product) intent.getSerializableExtra("product");

            if (this.currentProduct != null) {
                txtTitle.setText("EDIT BARANG");
                populateData();
            } else {
                Toast.makeText(this, "Gagal memuat data produk. Coba lagi.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            this.isEdit = false;
            txtTitle.setText("TAMBAH BARANG");
        }
    }

    private void populateData() {
        etProductName.setText(currentProduct.getName());
        etProductPrice.setText(currentProduct.getPrice());
        etStock.setText(String.valueOf(currentProduct.getStock()));
        currentImageUrl = currentProduct.getImageUrl();

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.add_image)
                    .into(imgProduct);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> openFileChooser());
        findViewById(R.id.btnIncreaseStock).setOnClickListener(v -> updateStock(1));
        findViewById(R.id.btnDecreaseStock).setOnClickListener(v -> updateStock(-1));
    }

    private void updateStock(int amount) {
        try {
            int currentStock = Integer.parseInt(etStock.getText().toString());
            if (currentStock + amount >= 0) {
                etStock.setText(String.valueOf(currentStock + amount));
            }
        } catch (NumberFormatException e) {
            etStock.setText("0");
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgProduct);
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        if (imageUri != null) {
            uploadImageAndSaveData(name, priceStr, stockStr);
        } else {
            saveDataToFirestore(name, priceStr, stockStr, currentImageUrl);
        }
    }

    private void uploadImageAndSaveData(String name, String price, String stock) {
        String fileName = "products/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (isEdit && currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        storage.getReferenceFromUrl(currentImageUrl).delete();
                    }
                    saveDataToFirestore(name, price, stock, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal upload gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
    }

    private void saveDataToFirestore(String name, String price, String stock, String imageUrl) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", name);
        productData.put("price", price);
        productData.put("stock", Integer.parseInt(stock));
        productData.put("imageUrl", imageUrl != null ? imageUrl : "");

        if (isEdit) {
            db.collection("products").document(currentProduct.getId())
                    .update(productData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Produk berhasil diupdate", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        Product updatedProduct = new Product(currentProduct.getId(), name, price, Integer.parseInt(stock), imageUrl);
                        resultIntent.putExtra("updatedProduct", updatedProduct);
                        resultIntent.putExtra("position", productPosition);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resetSaveButton();
                    });
        } else {
            db.collection("products").add(productData).addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetSaveButton();
            });
        }
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");
    }
}