package com.example.tippy.stokBarang;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tippy.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class StokActivity extends AppCompatActivity {

    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int EDIT_PRODUCT_REQUEST = 2;

    private List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private View emptyView;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stok);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.pink));
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupRecyclerView();
        loadProductsFromFirebase();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(StokActivity.this, TambahBarangActivity.class);
            startActivityForResult(intent, ADD_PRODUCT_REQUEST);
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        adapter.setOnDataChangedListener(itemCount -> {
            emptyView.setVisibility(itemCount == 0 ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(itemCount == 0 ? View.GONE : View.VISIBLE);
        });

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product, int position) {
                // Fungsi untuk Edit
                Intent intent = new Intent(StokActivity.this, TambahBarangActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("position", position);
                intent.putExtra("product", product);
                startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
            }

            @Override
            public void onDeleteClick(Product product, int position) {
                showDeleteConfirmation(product, position);
            }
        });
    }

    private void loadProductsFromFirebase() {
        db.collection("products")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }
                        adapter.updateData(productList);
                    } else {
                        Toast.makeText(StokActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmation(Product product, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus " + product.getName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteProduct(product, position))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteProduct(Product product, int position) {
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        StorageReference imageRef = storage.getReferenceFromUrl(product.getImageUrl());
                        imageRef.delete();
                    }
                    adapter.removeProduct(position);
                    Toast.makeText(StokActivity.this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StokActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_PRODUCT_REQUEST) {
                loadProductsFromFirebase();
            } else if (requestCode == EDIT_PRODUCT_REQUEST) {
                int position = data.getIntExtra("position", -1);
                Product updatedProduct = (Product) data.getSerializableExtra("updatedProduct");

                if (position != -1 && updatedProduct != null) {
                    adapter.updateProduct(position, updatedProduct);
                } else {
                    loadProductsFromFirebase();
                }
            }
        }
    }
}