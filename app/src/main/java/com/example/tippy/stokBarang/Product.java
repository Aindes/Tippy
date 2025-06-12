package com.example.tippy.stokBarang;

import android.net.Uri;
import java.io.Serializable;

public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String price;
    private int stock;
    private String imageUrl;

    private transient Uri localImageUri;

    public Product() {
        // Diperlukan oleh Firestore
    }

    // Constructor untuk data baru dari aplikasi (sebelum ke Firestore)
    public Product(String name, String price, int stock, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    // Constructor lengkap (berguna saat mengambil data dari Firestore atau update)
    public Product(String id, String name, String price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    // Constructor dengan URI lokal (tetap ada untuk logika upload)
    public Product(String name, String price, int stock, Uri localImageUri) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.localImageUri = localImageUri;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public Uri getLocalImageUri() { return localImageUri; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLocalImageUri(Uri localImageUri) { this.localImageUri = localImageUri; }
}