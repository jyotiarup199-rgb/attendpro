package com.dec.attendpro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.R;
import com.dec.attendpro.models.StudentBrief;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<StudentBrief> students;

    public StudentAdapter(List<StudentBrief> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentBrief student = students.get(position);
        holder.tvName.setText(student.getName());
        holder.tvId.setText("Roll: " + student.getId());
        
        holder.btnToggle.setOnClickListener(v -> {
            boolean isPresent = holder.btnToggle.getText().toString().equals("Present");
            if (isPresent) {
                holder.btnToggle.setText("Absent");
                holder.btnToggle.setTextColor(v.getContext().getColor(R.color.error_red));
            } else {
                holder.btnToggle.setText("Present");
                holder.btnToggle.setTextColor(v.getContext().getColor(R.color.success_green));
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;
        MaterialButton btnToggle;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvId = itemView.findViewById(R.id.tvStudentId);
            btnToggle = itemView.findViewById(R.id.btnAttendanceToggle);
        }
    }
}
