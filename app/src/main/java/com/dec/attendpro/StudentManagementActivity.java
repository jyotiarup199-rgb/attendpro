package com.dec.attendpro;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dec.attendpro.adapters.StudentAdapter;
import com.dec.attendpro.models.StudentBrief;
import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvStudents = findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        List<StudentBrief> studentList = new ArrayList<>();
        studentList.add(new StudentBrief("Robert Fox", "ID: 202101"));
        studentList.add(new StudentBrief("Jane Cooper", "ID: 202102"));
        studentList.add(new StudentBrief("Guy Hawkins", "ID: 202103"));
        studentList.add(new StudentBrief("Arlene McCoy", "ID: 202104"));
        studentList.add(new StudentBrief("Bessie Cooper", "ID: 202105"));
        studentList.add(new StudentBrief("Cody Fisher", "ID: 202106"));

        StudentAdapter adapter = new StudentAdapter(studentList);
        rvStudents.setAdapter(adapter);
    }
}
