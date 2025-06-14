package com.example.tippy.DaftarSupplier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tippy.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {

    private List<Supplier> supplierList;
    private OnSupplierUpdateClickListener updateClickListener;
    private OnSupplierDeleteClickListener deleteClickListener;

    public interface OnSupplierUpdateClickListener {
        void onUpdateClick(Supplier supplier, int position);
    }

    public interface OnSupplierDeleteClickListener {
        void onDeleteClick(int position);
    }

    public SupplierAdapter(List<Supplier> supplierList,
                           OnSupplierUpdateClickListener updateListener,
                           OnSupplierDeleteClickListener deleteListener) {
        this.supplierList = supplierList;
        this.updateClickListener = updateListener;
        this.deleteClickListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Supplier supplier = supplierList.get(position);
        holder.txtName.setText(supplier.getName());
        holder.txtEmail.setText(supplier.getEmail());
        holder.txtDesc.setText(supplier.getDescription());

        if (supplier.getImageUri() != null && !supplier.getImageUri().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(supplier.getImageUri())
                    .placeholder(R.drawable.ic_supplier)
                    .error(R.drawable.ic_supplier)
                    .into(holder.imgSupplier);
        } else {
            holder.imgSupplier.setImageResource(R.drawable.ic_supplier);
        }

        holder.btnUpdate.setOnClickListener(v -> {
            if (updateClickListener != null) {
                updateClickListener.onUpdateClick(supplier, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return supplierList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtDesc, btnUpdate, btnDelete;
        ImageView imgSupplier;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_supplier_name);
            txtEmail = itemView.findViewById(R.id.txt_supplier_email);
            txtDesc = itemView.findViewById(R.id.txt_supplier_desc);
            imgSupplier = itemView.findViewById(R.id.img_supplier);
            btnUpdate = itemView.findViewById(R.id.btn_update);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}