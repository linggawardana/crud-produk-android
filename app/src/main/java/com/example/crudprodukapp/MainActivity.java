package com.example.crudprodukapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView rvProduk;
    TextView tvStatus;
    Button btnTambah;
    ProdukAdapter adapter;
    ApiService api;

    private String fotoBase64 = ""; // 🔹 simpan foto base64 sementara
    private ImageView imgPreview;   // 🔹 preview foto di dialog

    // launcher untuk ambil gambar dari galeri
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (imgPreview != null) imgPreview.setImageBitmap(bitmap);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();

                        fotoBase64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvProduk = findViewById(R.id.rvProduk);
        tvStatus = findViewById(R.id.tvStatus);
        btnTambah = findViewById(R.id.btnTambah);

        rvProduk.setLayoutManager(new LinearLayoutManager(this));
        api = ApiClient.getClient().create(ApiService.class);

        // 🔹 tampilkan data dari API
        loadProduk();

        // 🔹 aksi tombol tambah
        btnTambah.setOnClickListener(v -> showFormTambah());
    }

    // ✅ ambil data produk dari API
    private void loadProduk() {
        api.getProduk().enqueue(new Callback<List<Produk>>() {
            @Override
            public void onResponse(Call<List<Produk>> call, Response<List<Produk>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Produk> produkList = response.body();
                    adapter = new ProdukAdapter(MainActivity.this, produkList);
                    rvProduk.setAdapter(adapter);
                    tvStatus.setText("Jumlah produk: " + produkList.size());
                } else {
                    tvStatus.setText("Gagal memuat data");
                }
            }

            @Override
            public void onFailure(Call<List<Produk>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal konek ke API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvStatus.setText("Koneksi gagal");
            }
        });
    }

    // ✅ tampilkan form tambah produk (sekarang bisa pilih gambar)
    private void showFormTambah() {
        View view = getLayoutInflater().inflate(R.layout.dialog_form, null);
        EditText etNama = view.findViewById(R.id.etNama);
        EditText etHarga = view.findViewById(R.id.etHarga);
        Button btnPilihFoto = view.findViewById(R.id.btnPilihFoto);
        imgPreview = view.findViewById(R.id.imgPreview);

        // reset foto setiap buka form
        fotoBase64 = "";
        imgPreview.setImageResource(0);

        btnPilihFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        new AlertDialog.Builder(this)
                .setTitle("Tambah Produk")
                .setView(view)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String nama = etNama.getText().toString();
                    String harga = etHarga.getText().toString();

                    if (nama.isEmpty() || harga.isEmpty()) {
                        Toast.makeText(this, "Isi semua data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, String> data = new HashMap<>();
                    data.put("action", "tambah");
                    data.put("nama_produk", nama);
                    data.put("harga", harga);
                    data.put("foto_base64", fotoBase64);

                    api.tambahProduk(data).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            Toast.makeText(MainActivity.this, "Produk berhasil ditambah", Toast.LENGTH_SHORT).show();
                            loadProduk();
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Gagal tambah produk: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
