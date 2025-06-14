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
import com.bumptech.glide.Glide;
import com.example.tippy.R;
import com.example.tippy.Keuangan.model.TransaksiTemp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class TambahTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSAKSI_JENIS = "extra_transaksi_jenis";
    public static final String EXTRA_TRANSAKSI_JUMLAH = "extra_transaksi_jumlah";
    public static final String EXTRA_TRANSAKSI_DESKRIPSI = "extra_deskripsi_transaksi";
    public static final String EXTRA_TRANSAKSI_TANGGAL = "extra_tanggal_transaksi";
    public static final String EXTRA_PATH_NOTA = "extra_path_nota";

    private Spinner spJenisTransaksi;
    private EditText etJumlah, etDeskripsi;
    private Button btnTanggal;
    private ImageView ivNotaPreview;
    private Button btnUploadNota;
    private Uri selectedNotaUri;
    private String currentNotaUrl;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String currentTransaksiId;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedNotaUri = uri;
                    try {
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
            currentTransaksiId = intent.getStringExtra(Constants.EXTRA_TRANSAKSI_ID);
            String jenis = intent.getStringExtra(EXTRA_TRANSAKSI_JENIS);
            double jumlah = intent.getDoubleExtra(EXTRA_TRANSAKSI_JUMLAH, 0.0);
            String deskripsi = intent.getStringExtra(EXTRA_TRANSAKSI_DESKRIPSI);
            String tanggal = intent.getStringExtra(EXTRA_TRANSAKSI_TANGGAL);
            currentNotaUrl = intent.getStringExtra(EXTRA_PATH_NOTA);

            if (jenis != null) {
                int spinnerPosition = adapter.getPosition(jenis);
                spJenisTransaksi.setSelection(spinnerPosition);
            }

            etJumlah.setText(String.valueOf(jumlah));
            etDeskripsi.setText(deskripsi);
            btnTanggal.setText(tanggal);

            if (currentNotaUrl != null && !currentNotaUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentNotaUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(ivNotaPreview);
                ivNotaPreview.setVisibility(View.VISIBLE);
            } else {
                ivNotaPreview.setVisibility(View.GONE);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
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
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
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
            Toast.makeText(this, "Jumlah tidak boleh kosong.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Deskripsi tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedNotaUri != null) {
            uploadNotaToFirebaseStorage(jenis, jumlah, deskripsi, tanggal);
        } else if (currentNotaUrl != null && !currentNotaUrl.isEmpty() && currentTransaksiId != null) {
            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, currentNotaUrl), currentTransaksiId);
        } else {
            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
        }
    }

    private void uploadNotaToFirebaseStorage(String jenis, double jumlah, String deskripsi, String tanggal) {
        if (selectedNotaUri != null) {
            String fileName = "notas/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference().child(fileName);

            UploadTask uploadTask = storageRef.putFile(selectedNotaUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String notaUrl = downloadUri.toString();
                            Log.d("TambahTransaksiActivity", "Nota uploaded, download URL: " + notaUrl);
                            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, notaUrl), currentTransaksiId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TambahTransaksiActivity", "Failed to get download URL: " + e.getMessage());
                            Toast.makeText(TambahTransaksiActivity.this, "Gagal mendapatkan URL nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("TambahTransaksiActivity", "Failed to upload nota: " + e.getMessage());
                    Toast.makeText(TambahTransaksiActivity.this, "Gagal mengunggah nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveTransaksiToFirestore(new TransaksiTemp(jenis, jumlah, deskripsi, tanggal, null), currentTransaksiId);
                }
            });
        }
    }

    private void saveTransaksiToFirestore(TransaksiTemp transaksi, String id) {
        if (id == null) {
            db.collection("transaksi")
                    .add(transaksi)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TambahTransaksiActivity", "Transaksi berhasil ditambahkan dengan ID: " + documentReference.getId());
                            Toast.makeText(TambahTransaksiActivity.this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
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
        } else {
            db.collection("transaksi").document(id)
                    .set(transaksi)
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