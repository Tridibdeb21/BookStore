package com.example.bookstore.ui.screens.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onAdminClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onShelfClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var isAdmin by remember { mutableStateOf(false) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    var userStreak by remember { mutableStateOf(0) }
    var yearlyGoal by remember { mutableStateOf(0) }
    var booksFinished by remember { mutableStateOf(0) }
    var unlockedBadges by remember { mutableStateOf<List<String>>(emptyList()) }
    var showGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        if (doc.getString("role") == "admin") isAdmin = true
                        userStreak = doc.getLong("readingStreak")?.toInt() ?: 0
                        yearlyGoal = doc.getLong("yearlyGoal")?.toInt() ?: 0
                        booksFinished = doc.getLong("booksFinishedThisYear")?.toInt() ?: 0
                        unlockedBadges = doc.get("unlockedBadges") as? List<String> ?: emptyList()
                        
                        val base64 = doc.getString("profileImageBase64")
                        if (!base64.isNullOrBlank()) {
                            try {
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                profileBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Failed to decode image: ${e.message}")
                            }
                        }
                    }
                }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentUser != null) {
            isUploading = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val size = 200
                val scaled = Bitmap.createScaledBitmap(originalBitmap, size, size, true)
                profileBitmap = scaled

                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
                val base64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)

                db.collection("users").document(currentUser.uid)
                    .update("profileImageBase64", base64)
                    .addOnCompleteListener { isUploading = false }
            } catch (e: Exception) {
                isUploading = false
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Executive Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                ) {
                    Text(
                        text = "Member Profile",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 44.dp)
                    )
                }

                // Profile Card Overlapping Header
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Section
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileBitmap != null) {
                                    Image(
                                        bitmap = profileBitmap!!.asImageBitmap(),
                                        contentDescription = "Profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                }

                                if (isUploading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }

                            // Camera Action Badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Profile Titles
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Book Enthusiast",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentUser?.email ?: "Guest User",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Reading Stats Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Reading Performance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF9800).copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = "$userStreak Days Streak 🔥", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Daily reading habit active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Annual Target", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showGoalDialog = true }) {
                                Text("Edit Goal ✏️", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        val progress = if (yearlyGoal > 0) booksFinished.toFloat() / yearlyGoal.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$booksFinished of ${if(yearlyGoal > 0) yearlyGoal else "No Target"} Books Finished",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Milestone Achievements Gallery
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milestones & Badges 🏆",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${unlockedBadges.size}/3 Unlocked",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val allBadges = listOf(
                    Triple("First Step", "🏁 First Step", "Finished 1st book"),
                    Triple("Halfway There", "🏃 Halfway There", "50% of yearly goal"),
                    Triple("Goal Achiever", "🏆 Goal Achiever", "Completed yearly goal")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allBadges.forEach { badge ->
                        val badgeKey = badge.first
                        val badgeTitle = badge.second
                        val badgeDesc = badge.third
                        val isUnlocked = unlockedBadges.contains(badgeKey)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(badgeTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(badgeDesc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "Unlocked 🔓" else "Locked 🔒",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Actions Menu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "My Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Default.Book,
                            title = "My Bookshelf",
                            iconTint = Color(0xFF8B5CF6),
                            onClick = onShelfClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileMenuItem(
                            icon = Icons.Default.Favorite,
                            title = "My Wishlist",
                            iconTint = Color(0xFFE91E63),
                            onClick = onWishlistClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileMenuItem(
                            icon = Icons.Default.ListAlt,
                            title = "My Orders",
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onOrdersClick
                        )
                    }
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Administration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Dashboard",
                            iconTint = MaterialTheme.colorScheme.secondary,
                            onClick = onAdminClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Logout Button
                OutlinedButton(
                    onClick = { onLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    if (showGoalDialog) {
        var newGoal by remember { mutableStateOf(if (yearlyGoal > 0) yearlyGoal.toString() else "") }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Yearly Goal 🎯", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newGoal,
                    onValueChange = { newGoal = it },
                    label = { Text("Target number of books") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val goalInt = newGoal.toIntOrNull() ?: 0
                        currentUser?.uid?.let { uid ->
                            db.collection("users").document(uid).update("yearlyGoal", goalInt)
                            yearlyGoal = goalInt
                        }
                        showGoalDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
