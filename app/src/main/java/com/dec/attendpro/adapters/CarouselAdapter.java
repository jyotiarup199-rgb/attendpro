package com.dec.attendpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.R;
import com.dec.attendpro.models.CarouselItem;
import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private List<CarouselItem> items;

    public CarouselAdapter(List<CarouselItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarouselItem item = items.get(position);
        holder.tvTag.setText(item.getTag());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvTitle, tvDesc;
        ViewHolder(View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvCarouselTag);
            tvTitle = itemView.findViewById(R.id.tvCarouselTitle);
            tvDesc = itemView.findViewById(R.id.tvCarouselDesc);
        }
    }
}
