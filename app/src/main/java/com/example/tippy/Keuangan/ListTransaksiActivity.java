package com.example.tippy.Keuangan;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import com.example.tippy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tippy.Keuangan.adapter.TransaksiAdapter;
import com.example.tippy.Keuangan.model.TransaksiTemp; // Menggunakan TransaksiTemp

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSAKSI_LIST = "extra_transaksi_list"; // Untuk initial load dari MainActivity

    private RecyclerView recyclerViewTransaksi;
    private TransaksiAdapter transaksiAdapter;
    private List<TransaksiTemp> transaksiList;
    private TextView tvEmptyState;

    private FirebaseFirestore db; // Objek Firebase Firestore

    private static final String TAG = "ListTransaksiActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transaksi);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Daftar Transaksi");
        }

        // Inisialisasi Firebase Firestore
        db = FirebaseFirestore.getInstance();

        recyclerViewTransaksi = findViewById(R.id.recycler_view_transaksi);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        recyclerViewTransaksi.setLayoutManager(new LinearLayoutManager(this));

        transaksiList = new ArrayList<>(); // Akan diisi dari Firestore

        // Ambil list transaksi dari Intent (dikirim dari MainActivity)
        List<TransaksiTemp> initialList = (ArrayList<TransaksiTemp>) getIntent().getSerializableExtra(EXTRA_TRANSAKSI_LIST);
        if (initialList != null) {
            transaksiList.addAll(initialList);
        }

        updateEmptyStateVisibility();

        transaksiAdapter = new TransaksiAdapter(this, transaksiList, new TransaksiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TransaksiTemp transaksi, int position) {
                Intent detailIntent = new Intent(ListTransaksiActivity.this, DetailTransaksiActivity.class);
                detailIntent.putExtra(Constants.EXTRA_TRANSAKSI_ID, transaksi.getId()); // Kirim ID dokumen Firebase
                detailIntent.putExtra(DetailTransaksiActivity.EXTRA_JENIS_TRANSAKSI, transaksi.getJenis());
                detailIntent.putExtra(DetailTransaksiActivity.EXTRA_JUMLAH_TRANSAKSI, transaksi.getJumlah());
                detailIntent.putExtra(DetailTransaksiActivity.EXTRA_DESKRIPSI_TRANSAKSI, transaksi.getDeskripsi());
                detailIntent.putExtra(DetailTransaksiActivity.EXTRA_TANGGAL_TRANSAKSI, transaksi.getTanggal());
                detailIntent.putExtra(DetailTransaksiActivity.EXTRA_PATH_NOTA, transaksi.getNotaPath()); // Kirim URL nota
                detailIntent.putExtra(Constants.EXTRA_POSITION, position); // Posisi di list lokal

                startActivityForResult(detailIntent, Constants.REQUEST_CODE_DETAIL_TRANSAKSI);
            }
        });
        recyclerViewTransaksi.setAdapter(transaksiAdapter);

        // Muat data dari Firestore saat activity dibuat (atau dilanjutkan)
        loadTransaksiFromFirestore();
    }

    private void loadTransaksiFromFirestore() {
        db.collection("transaksi")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            transaksiList.clear(); // Bersihkan list yang sudah ada
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                TransaksiTemp transaksi = document.toObject(TransaksiTemp.class);
                                transaksi.setId(document.getId()); // Set ID dokumen dari Firestore
                                transaksiList.add(transaksi);
                            }
                            // Urutkan transaksi (misalnya berdasarkan tanggal)
                            Collections.sort(transaksiList, new Comparator<TransaksiTemp>() {
                                @Override
                                public int compare(TransaksiTemp t1, TransaksiTemp t2) {
                                    // Asumsi tanggal dalam format "dd MMMM yyyy"
                                    // Anda mungkin perlu DateFormat untuk perbandingan yang lebih tepat
                                    return t1.getTanggal().compareTo(t2.getTanggal());
                                }
                            });
                            transaksiAdapter.notifyDataSetChanged(); // Beritahu adapter bahwa data berubah
                            updateEmptyStateVisibility();
                            Log.d(TAG, "Transaksi berhasil dimuat dari Firestore di ListTransaksiActivity. Jumlah: " + transaksiList.size());
                        } else {
                            Log.w(TAG, "Error getting documents in ListTransaksiActivity.", task.getException());
                            Toast.makeText(ListTransaksiActivity.this, "Gagal memuat data transaksi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_DETAIL_TRANSAKSI) {
            // Setelah kembali dari DetailTransaksiActivity (baik hapus atau edit),
            // kita perlu memuat ulang data dari Firestore untuk memastikan data terbaru.
            // Ini lebih robust daripada mencoba mengupdate/menghapus item secara lokal.
            loadTransaksiFromFirestore();
            if (resultCode == Constants.RESULT_CODE_DELETE_TRANSAKSI) {
                Toast.makeText(this, "Transaksi berhasil dihapus.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Transaksi berhasil diperbarui.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateEmptyStateVisibility() {
        if (transaksiList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewTransaksi.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewTransaksi.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}