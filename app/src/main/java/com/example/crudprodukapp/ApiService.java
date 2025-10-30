package com.example.crudprodukapp;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // 🔹 READ - Ambil semua produk
    @GET("api.php?action=get")
    Call<List<Produk>> getProduk();

    // 🔹 CREATE - Tambah produk
    @FormUrlEncoded
    @POST("api.php")
    Call<Map<String, Object>> tambahProduk(@FieldMap Map<String, String> fields);

    // 🔹 UPDATE - Edit produk
    @FormUrlEncoded
    @POST("api.php")
    Call<Map<String, Object>> updateProduk(@FieldMap Map<String, String> fields);

    // 🔹 DELETE - Hapus produk
    @FormUrlEncoded
    @POST("api.php")
    Call<Map<String, Object>> hapusProduk(@FieldMap Map<String, String> fields);
}
