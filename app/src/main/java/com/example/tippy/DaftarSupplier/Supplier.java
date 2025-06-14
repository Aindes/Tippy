package com.example.tippy.DaftarSupplier;

import com.google.firebase.firestore.Exclude;

public class Supplier {
    private String documentId;

    private String name;
    private String email;
    private String description;
    private String imageUri;
    public Supplier() {
    }

    public Supplier(String name, String email, String description, String imageUri) {
        this.name = name;
        this.email = email;
        this.description = description;
        this.imageUri = imageUri;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setName(String name) {
        this.name = name;
    }
}