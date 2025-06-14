package com.example.tippy.Pengiriman;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tippy.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class TambahPengiriman extends AppCompatActivity {

    private ImageView backButton;
    private EditText etNamaProduk, etJumlahBarang, etEstimasiTiba;
    private RadioGroup rgStatus;
    private Button addButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pengiriman);

        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.backButton);
        etNamaProduk = findViewById(R.id.itemName);
        etJumlahBarang = findViewById(R.id.orderQuantity);
        etEstimasiTiba = findViewById(R.id.arrivalDateInput);
        rgStatus = findViewById(R.id.shippingStatusGroup);
        addButton = findViewById(R.id.addButton);

        backButton.setOnClickListener(v -> finish());
        addButton.setOnClickListener(v -> simpanDataPengiriman());
    }

    private void simpanDataPengiriman() {
        String namaProduk = etNamaProduk.getText().toString().trim();
        String jumlahBarang = etJumlahBarang.getText().toString().trim();
        String estimasiTiba = etEstimasiTiba.getText().toString().trim();

        if (TextUtils.isEmpty(namaProduk) || TextUtils.isEmpty(jumlahBarang) || TextUtils.isEmpty(estimasiTiba)) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedStatusId = rgStatus.getCheckedRadioButtonId();
        if (selectedStatusId == -1) {
            Toast.makeText(this, "Pilih status pengiriman", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadioButton = findViewById(selectedStatusId);
        String statusPengiriman = selectedRadioButton.getText().toString();

        Pengiriman pengirimanBaru = new Pengiriman(namaProduk, jumlahBarang, estimasiTiba, statusPengiriman);

        db.collection("pengiriman")
                .add(pengirimanBaru)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TambahPengiriman.this, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TambahPengiriman.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}