package com.example.tippy.Keuangan;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.net.Uri;
import com.bumptech.glide.Glide;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.tippy.R;
import com.bumptech.glide.Glide; // Import Glide

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri"; // Sekarang akan menerima URL Firebase Storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lihat Nota");
        }

        ImageView fullImageView = findViewById(R.id.full_image_view);

        String imageUriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (imageUriString != null && !imageUriString.isEmpty()) {
            // Gunakan Glide untuk memuat gambar dari URL Firebase Storage
            Glide.with(this)
                    .load(imageUriString)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(fullImageView);
        } else {
            Toast.makeText(this, "Gambar tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}