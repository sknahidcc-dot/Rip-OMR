package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudentEntity
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BengaliUtils

@Composable
fun StudentListScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var deleteResultsAlong by remember { mutableStateOf(true) }

    // Add or Edit Student Dialog
    if (showAddDialog || studentToEdit != null) {
        val isEdit = studentToEdit != null
        var rollInput by remember(studentToEdit) { mutableStateOf(studentToEdit?.roll ?: "") }
        var nameInput by remember(studentToEdit) { mutableStateOf(studentToEdit?.name ?: "") }
        var phoneInput by remember(studentToEdit) { mutableStateOf(studentToEdit?.phone ?: "") }
        var validationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                studentToEdit = null
            },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = if (isEdit) "শিক্ষার্থী তথ্য পরিবর্তন" else "নতুন শিক্ষার্থী যোগ করুন",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rollInput,
                        onValueChange = {
                            if (it.length <= 4) rollInput = it
                            validationError = null
                        },
                        label = { Text("৪-সংখ্যার রোল (Roll No)") },
                        enabled = !isEdit, // Primary key immutable on edit
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            validationError = null
                        },
                        label = { Text("শিক্ষার্থীর নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("মোবাইল / নোট (ঐচ্ছিক)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = CrimsonFail,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formattedRoll = rollInput.padStart(4, '0')
                        if (formattedRoll.length != 4) {
                            validationError = "রোল অবশ্যই ৪ সংখ্যার হতে হবে।"
                            return@Button
                        }
                        if (nameInput.isBlank()) {
                            validationError = "শিক্ষার্থীর নাম লিখুন।"
                            return@Button
                        }

                        val entity = StudentEntity(
                            roll = formattedRoll,
                            name = nameInput.trim(),
                            phone = phoneInput.trim()
                        )
                        viewModel.saveStudent(entity) {
                            showAddDialog = false
                            studentToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        studentToEdit = null
                    }
                ) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    // Delete Student Confirmation Dialog
    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = "শিক্ষার্থী মুছে ফেলবেন?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "\"${studentToDelete?.name} (রোল: ${studentToDelete?.roll})\" মুছে ফেলতে চান?",
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { deleteResultsAlong = !deleteResultsAlong }
                    ) {
                        Checkbox(
                            checked = deleteResultsAlong,
                            onCheckedChange = { deleteResultsAlong = it },
                            colors = CheckboxDefaults.colors(checkedColor = CrimsonFail)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "তার সকল সংরক্ষিত পরীক্ষার ফলাফলও মুছে দিন",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete?.let {
                            viewModel.deleteStudent(it, deleteResultsAlong)
                        }
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonFail,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students else {
            students.filter {
                it.roll.contains(searchQuery.trim()) ||
                it.name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "শিক্ষার্থী তালিকা",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "মোট ${BengaliUtils.formatNumber(students.size, useBn)} জন নিবন্ধিত",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("রোল বা নাম দিয়ে খুঁজুন...", color = TextTertiary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CyanAccent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = AmoledCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = AmoledSurface,
                        unfocusedContainerColor = AmoledSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Empty",
                                tint = TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "কোনো শিক্ষার্থী খুঁজে পাওয়া যায়নি" else "কোনো শিক্ষার্থী যোগ করা হয়নি",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredStudents) { student ->
                            val studentResultsCount = results.count { it.roll == student.roll }
                            StudentCard(
                                student = student,
                                examsTakenCount = studentResultsCount,
                                useBn = useBn,
                                onClick = { onNavigate(Screen.StudentDetail(student.roll)) },
                                onEdit = { studentToEdit = student },
                                onDelete = { studentToDelete = student }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            // FAB: Add Student
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanAccent,
                contentColor = AmoledBlack,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Student",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: StudentEntity,
    examsTakenCount: Int,
    useBn: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmoledSurface)
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.12f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.roll.takeLast(2),
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = student.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "রোল: ${BengaliUtils.formatNumber(student.roll, useBn)} • পরীক্ষা দিয়েছে: ${BengaliUtils.formatNumber(examsTakenCount, useBn)}টি",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CrimsonFail,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
