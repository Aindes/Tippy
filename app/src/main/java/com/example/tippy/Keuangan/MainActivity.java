package com.example.tippy.Keuangan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tippy.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tippy.Keuangan.model.TransaksiTemp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnTambahTransaksi;
    private Button btnLihatDaftarTransaksi;
    private ArrayList<TransaksiTemp> transaksiList = new ArrayList<>();
    private FirebaseFirestore db;

    private static final String TAG = "KeuanganMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Fitur Keuangan");
        }

        db = FirebaseFirestore.getInstance();

        btnTambahTransaksi = findViewById(R.id.btn_tambah_transaksi);
        btnLihatDaftarTransaksi = findViewById(R.id.btn_lihat_daftar_transaksi);

        loadTransaksiFromFirestore();

        btnTambahTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TambahTransaksiActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_ADD_TRANSAKSI);
            }
        });

        btnLihatDaftarTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaksiList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Belum ada transaksi ditambahkan.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ListTransaksiActivity.class);
                    intent.putExtra(ListTransaksiActivity.EXTRA_TRANSAKSI_LIST, (Serializable) transaksiList);
                    startActivityForResult(intent, Constants.REQUEST_CODE_LIST_TRANSAKSI);
                }
            }
        });
    }

    private void loadTransaksiFromFirestore() {
        db.collection("transaksi")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            transaksiList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                TransaksiTemp transaksi = document.toObject(TransaksiTemp.class);
                                transaksi.setId(document.getId()); // Set ID dokumen dari Firestore
                                transaksiList.add(transaksi);
                            }
                            Collections.sort(transaksiList, new Comparator<TransaksiTemp>() {
                                @Override
                                public int compare(TransaksiTemp t1, TransaksiTemp t2) {

                                    return t1.getTanggal().compareTo(t2.getTanggal());
                                }
                            });
                            Log.d(TAG, "Transaksi berhasil dimuat dari Firestore. Jumlah: " + transaksiList.size());
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(MainActivity.this, "Gagal memuat data transaksi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_ADD_TRANSAKSI ||
                requestCode == Constants.REQUEST_CODE_LIST_TRANSAKSI) {
            if (resultCode == RESULT_OK || resultCode == Constants.RESULT_CODE_DELETE_TRANSAKSI) {
                loadTransaksiFromFirestore();
                if (requestCode == Constants.REQUEST_CODE_ADD_TRANSAKSI && resultCode == RESULT_OK) {
                    Toast.makeText(this, "Transaksi baru berhasil ditambahkan.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}