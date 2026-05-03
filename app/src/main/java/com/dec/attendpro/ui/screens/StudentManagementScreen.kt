package com.dec.attendpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dec.attendpro.ui.components.AttendProCard
import com.dec.attendpro.ui.models.StudentBrief

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val studentList = listOf(
        StudentBrief("Robert Fox", "ID: 202101"),
        StudentBrief("Jane Cooper", "ID: 202102"),
        StudentBrief("Guy Hawkins", "ID: 202103"),
        StudentBrief("Arlene McCoy", "ID: 202104"),
        StudentBrief("Bessie Cooper", "ID: 202105"),
        StudentBrief("Cody Fisher", "ID: 202106")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Students", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Student */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { isSearchActive = false },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = { Text("Search students...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it }
            ) {
                // Search results or suggestions
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text(
                        text = "Class: Computer Science 101",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                items(studentList) { student ->
                    StudentItem(student)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StudentItem(student: StudentBrief) {
    AttendProCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = student.id,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { /* Options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    }
}
