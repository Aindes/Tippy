package com.example.tippy.Keuangan;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;
import com.bumptech.glide.Glide; // Pastikan ini ada
import com.example.tippy.R;
import com.example.tippy.Keuangan.model.TransaksiTemp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask; // Pastikan ini ada

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

// Hapus impor ini karena tidak digunakan secara langsung
// import com.google.firebase.storage.StorageTask;

public class TambahTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSAKSI_JENIS = "extra_transaksi_jenis";
    public static final String EXTRA_TRANSAKSI_JUMLAH = "extra_transaksi_jumlah";
    public static final String EXTRA_TRANSAKSI_DESKRIPSI = "extra_deskripsi_transaksi";
    public static final String EXTRA_TRANSAKSI_TANGGAL = "extra_tanggal_transaksi";
    public static final String EXTRA_PATH_NOTA = "extra_path_nota"; // Ini akan jadi URL nota dari Storage

    private Spinner spJenisTransaksi;
    private EditText etJumlah, etDeskripsi;
    private Button btnTanggal;
    private ImageView ivNotaPreview;
    private Button btnUploadNota;
    private Uri selectedNotaUri; // URI lokal saat memilih gambar
    private String currentNotaUrl; // URL dari Firebase Storage jika ada

    private FirebaseFirestore db; // Objek Firebase Firestore
    private FirebaseStorage storage; // Objek Firebase Storage
    private String currentTransaksiId; // ID dokumen transaksi jika dalam mode edit

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedNotaUri = uri;
                    try {
                        // Ambil izin persisten untuk URI agar bisa diakses nanti
                        // Ini penting agar URI tetap valid setelah aplikasi ditutup
                        getContentResolver().takePersistableUriPermission(selectedNotaUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Log.d("TambahTransaksiActivity", "Persistable URI permission taken for: " + selectedNotaUri.toString());

                        InputStream inputStream = getContentResolver().openInputStream(selectedNotaUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivNotaPreview.setImageBitmap(bitmap);
                        ivNotaPreview.setVisibility(View.VISIBLE);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (SecurityException e) {
                        Log.e("TambahTransaksiActivity", "SecurityException taking persistable URI permission or loading image: " + e.getMessage());
                        Toast.makeText(TambahTransaksiActivity.this, "Gagal mendapatkan izin untuk mengakses gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        selectedNotaUri = null;
                    } catch (Exception e) {
                        Log.e("TambahTransaksiActivity", "Error loading image: " + e.getMessage());
                        Toast.makeText(TambahTransaksiActivity.this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        selectedNotaUri = null;
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(TambahTransaksiActivity.this, "Tidak ada gambar yang dipilih.", Toast.LENGTH_SHORT).show();
                    selectedNotaUri = null;
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tambah Transaksi Baru");
        }

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        spJenisTransaksi = findViewById(R.id.sp_jenis_transaksi);
        etJumlah = findViewById(R.id.et_jumlah);
        etDeskripsi = findViewById(R.id.et_deskripsi);
        btnTanggal = findViewById(R.id.btn_tanggal);
        ivNotaPreview = findViewById(R.id.iv_preview_nota);
        btnUploadNota = findViewById(R.id.btn_upload_nota);
        Button btnSimpan = findViewById(R.id.btn_simpan_transaksi);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.jenis_transaksi_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJenisTransaksi.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(Constants.EXTRA_EDIT_MODE, false)) {
            getSupportActionBar().setTitle("Edit Transaksi");
            currentTransaksiId = intent.getStringExtra(Constants.EXTRA_TRANSAKSI_ID); // Ambil ID transaksi
            String jenis = intent.getStringExtra(EXTRA_TRANSAKSI_JENIS);
            double jumlah = intent.getDoubleExtra(EXTRA_TRANSAKSI_JUMLAH, 0.0);
            String deskripsi = intent.getStringExtra(EXTRA_TRANSAKSI_DESKRIPSI);
            String tanggal = intent.getStringExtra(EXTRA_TRANSAKSI_TANGGAL);
            currentNotaUrl = intent.getStringExtra(EXTRA_PATH_NOTA); // Ini adalah URL dari Firebase Storage

            if (jenis != null) {
                int spinnerPosition = adapter.getPosition(jenis);
                spJenisTransaksi.setSelection(spinnerPosition);
            }

            etJumlah.setText(String.valueOf(jumlah));
            etDeskripsi.setText(deskripsi);
            btnTanggal.setText(tanggal);

            if (currentNotaUrl != null && !currentNotaUrl.isEmpty()) {
                // Memuat gambar dari URL Firebase Storage menggunakan Glide
                Glide.with(this)
                        .load(currentNotaUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        // .error(R.drawable.ic_error_image) // Hapus atau tambahkan ic_error_image jika belum ada
                        .into(ivNotaPreview);
                ivNotaPreview.setVisibility(View.VISIBLE);
                // Penting: selectedNotaUri tetap null atau hanya untuk URI lokal baru yang dipilih
                // Jangan set selectedNotaUri dengan URL string karena itu bukan URI lokal
            } else {
                ivNotaPreview.setVisibility(View.GONE);
            }
        } else {
            // Mode tambah baru, set tanggal ke tanggal hari ini
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")); // Pastikan format yyyy
            String currentDate = sdf.format(calendar.getTime());
            btnTanggal.setText(currentDate);
        }

        btnTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnUploadNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/*");
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanTransaksi();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")); // Pastikan format yyyy
                    btnTanggal.setText(sdf.format(selectedDate.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void simpanTransaksi() {
        String jenis = spJenisTransaksi.getSelectedItem().toString();
        String jumlahStr = etJumlah.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String tanggal = btnTanggal.getText().toString();

        if (TextUtils.isEmpty(jumlahStr)) {
            etJumlah.setError("Jumlah tidak boleh kosong");
            Toast.makeText(this, "Jumlah tidak boleh kosong.", Toast.LENGTH_SHORT).show(); // Tambahkan Toast untuk user feedback
            return;
        }

        double jumlah;
        try {
            jumlah = Double.parseDouble(jumlahStr);
        } catch (NumberFormatException e) {
            etJumlah.setError("Masukkan jumlah yang valid (angka)");
            Toast.makeText(this, "Jumlah harus berupa angka yang valid.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(deskripsi)) {
            etDeskripsi.setError("Deskripsi tidak boleh kosong");
            Toast.makeText(this, "Deskripsi tidak boleh kosong.", Toast.LENGTH_SHORT).show(); // Tambahkan Toast untuk user feedback
            return;
        }

        if (selectedNotaUri != null) {
            // Jika ada gambar baru yang dipilih, unggah dulu
            uploadNotaToFirebaseStorage(jenis, jumlah, deskripsi, tanggal);
        } else if (currentNotaUrl != null && !currentNotaUrl.isEmpty() && currentTransaksiId != null) {
            // Jika tidak ada gambar baru yang dipilih, tapi sudah ada nota sebelumnya (mode edit)
            // Simpan transaksi dengan URL nota yang lama
            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, currentNotaUrl), currentTransaksiId);
        } else {
            // Jika tidak ada nota sama sekali (baik baru maupun lama)
            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
        }
    }

    private void uploadNotaToFirebaseStorage(String jenis, double jumlah, String deskripsi, String tanggal) {
        if (selectedNotaUri != null) {
            // Buat nama file unik menggunakan UUID
            String fileName = "notas/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference().child(fileName);

            UploadTask uploadTask = storageRef.putFile(selectedNotaUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { // Ini adalah impor dan penggunaan yang benar
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Setelah sukses upload, dapatkan URL unduhnya
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String notaUrl = downloadUri.toString();
                            Log.d("TambahTransaksiActivity", "Nota uploaded, download URL: " + notaUrl);
                            // Simpan transaksi ke Firestore dengan URL nota
                            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, notaUrl), currentTransaksiId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TambahTransaksiActivity", "Failed to get download URL: " + e.getMessage());
                            Toast.makeText(TambahTransaksiActivity.this, "Gagal mendapatkan URL nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // Tetap simpan transaksi tanpa nota jika gagal mendapatkan URL
                            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("TambahTransaksiActivity", "Failed to upload nota: " + e.getMessage());
                    Toast.makeText(TambahTransaksiActivity.this, "Gagal mengunggah nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Tetap simpan transaksi tanpa nota jika gagal mengunggah
                    saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
                }
            });
        }
    }

    private void saveTransaksiToFirestore(TransaksiTemp transaksi, String id) {
        if (id == null) { // Mode tambah baru (ID null)
            db.collection("transaksi")
                    .add(transaksi) // Firestore akan menghasilkan ID otomatis
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TambahTransaksiActivity", "Transaksi berhasil ditambahkan dengan ID: " + documentReference.getId());
                            Toast.makeText(TambahTransaksiActivity.this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK); // Beritahu Activity pemanggil bahwa sukses
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TambahTransaksiActivity", "Error adding transaksi", e);
                            Toast.makeText(TambahTransaksiActivity.this, "Gagal menyimpan transaksi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else { // Mode edit (ID sudah ada)
            db.collection("transaksi").document(id)
                    .set(transaksi) // Menggunakan set untuk update data di dokumen dengan ID spesifik
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TambahTransaksiActivity", "Transaksi berhasil diperbarui dengan ID: " + id);
                            Toast.makeText(TambahTransaksiActivity.this, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TambahTransaksiActivity", "Error updating transaksi", e);
                            Toast.makeText(TambahTransaksiActivity.this, "Gagal memperbarui transaksi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}