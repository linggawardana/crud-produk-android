package com.example.crudprodukapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ViewHolder> {
    private Context context;
    private List<Produk> produkList;
    private ApiService api;

    public ProdukAdapter(Context context, List<Produk> produkList) {
        this.context = context;
        this.produkList = produkList;
        api = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Produk produk = produkList.get(position);
        holder.tvNama.setText(produk.getNama_produk());
        holder.tvHarga.setText("Rp " + produk.getHarga());

        String imageUrl = "http://172.14.6.216/crud_produk_web/" + produk.getFoto_produk();
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgFoto);

        // tombol hapus: gunakan posisi, lalu ambil id di hapusProduk()
        holder.btnHapus.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Produk")
                    .setMessage("Yakin ingin menghapus \"" + produk.getNama_produk() + "\"?")
                    .setPositiveButton("Ya", (dialog, which) -> hapusProduk(position))
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    /**
     * Hapus produk berdasarkan posisi di list.
     * Adapter akan mencoba mengekstrak nilai id (no_produk) dari objek Produk
     * dengan refleksi (mencari getter lalu field) — sehingga tidak bergantung
     * pada nama method/tipe tertentu di class Produk.
     */
    private void hapusProduk(int position) {
        if (position < 0 || position >= produkList.size()) return;

        Produk p = produkList.get(position);
        String idStr = extractIdAsString(p);
        if (idStr == null || idStr.isEmpty()) {
            Toast.makeText(context, "ID produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("action", "hapus");
        data.put("no_produk", idStr);

        api.hapusProduk(data).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // cek respon sukses (opsional: periksa body)
                produkList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(context, "Gagal hapus: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Coba ekstrak id (no_produk) dari objek Produk.
     * - Pertama cari method getter dengan beberapa varian nama:
     *   getNo_produk, getNoProduk, getNo_produk, getNoProduk (case-insensitive tries)
     * - Jika tidak ada, coba akses field "no_produk" atau "noProduk"
     * - Kembalikan sebagai String (null jika tidak ditemukan)
     */
    private String extractIdAsString(Produk p) {
        if (p == null) return null;

        // 1) coba beberapa kemungkinan nama getter
        String[] candidateGetters = new String[] {
                "getNo_produk", "getNoProduk", "getNo_produk", "getNoProduk",
                "getNo", "getNoProduk"
        };

        for (String name : candidateGetters) {
            try {
                Method m = p.getClass().getMethod(name);
                if (m != null) {
                    Object val = m.invoke(p);
                    if (val != null) return String.valueOf(val);
                }
            } catch (NoSuchMethodException nsme) {
                // ignore, coba nama getter berikutnya
            } catch (Exception e) {
                // jika error saat invoke, lanjut ke cara lain
                e.printStackTrace();
            }
        }

        // 2) coba cari field secara langsung
        String[] candidateFields = new String[] { "no_produk", "noProduk", "no_produk", "noProduk", "no" };
        for (String fieldName : candidateFields) {
            try {
                Field f = p.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object val = f.get(p);
                if (val != null) return String.valueOf(val);
            } catch (NoSuchFieldException nsfe) {
                // ignore, coba field lain
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3) kalau masih belum ketemu, cari method getter yang mengandung kata "no" dan "produk"
        try {
            Method[] methods = p.getClass().getMethods();
            for (Method m : methods) {
                String mn = m.getName().toLowerCase();
                if ((mn.contains("no") || mn.contains("id")) && mn.contains("produk")) {
                    try {
                        Object val = m.invoke(p);
                        if (val != null) return String.valueOf(val);
                    } catch (Exception ignore) {}
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    @Override
    public int getItemCount() {
        return produkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvHarga;
        ImageView imgFoto;
        Button btnHapus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            btnHapus = itemView.findViewById(R.id.btnHapus);
        }
    }
}
