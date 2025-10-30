package com.example.crudprodukapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class TambahEditActivity extends AppCompatActivity {

    EditText etNama, etHarga;
    Button btnSimpan;
    ImageView imgPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_edit);

        etNama = findViewById(R.id.etNama);
        etHarga = findViewById(R.id.etHarga);
        btnSimpan = findViewById(R.id.btnSimpan);
        imgPreview = findViewById(R.id.imgPreview);

        btnSimpan.setOnClickListener(v -> simpanProduk());
    }

    private void simpanProduk() {
        String nama = etNama.getText().toString();
        String harga = etHarga.getText().toString();

        if (nama.isEmpty() || harga.isEmpty()) {
            Toast.makeText(this, "Nama dan harga wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> data = new HashMap<>();
        data.put("action", "tambah");
        data.put("nama_produk", nama);
        data.put("harga", harga);

        Call<Map<String, Object>> call = api.tambahProduk(data);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TambahEditActivity.this, "Berhasil tambah produk", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TambahEditActivity.this, "Gagal tambah produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(TambahEditActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
