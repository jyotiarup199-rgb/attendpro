package com.dec.attendpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.R;
import com.dec.attendpro.models.AttendanceRecord;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceRecord> records;

    public AttendanceAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);
        holder.tvSubject.setText(record.getSubject());
        holder.tvDate.setText(record.getDate());
        
        switch (record.getStatus()) {
            case PRESENT:
                holder.tvStatus.setText("Present");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_present);
                break;
            case ABSENT:
                holder.tvStatus.setText("Absent");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_absent);
                break;
            case LATE:
                holder.tvStatus.setText("Late");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning_yellow));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_late);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvStatus;
        ViewHolder(View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
