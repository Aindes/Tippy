package com.example.tippy.Pengiriman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tippy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PengirimanActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageButton pengAdd;
    private RecyclerView recyclerView;
    private PengirimanAdapter adapter;
    private List<Pengiriman> pengirimanList;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pengiriman);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        btnBack = findViewById(R.id.btnBack);
        pengAdd = findViewById(R.id.pengAdd);
        recyclerView = findViewById(R.id.recyclerView);

        pengirimanList = new ArrayList<>();
        adapter = new PengirimanAdapter(pengirimanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        pengAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PengirimanActivity.this, TambahPengiriman.class);
            startActivity(intent);
        });

        loadDataFromFirestore();
        setupItemClickListener();
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(new PengirimanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Pengiriman pengiriman) {
                Intent intent = new Intent(PengirimanActivity.this, DetailPengiriman.class);
                intent.putExtra("PENGIRIMAN_ID", pengiriman.getDocumentId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Pengiriman pengiriman) {
                showDeleteConfirmationDialog(pengiriman);
            }
        });
    }

    private void showDeleteConfirmationDialog(Pengiriman pengiriman) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus data '" + pengiriman.getNamaProduk() + "'?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    deletePengiriman(pengiriman);
                })
                .setNegativeButton("Tidak", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePengiriman(Pengiriman pengiriman) {
        String documentId = pengiriman.getDocumentId();
        db.collection("pengiriman").document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PengirimanActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();

                    String imageUrl = pengiriman.getUrlBukti();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        deleteImageFromStorage(imageUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PengirimanActivity.this, "Gagal menghapus data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteImageFromStorage(String imageUrl) {
        storage.getReferenceFromUrl(imageUrl).delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    private void loadDataFromFirestore() {
        db.collection("pengiriman")
                .orderBy("namaProduk", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        pengirimanList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Pengiriman pengiriman = doc.toObject(Pengiriman.class);
                            pengiriman.setDocumentId(doc.getId());
                            pengirimanList.add(pengiriman);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
