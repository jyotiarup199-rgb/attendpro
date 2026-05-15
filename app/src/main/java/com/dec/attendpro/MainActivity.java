package com.dec.attendpro;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.dec.attendpro.adapters.AttendanceAdapter;
import com.dec.attendpro.adapters.CarouselAdapter;
import com.dec.attendpro.adapters.StudentAdapter;
import com.dec.attendpro.models.AttendanceRecord;
import com.dec.attendpro.models.CarouselItem;
import com.dec.attendpro.models.StudentBrief;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String userRole = "Teacher"; // Default for testing, will come from Intent
    private ViewPager2 carouselViewPager;
    private TabLayout carouselIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve role from LoginActivity
        if (getIntent().hasExtra("USER_ROLE")) {
            userRole = getIntent().getStringExtra("USER_ROLE");
        }

        setupCommonUI();
        setupRoleBasedUI();
    }

    private void setupCommonUI() {
        // Setup Carousel
        carouselViewPager = findViewById(R.id.carouselViewPager);
        carouselIndicator = findViewById(R.id.carouselIndicator);

        List<CarouselItem> carouselItems = new ArrayList<>();
        carouselItems.add(new CarouselItem("PRO TIP", "Mark Faster", "Use AI Face Detection to mark attendance in seconds."));
        carouselItems.add(new CarouselItem("ALERT", "Low Attendance", "3 students are currently below 75% attendance."));
        carouselItems.add(new CarouselItem("EVENT", "Semester Finals", "Final examinations start from next Monday."));

        CarouselAdapter adapter = new CarouselAdapter(carouselItems);
        carouselViewPager.setAdapter(adapter);

        new TabLayoutMediator(carouselIndicator, carouselViewPager, (tab, position) -> {}).attach();

        // Header Profile Click
        findViewById(R.id.btnProfile).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Profile Settings", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRoleBasedUI() {
        ViewGroup container = findViewById(R.id.content_container);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        if ("Student".equalsIgnoreCase(userRole)) {
            // Student: Single-view, no navigation bar
            getLayoutInflater().inflate(R.layout.layout_student_content, container, true);
            bottomAppBar.setVisibility(View.GONE);
            setupStudentHistory();
        } else {
            // Teacher: Searchable list, full navigation
            getLayoutInflater().inflate(R.layout.layout_teacher_content, container, true);
            bottomAppBar.setVisibility(View.VISIBLE);
            setupTeacherFunctions();
        }
    }

    private void setupStudentHistory() {
        RecyclerView rv = findViewById(R.id.rvStudentAttendance);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            List<AttendanceRecord> records = new ArrayList<>();
            records.add(new AttendanceRecord("Advanced Mathematics", "Today, 10:00 AM", AttendanceRecord.Status.PRESENT));
            records.add(new AttendanceRecord("Operating Systems", "Yesterday, 02:00 PM", AttendanceRecord.Status.PRESENT));
            records.add(new AttendanceRecord("Database Management", "12 May, 09:00 AM", AttendanceRecord.Status.ABSENT));
            records.add(new AttendanceRecord("Software Engineering", "11 May, 11:30 AM", AttendanceRecord.Status.PRESENT));
            rv.setAdapter(new AttendanceAdapter(records));
        }
    }

    private void setupTeacherFunctions() {
        // Setup Dropdowns
        setupDropdown(R.id.dropdownClass, new String[]{"CSE - Section A", "IT - Section B", "ECE - Section C"});
        setupDropdown(R.id.dropdownSemester, new String[]{"Semester 4", "Semester 6", "Semester 8"});
        setupDropdown(R.id.dropdownBranch, new String[]{"CS Engineering", "Information Tech", "Electronics"});

        // Setup Student List
        RecyclerView rv = findViewById(R.id.rvStudents);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            List<StudentBrief> students = new ArrayList<>();
            students.add(new StudentBrief("Alex Johnson", "2024001"));
            students.add(new StudentBrief("Emma Watson", "2024002"));
            students.add(new StudentBrief("Robert Downey", "2024003"));
            students.add(new StudentBrief("Scarlett J.", "2024004"));
            rv.setAdapter(new StudentAdapter(students));
        }

        // Search trigger and list expansion logic (if dropdowns are selected)
        View listContainer = findViewById(R.id.studentListContainer);
        if (listContainer != null) {
            AutoCompleteTextView classDropdown = findViewById(R.id.dropdownClass);
            classDropdown.setOnItemClickListener((parent, view, position, id) -> {
                listContainer.setVisibility(View.VISIBLE);
                listContainer.setTranslationY(100f);
                listContainer.setAlpha(0f);
                listContainer.animate().translationY(0f).alpha(1f).setDuration(400).start();
            });
        }
    }

    private void setupDropdown(int resId, String[] items) {
        AutoCompleteTextView dropdown = findViewById(resId);
        if (dropdown != null) {
            dropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        }
    }
}
