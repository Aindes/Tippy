package com.example.tippy.Pengiriman;

import com.google.firebase.firestore.Exclude;

public class Pengiriman {
    private String namaProduk;
    private String jumlahBarang;
    private String estimasiTiba;
    private String statusPengiriman;
    private String urlBukti;
    private String documentId;

    public Pengiriman() {
    }

    public Pengiriman(String namaProduk, String jumlahBarang, String estimasiTiba, String statusPengiriman) {
        this.namaProduk = namaProduk;
        this.jumlahBarang = jumlahBarang;
        this.estimasiTiba = estimasiTiba;
        this.statusPengiriman = statusPengiriman;
    }

    public String getNamaProduk() {
        return namaProduk;
    }
    public String getJumlahBarang() {
        return jumlahBarang;
    }
    public String getEstimasiTiba() {
        return estimasiTiba;
    }
    public String getStatusPengiriman() {
        return statusPengiriman;
    }
    public String getUrlBukti() { return urlBukti; }
    public void setUrlBukti(String urlBukti) { this.urlBukti = urlBukti; }

    @Exclude
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}