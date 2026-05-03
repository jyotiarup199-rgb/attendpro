package com.dec.attendpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.R;
import com.dec.attendpro.models.ClassInfo;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassInfo> classList;

    public ClassAdapter(List<ClassInfo> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassInfo classInfo = classList.get(position);
        holder.tvSubject.setText(classInfo.getSubject());
        holder.tvDetails.setText(classInfo.getTime() + " • " + classInfo.getRoom());
        holder.tvInitial.setText(classInfo.getSubject().substring(0, 1));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDetails, tvInitial;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvInitial = itemView.findViewById(R.id.tvInitial);
        }
    }
}
