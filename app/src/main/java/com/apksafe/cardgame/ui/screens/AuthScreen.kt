package com.apksafe.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.apksafe.cardgame.R
import com.apksafe.cardgame.data.network.DeviceUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.ui.components.AvatarCatalog
import com.apksafe.cardgame.ui.components.AvatarCircle
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import com.apksafe.cardgame.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()

    val context = LocalContext.current
    var usernameInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var selectedUserForLogin by remember { mutableStateOf<UserEntity?>(null) }

    // Auto-advance if already authenticated
    if (uiState.currentUser != null) {
        onAuthSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp)
            .testTag("auth_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Header Logo and Title
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_card_logo),
                        contentDescription = "Logo Game Card",
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CARD BATTLE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Sistem Akun Lokal & Login Offline Aman",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Account Switcher Section (if local users exist)
            if (allUsers.isNotEmpty() && !uiState.isRegisterMode) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Akun Tersimpan di Perangkat:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allUsers) { user ->
                                val isSelected = selectedUserForLogin?.id == user.id
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            selectedUserForLogin = user
                                            usernameInput = user.username
                                            // Pre-fill PIN if demo
                                            if (user.username == "PlayerOne" || user.username == "PlayerTwo") {
                                                pinInput = "1234"
                                            }
                                        }
                                        .testTag("user_item_${user.username}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.2f) else SurfaceCard
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarCircle(avatarId = user.avatar, size = 32.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = user.username,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Menang: ${user.wins} | Koin: ${user.coins}",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Login or Register Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.isRegisterMode) "Buat Akun Lokal Baru" else "Masuk Akun Offline",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Picker (if Register mode)
                        if (uiState.isRegisterMode) {
                            Text(
                                text = "Pilih Avatar:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(AvatarCatalog.options) { opt ->
                                    val isSelected = uiState.selectedAvatar == opt.id
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) GoldPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                            .border(
                                                2.dp,
                                                if (isSelected) GoldPrimary else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable { authViewModel.selectAvatar(opt.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = opt.icon,
                                            contentDescription = opt.name,
                                            tint = opt.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Username Input
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Nama Pengguna / Nickname") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = GoldPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // PIN / Password Input
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("PIN / Kata Sandi (Min 4 karakter)") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldPrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Error message
                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error ?: "",
                                color = HealthRed,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Success message
                        if (uiState.successMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.successMessage ?: "",
                                color = GoldPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (uiState.isRegisterMode) {
                                    authViewModel.register(usernameInput, pinInput, DeviceUtils.getThisDeviceId(context))
                                } else {
                                    authViewModel.login(usernameInput, pinInput)
                                }
                            },
                            enabled = !uiState.isLoading && usernameInput.isNotBlank() && pinInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                            } else {
                                Text(
                                    text = if (uiState.isRegisterMode) "Daftar Akun Offline" else "Masuk ke Permainan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Switch Mode Button
                        TextButton(
                            onClick = {
                                authViewModel.toggleMode(!uiState.isRegisterMode)
                            },
                            modifier = Modifier.testTag("switch_auth_mode_button")
                        ) {
                            Text(
                                text = if (uiState.isRegisterMode) "Sudah punya akun? Masuk di sini" else "Belum punya akun? Buat akun baru",
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Offline & Security Note
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Safe",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Data disimpan terenkripsi SHA-256 di basis data Room lokal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
