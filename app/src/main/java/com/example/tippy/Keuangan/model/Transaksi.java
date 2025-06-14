package com.example.tippy.Keuangan.model;
import java.io.Serializable;

public class Transaksi implements Serializable {
    private String jenis;
    private double jumlah;
    private String deskripsi;
    private String tanggal;
    private String notaPath;

    public Transaksi(String jenis, double jumlah, String deskripsi, String tanggal, String notaPath) {
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.notaPath = notaPath;
    }

    // Getters
    public String getJenis() {
        return jenis;
    }

    public double getJumlah() {
        return jumlah;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getNotaPath() {
        return notaPath;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public void setNotaPath(String notaPath) {
        this.notaPath = notaPath;
    }
}
