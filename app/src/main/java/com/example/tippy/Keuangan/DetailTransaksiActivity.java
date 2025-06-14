package com.example.tippy.Keuangan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.bumptech.glide.Glide;
import com.example.tippy.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class DetailTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_JENIS_TRANSAKSI = "extra_jenis_transaksi";
    public static final String EXTRA_JUMLAH_TRANSAKSI = "extra_jumlah_transaksi";
    public static final String EXTRA_DESKRIPSI_TRANSAKSI = "extra_deskripsi_transaksi";
    public static final String EXTRA_TANGGAL_TRANSAKSI = "extra_tanggal_transaksi";
    public static final String EXTRA_PATH_NOTA = "extra_path_nota"; // Ini akan jadi URL nota
    public static final String EXTRA_POSITION = "extra_position"; // Posisi di RecyclerView (lokal)

    private TextView tvDetailJenis;
    private TextView tvDetailJumlah;
    private TextView tvDetailDeskripsi;
    private TextView tvDetailTanggal;
    private ImageView ivNotaThumbnailDetail;
    private Button btnEditTransaksi;
    private Button btnHapusTransaksi;

    private String currentNotaPath; // Sekarang ini adalah URL nota dari Firebase Storage
    private int currentPosition;
    private String currentTransaksiId; // ID dokumen dari Firestore

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final String TAG = "DetailTransaksiActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_transaksi);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Transaksi");
        }

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        tvDetailJenis = findViewById(R.id.tv_detail_jenis);
        tvDetailJumlah = findViewById(R.id.tv_detail_jumlah);
        tvDetailDeskripsi = findViewById(R.id.tv_detail_deskripsi);
        tvDetailTanggal = findViewById(R.id.tv_detail_tanggal);
        ivNotaThumbnailDetail = findViewById(R.id.iv_nota_thumbnail_detail);
        btnEditTransaksi = findViewById(R.id.btn_edit_transaksi);
        btnHapusTransaksi = findViewById(R.id.btn_hapus_transaksi);

        Intent intent = getIntent();
        if (intent != null) {
            currentTransaksiId = intent.getStringExtra(Constants.EXTRA_TRANSAKSI_ID); // Ambil ID transaksi
            String jenis = intent.getStringExtra(EXTRA_JENIS_TRANSAKSI);
            double jumlah = intent.getDoubleExtra(EXTRA_JUMLAH_TRANSAKSI, 0.0);
            String deskripsi = intent.getStringExtra(EXTRA_DESKRIPSI_TRANSAKSI);
            String tanggal = intent.getStringExtra(EXTRA_TANGGAL_TRANSAKSI);
            currentNotaPath = intent.getStringExtra(EXTRA_PATH_NOTA); // Ini adalah URL nota
            currentPosition = intent.getIntExtra(EXTRA_POSITION, -1);

            tvDetailJenis.setText(jenis);

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            String formattedJumlah = formatRupiah.format(jumlah);
            tvDetailJumlah.setText(formattedJumlah);

            tvDetailDeskripsi.setText(deskripsi);
            tvDetailTanggal.setText(tanggal);

            if (currentNotaPath != null && !currentNotaPath.isEmpty()) {
                // Gunakan Glide untuk memuat gambar dari URL Firebase Storage
                Glide.with(this)
                        .load(currentNotaPath)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(ivNotaThumbnailDetail);
                ivNotaThumbnailDetail.setVisibility(View.VISIBLE);

                // Tambahkan OnClickListener untuk melihat gambar penuh
                ivNotaThumbnailDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageViewerIntent = new Intent(DetailTransaksiActivity.this, ImageViewerActivity.class);
                        imageViewerIntent.putExtra(Constants.EXTRA_IMAGE_URI, currentNotaPath); // Kirim URL nota
                        startActivity(imageViewerIntent);
                    }
                });
            } else {
                ivNotaThumbnailDetail.setVisibility(View.GONE);
            }
        }

        btnEditTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(DetailTransaksiActivity.this, TambahTransaksiActivity.class);

                // Kirim ID transaksi untuk mode edit
                editIntent.putExtra(Constants.EXTRA_TRANSAKSI_ID, currentTransaksiId);
                editIntent.putExtra(TambahTransaksiActivity.EXTRA_TRANSAKSI_JENIS, tvDetailJenis.getText().toString());

                double jumlahTanpaFormat;
                try {
                    NumberFormat formatRupiahParser = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                    jumlahTanpaFormat = formatRupiahParser.parse(tvDetailJumlah.getText().toString()).doubleValue();
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing number for edit: " + e.getMessage());
                    jumlahTanpaFormat = 0.0;
                }
                editIntent.putExtra(TambahTransaksiActivity.EXTRA_TRANSAKSI_JUMLAH, jumlahTanpaFormat);
                editIntent.putExtra(TambahTransaksiActivity.EXTRA_TRANSAKSI_DESKRIPSI, tvDetailDeskripsi.getText().toString());
                editIntent.putExtra(TambahTransaksiActivity.EXTRA_TRANSAKSI_TANGGAL, tvDetailTanggal.getText().toString());
                editIntent.putExtra(TambahTransaksiActivity.EXTRA_PATH_NOTA, currentNotaPath); // Kirim URL nota yang ada
                editIntent.putExtra(Constants.EXTRA_EDIT_MODE, true);
                editIntent.putExtra(Constants.EXTRA_POSITION, currentPosition); // Posisi lokal

                startActivityForResult(editIntent, Constants.REQUEST_CODE_EDIT_TRANSAKSI);
            }
        });

        btnHapusTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DetailTransaksiActivity.this)
                        .setTitle("Hapus Transaksi")
                        .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                        .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteTransaksiFromFirestore();
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
    }

    private void deleteTransaksiFromFirestore() {
        if (currentTransaksiId != null) {
            db.collection("transaksi").document(currentTransaksiId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Dokumen transaksi berhasil dihapus dari Firestore.");
                            // Jika ada nota, hapus juga dari Storage
                            if (currentNotaPath != null && !currentNotaPath.isEmpty()) {
                                deleteNotaFromFirebaseStorage(currentNotaPath);
                            } else {
                                // Jika tidak ada nota, langsung kirim hasil dan selesai
                                sendDeleteResultAndFinish();
                            }
                            Toast.makeText(DetailTransaksiActivity.this, "Transaksi berhasil dihapus!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error deleting dokumen transaksi dari Firestore", e);
                            Toast.makeText(DetailTransaksiActivity.this, "Gagal menghapus transaksi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteNotaFromFirebaseStorage(String notaUrl) {
        try {
            // Dapatkan referensi storage dari URL
            StorageReference photoRef = storage.getReferenceFromUrl(notaUrl);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Nota berhasil dihapus dari Firebase Storage.");
                    sendDeleteResultAndFinish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "Error menghapus nota dari Firebase Storage: " + exception.getMessage());
                    // Tetap kirim hasil delete ke ListTransaksiActivity meskipun gagal menghapus nota
                    sendDeleteResultAndFinish();
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Firebase Storage URL for deletion: " + e.getMessage());
            sendDeleteResultAndFinish(); // Lanjut meskipun URL tidak valid atau format salah
        }
    }

    private void sendDeleteResultAndFinish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.EXTRA_POSITION, currentPosition);
        resultIntent.putExtra(Constants.EXTRA_TRANSAKSI_ID, currentTransaksiId); // Kirim ID yang dihapus
        setResult(Constants.RESULT_CODE_DELETE_TRANSAKSI, resultIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_EDIT_TRANSAKSI && resultCode == Activity.RESULT_OK && data != null) {
            // Setelah edit, DetailTransaksiActivity perlu diperbarui atau ditutup
            // Karena data akan dimuat ulang di ListTransaksiActivity, cukup tutup DetailTransaksiActivity ini
            // dan set result OK agar ListTransaksiActivity tahu ada perubahan
            setResult(Activity.RESULT_OK); // Beri sinyal ke ListTransaksiActivity bahwa ada perubahan
            finish(); // Tutup DetailTransaksiActivity
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}