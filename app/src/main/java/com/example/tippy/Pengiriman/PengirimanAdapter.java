package com.example.tippy.Pengiriman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tippy.R;
import java.util.List;

public class PengirimanAdapter extends RecyclerView.Adapter<PengirimanAdapter.PengirimanViewHolder> {
    private List<Pengiriman> pengirimanList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Pengiriman pengiriman);
        void onDeleteClick(Pengiriman pengiriman);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PengirimanAdapter(List<Pengiriman> pengirimanList) {
        this.pengirimanList = pengirimanList;
    }

    @NonNull
    @Override
    public PengirimanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pengiriman, parent, false);
        return new PengirimanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PengirimanViewHolder holder, int position) {
        Pengiriman pengiriman = pengirimanList.get(position);
        holder.tvNamaProduk.setText(pengiriman.getNamaProduk());
        holder.tvJumlahBarang.setText(pengiriman.getJumlahBarang());
        holder.tvEstimasiTiba.setText("Estimasi: " + pengiriman.getEstimasiTiba());
        holder.tvStatus.setText(pengiriman.getStatusPengiriman());
    }

    @Override
    public int getItemCount() {
        return pengirimanList.size();
    }

    class PengirimanViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProduk, tvJumlahBarang, tvEstimasiTiba, tvStatus;
        ImageView btnDelete;

        public PengirimanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaProduk = itemView.findViewById(R.id.tvNamaProduk);
            tvJumlahBarang = itemView.findViewById(R.id.tvJumlahBarang);
            tvEstimasiTiba = itemView.findViewById(R.id.tvEstimasiTiba);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pengirimanList.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(pengirimanList.get(position));
                }
            });
        }
    }
}
