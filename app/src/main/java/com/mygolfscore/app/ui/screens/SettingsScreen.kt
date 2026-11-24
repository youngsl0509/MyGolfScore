package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mygolfscore.app.ui.theme.GolfDarkEmerald
import com.mygolfscore.app.ui.theme.GolfGreenDark
import com.mygolfscore.app.ui.theme.GolfGreenLight
import com.mygolfscore.app.ui.theme.GolfGreenMid

@Composable
fun SettingsScreen(
    isSignedIn: Boolean,
    accountEmail: String?,
    accountDisplayName: String?,
    accountPhotoUrl: String?,
    isUsingOfflineSample: Boolean,
    totalRoundsCount: Int,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("설정 및 동기화", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = GolfDarkEmerald)
                Text("계정 연결 상태 및 백업 설정", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // 구글 계정 섹션 Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GolfDarkEmerald)
                    Text("Google 계정 정보", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GolfDarkEmerald)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

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
                                    .size(52.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                color = GolfGreenLight,
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = GolfGreenDark,
                                        modifier = Modifier.size(36.dp)
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
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GolfGreenMid,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Google Drive 연결됨",
                                    fontSize = 11.sp,
                                    color = GolfGreenMid,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onSignOutClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google 계정 로그아웃", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Surface(
                        color = GolfGreenLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Google 계정 로그인 필요",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GolfDarkEmerald
                            )
                            Text(
                                "계정 로그인 시 구글 드라이브에 저장된 본인의 골프 스코어 데이터를 불러오고, 스코어카드 이미지를 동기화합니다.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Button(
                        onClick = onSignInClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GolfDarkEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google 계정으로 로그인", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 드라이브 동기화 상태 섹션 Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = GolfDarkEmerald)
                    Text("데이터 동기화 상태", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GolfDarkEmerald)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("현재 데이터 출처", fontSize = 13.sp, color = Color.Gray)
                    Surface(
                        color = if (isUsingOfflineSample) Color(0xFFFEF3C7) else GolfGreenLight,
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (isUsingOfflineSample) "오프라인 내장 샘플" else "Google Drive 실시간",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUsingOfflineSample) Color(0xFFB45309) else GolfGreenDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("로드된 라운딩 수", fontSize = 13.sp, color = Color.Gray)
                    Text("${totalRoundsCount}회", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GolfDarkEmerald)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("드라이브 폴더", fontSize = 13.sp, color = Color.Gray)
                    Text("GolfScore / Image", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }

                if (isSignedIn) {
                    Spacer(modifier = Modifier.height(2.dp))
                    OutlinedButton(
                        onClick = onRefreshClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Drive에서 새로고침", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 앱 정보 섹션 Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GolfDarkEmerald)
                    Text("앱 정보", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GolfDarkEmerald)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("애플리케이션", fontSize = 13.sp, color = Color.Gray)
                    Text("My Golf Score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GolfDarkEmerald)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("버전", fontSize = 13.sp, color = Color.Gray)
                    Text("v1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }
            }
        }
    }
}
