package com.example.tippy.Keuangan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tippy.R;
import com.example.tippy.Keuangan.model.TransaksiTemp;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder> {

    private Context context;
    private List<TransaksiTemp> transaksiList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TransaksiTemp transaksi, int position);
    }

    public TransaksiAdapter(Context context, List<TransaksiTemp> transaksiList, OnItemClickListener listener) {
        this.context = context;
        this.transaksiList = transaksiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransaksiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaksi, parent, false);
        return new TransaksiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiViewHolder holder, int position) {
        TransaksiTemp transaksi = transaksiList.get(position);

        holder.tvJenis.setText(transaksi.getJenis());

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String formattedJumlah = formatRupiah.format(transaksi.getJumlah());
        holder.tvJumlah.setText(formattedJumlah);

        holder.tvDeskripsi.setText(transaksi.getDeskripsi());
        holder.tvTanggal.setText(transaksi.getTanggal());

        String notaPath = transaksi.getNotaPath();
        if (notaPath != null && !notaPath.isEmpty()) {
            holder.ivNotaThumbnail.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(notaPath)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(holder.ivNotaThumbnail);
        } else {
            holder.ivNotaThumbnail.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(transaksiList.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public static class TransaksiViewHolder extends RecyclerView.ViewHolder {
        TextView tvJenis, tvJumlah, tvTanggal, tvDeskripsi;
        ImageView ivNotaThumbnail;

        public TransaksiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tv_item_jenis);
            tvJumlah = itemView.findViewById(R.id.tv_item_jumlah);
            tvTanggal = itemView.findViewById(R.id.tv_item_tanggal);
            tvDeskripsi = itemView.findViewById(R.id.tv_item_deskripsi);
            ivNotaThumbnail = itemView.findViewById(R.id.iv_item_nota_thumbnail);
        }
    }
}