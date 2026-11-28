package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mygolfscore.app.ui.theme.*

@Composable
fun SettingsScreen(
    isSignedIn: Boolean,
    accountEmail: String?,
    accountDisplayName: String?,
    accountPhotoUrl: String?,
    lastSyncedAt: String?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "설정 및 동기화",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = GolfDarkEmerald
        )

        // 1. 계정 정보
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "계정 정보",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GolfDarkEmerald
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isSignedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!accountPhotoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = accountPhotoUrl,
                                    contentDescription = "프로필 사진",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    color = MintTint,
                                    shape = CircleShape,
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = accountDisplayName ?: "구글 사용자",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GolfDarkEmerald
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = accountEmail ?: "이메일 정보 없음",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Google Drive 연결됨",
                                        fontSize = 12.sp,
                                        color = BrandPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = DestructiveRed
                            ),
                            border = BorderStroke(1.dp, DestructiveBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("로그아웃", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Google 계정으로 로그인", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. 데이터 동기화 상태
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "데이터 동기화 상태",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GolfDarkEmerald
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("마지막 동기화", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = lastSyncedAt ?: "동기화 대기",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GolfDarkEmerald
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("이미지 저장 폴더", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = "GolfScore / Image",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GolfDarkEmerald
                        )
                    }

                    if (isSignedIn) {
                        OutlinedButton(
                            onClick = onRefreshClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = BrandPrimary
                            ),
                            border = BorderStroke(1.dp, BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("지금 동기화", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // 3. 앱 정보
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "앱 정보",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GolfDarkEmerald
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("애플리케이션", fontSize = 13.sp, color = TextSecondary)
                        Text("MyScore", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GolfDarkEmerald)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("버전", fontSize = 13.sp, color = TextSecondary)
                        Text("v1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GolfDarkEmerald)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "로그아웃",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GolfDarkEmerald
                )
            },
            text = {
                Text(
                    text = "로그아웃하면 Google Drive 연동이 해제되어 라운딩 기록을 불러올 수 없습니다. 계속할까요?",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOutClick()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DestructiveRed,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("로그아웃", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
