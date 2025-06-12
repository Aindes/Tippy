package com.example.tippy.stokBarang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tippy.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;
    private OnItemClickListener itemClickListener;
    private OnDataChangedListener onDataChangedListener;

    public interface OnDataChangedListener {
        void onDataChanged(int itemCount);
    }

    public interface OnItemClickListener {
        void onItemClick(Product product, int position);
        void onDeleteClick(Product product, int position);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.onDataChangedListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView imgProduct, btnDelete;
        RelativeLayout itemContainer;

        public ProductViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            itemContainer = itemView.findViewById(R.id.item_container);
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barang, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("Rp" + product.getPrice());
        holder.tvStock.setText("Stok: " + product.getStock());

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context).load(product.getImageUrl()).placeholder(R.drawable.add_image).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.add_image);
        }

        holder.itemContainer.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(productList.get(currentPosition), currentPosition);
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onDeleteClick(productList.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();

        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged(getItemCount());
        }
    }

    public void addProduct(Product product) {
        productList.add(product);
        notifyItemInserted(productList.size() - 1);

        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged(getItemCount());
        }
    }

    public void removeProduct(int position) {
        if (position >= 0 && position < productList.size()) {
            productList.remove(position);
            notifyItemRemoved(position);
            if (onDataChangedListener != null) {
                onDataChangedListener.onDataChanged(getItemCount());
            }
        }
    }

    public void updateProduct(int position, Product product) {
        if (position >= 0 && position < productList.size()) {
            productList.set(position, product);
            notifyItemChanged(position);
        }
    }
}