package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mygolfscore.app.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Surface(
                color = BrandPrimary,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 6.dp,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GolfCourse,
                        contentDescription = "MyScore",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MyScore",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = GolfDarkEmerald
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "구글 드라이브 연동 골프 스코어카드",
                fontSize = 14.sp,
                color = TextSecondary
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = TripleBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = DestructiveRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onSignInClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, HairlineDivider),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = GolfDarkEmerald
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BrandPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "연결 중...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GolfDarkEmerald
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google 계정으로 로그인",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GolfDarkEmerald
                    )
                }
            }
        }
    }
}
