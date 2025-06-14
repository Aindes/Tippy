package com.example.tippy.DaftarSupplier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tippy.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class DaftarSupplier extends AppCompatActivity {
    private static final String TAG = "DaftarSupplierActivity";

    private RecyclerView recyclerView;
    private SupplierAdapter adapter;
    private List<Supplier> supplierList;
    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;
    private static final int REQUEST_ADD_SUPPLIER = 1;
    private static final int REQUEST_EDIT_SUPPLIER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_supplier);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_daftar_supplier);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerViewSupplier);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        supplierList = new ArrayList<>();
        adapter = new SupplierAdapter(supplierList,
                new SupplierAdapter.OnSupplierUpdateClickListener() {
                    @Override
                    public void onUpdateClick(Supplier supplier, int position) {
                        if (supplier.getDocumentId() == null || supplier.getDocumentId().isEmpty()) {
                            Toast.makeText(DaftarSupplier.this, "Gagal mengedit: ID Supplier tidak ditemukan.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Attempted to edit supplier without a document ID: " + supplier.getName());
                            return;
                        }

                        Intent intent = new Intent(DaftarSupplier.this, EditSupplier.class);
                        intent.putExtra("SUPPLIER_DOCUMENT_ID", supplier.getDocumentId());
                        intent.putExtra("NAMA", supplier.getName());
                        intent.putExtra("KONTAK", supplier.getEmail());
                        intent.putExtra("DESKRIPSI", supplier.getDescription());
                        intent.putExtra("IMAGE_URI", supplier.getImageUri());
                        // Tidak perlu lagi mengirim "POSISI" karena edit via ID
                        startActivityForResult(intent, REQUEST_EDIT_SUPPLIER);
                    }
                },
                new SupplierAdapter.OnSupplierDeleteClickListener() {
                    @Override
                    public void onDeleteClick(int position) {
                        if (position != RecyclerView.NO_POSITION) {
                            Supplier supplierToDelete = supplierList.get(position);
                            if (supplierToDelete.getDocumentId() == null || supplierToDelete.getDocumentId().isEmpty()) {
                                Toast.makeText(DaftarSupplier.this, "Gagal menghapus: ID Supplier tidak ditemukan.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Attempted to delete supplier without a document ID: " + supplierToDelete.getName());
                                return;
                            }
                            deleteSupplierFromFirestore(supplierToDelete);
                        }
                    }
                });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddSupplier = findViewById(R.id.fab_add_supplier);
        fabAddSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DaftarSupplier.this, TambahSupplier.class);
                startActivityForResult(intent, REQUEST_ADD_SUPPLIER);
            }
        });

        loadSuppliersFromFirestore();
    }

    private void loadSuppliersFromFirestore() {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        firestoreListener = db.collection("suppliers")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            Toast.makeText(DaftarSupplier.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            supplierList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Supplier supplier = doc.toObject(Supplier.class);
                                supplier.setDocumentId(doc.getId());
                                supplierList.add(supplier);
                            }
                            adapter.notifyDataSetChanged();
                            if (supplierList.isEmpty()) {
                                Toast.makeText(DaftarSupplier.this, "Tidak ada data supplier.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void deleteSupplierFromFirestore(Supplier supplier) {
        String documentId = supplier.getDocumentId();
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "ID dokumen supplier tidak valid untuk dihapus.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("suppliers").document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DaftarSupplier.this, "Supplier berhasil dihapus", Toast.LENGTH_SHORT).show();

                    if (supplier.getImageUri() != null && supplier.getImageUri().startsWith("gs://")) {

                        Log.d(TAG, "Perintah penghapusan gambar dari Firebase Storage untuk: " + supplier.getImageUri());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DaftarSupplier.this, "Gagal menghapus supplier: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting document: " + e.getMessage(), e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_SUPPLIER) {
                Toast.makeText(this, "Supplier berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_EDIT_SUPPLIER) {
                Toast.makeText(this, "Supplier berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Operasi dibatalkan.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}