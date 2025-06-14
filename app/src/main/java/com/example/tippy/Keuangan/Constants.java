package com.example.tippy.Keuangan;

public class Constants {
    // Anda bisa mengganti ini jika ingin tetap menggunakan nama variabel Anda
    public static final String EXTRA_NOMINAL = "nominal"; // Tidak digunakan di sini, bisa dihapus jika tidak terpakai
    public static final String EXTRA_TIPE = "tipe";       // Tidak digunakan di sini, bisa dihapus jika tidak terpakai
    public static final String EXTRA_IMAGE_URI = "imageUri"; // Akan digunakan untuk URL nota

    public static final String EXTRA_EDIT_MODE = "editMode";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_TRANSAKSI_ID = "transaksiId"; // <-- Tambahan

    // Request Codes untuk startActivityForResult
    public static final int REQUEST_CODE_ADD_TRANSAKSI = 1;
    public static final int REQUEST_CODE_EDIT_TRANSAKSI = 2;
    public static final int REQUEST_CODE_DETAIL_TRANSAKSI = 3;
    public static final int REQUEST_CODE_LIST_TRANSAKSI = 4;

    // Result Codes kustom (bukan Activity.RESULT_OK/CANCELED)
    public static final int RESULT_CODE_DELETE_TRANSAKSI = 101;
}