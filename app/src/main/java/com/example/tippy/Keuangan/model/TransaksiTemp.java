package com.example.tippy.Keuangan.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class TransaksiTemp implements Serializable {
    private String id;
    private String jenis;
    private double jumlah;
    private String deskripsi;
    private String tanggal;
    private String notaPath;

    public TransaksiTemp() {
    }

    public TransaksiTemp(String jenis, double jumlah, String deskripsi, String tanggal, String notaPath) {
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.notaPath = notaPath;
    }

    public TransaksiTemp(String id, String jenis, double jumlah, String deskripsi, String tanggal, String notaPath) {
        this.id = id;
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.notaPath = notaPath;
    }

    // Getter dan Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("jenis")
    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    @PropertyName("jumlah")
    public double getJumlah() {
        return jumlah;
    }

    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    @PropertyName("deskripsi")
    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @PropertyName("tanggal")
    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    @PropertyName("notaPath")
    public String getNotaPath() {
        return notaPath;
    }

    public void setNotaPath(String notaPath) {
        this.notaPath = notaPath;
    }
}