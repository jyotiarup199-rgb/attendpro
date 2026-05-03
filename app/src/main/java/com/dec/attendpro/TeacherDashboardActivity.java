package com.dec.attendpro;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.adapters.ClassAdapter;
import com.dec.attendpro.models.ClassInfo;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        RecyclerView rvClasses = findViewById(R.id.rvClasses);
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        
        List<ClassInfo> classList = new ArrayList<>();
        classList.add(new ClassInfo("Computer Science 101", "09:00 AM - 10:00 AM", "Room 402", 45));
        classList.add(new ClassInfo("Machine Learning", "11:30 AM - 01:00 PM", "Lab 2", 38));
        classList.add(new ClassInfo("Mobile App Dev", "02:00 PM - 03:30 PM", "Room 105", 42));

        ClassAdapter adapter = new ClassAdapter(classList);
        rvClasses.setAdapter(adapter);
    }
}
