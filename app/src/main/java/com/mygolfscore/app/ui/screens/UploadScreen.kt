package com.mygolfscore.app.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mygolfscore.app.ui.theme.*
import java.util.Locale

@Composable
fun UploadScreen(
    isSignedIn: Boolean,
    selectedImageUri: Uri?,
    isUploading: Boolean,
    onSelectImage: (Uri?) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSelectImage(uri)
        }
    }

    fun launchPicker() {
        if (!isUploading) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "스코어카드 업로드",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = GolfDarkEmerald
            )
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "업로드 안내",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (selectedImageUri != null) {
            val (fileName, fileSizeText) = remember(selectedImageUri) {
                resolveImageMeta(context, selectedImageUri)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, BrandPrimary, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "선택된 스코어카드",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        )
                        if (!isUploading) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.58f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(34.dp)
                            ) {
                                IconButton(
                                    onClick = { onSelectImage(null) },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "선택된 이미지 삭제",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GolfDarkEmerald,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (fileSizeText.isNotBlank()) "$fileSizeText · 업로드 준비 완료" else "업로드 준비 완료",
                                fontSize = 12.sp,
                                color = BrandPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = { launchPicker() },
                            enabled = !isUploading,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HairlineDivider),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "다시 선택",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GolfDarkEmerald
                            )
                        }
                    }
                }
            }
        } else {
            val dashColor = BrandPrimary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MintTint.copy(alpha = 0.45f))
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = dashColor,
                            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                            style = stroke
                        )
                    }
                    .clickable { launchPicker() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = MintTint,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "스코어카드 이미지를 탭하여 선택",
                        color = GolfDarkEmerald,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PNG, JPG 포맷 지원",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        val uploadEnabled = isSignedIn && selectedImageUri != null && !isUploading
        Button(
            onClick = onUploadClick,
            enabled = uploadEnabled,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimary,
                contentColor = Color.White,
                disabledContainerColor = if (isUploading) BrandPrimary else HairlineDivider,
                disabledContentColor = if (isUploading) Color.White else Color(0xFF9CA3AF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "드라이브에 업로드",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (uploadEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "스코어카드 업로드 안내",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GolfDarkEmerald
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• 선택한 이미지는 Google Drive(GolfScore/Image) 폴더에 저장됩니다.",
                        fontSize = 13.5.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• 업로드 후 자동 분석을 거쳐 기록과 통계에 반영됩니다.",
                        fontSize = 13.5.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• PNG, JPG 이미지 포맷을 지원합니다.",
                        fontSize = 13.5.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("확인", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
}

private fun resolveImageMeta(context: Context, uri: Uri): Pair<String, String> {
    var name = "scorecard.jpg"
    var sizeBytes = -1L
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0) {
                    cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIdx >= 0) {
                    sizeBytes = cursor.getLong(sizeIdx)
                }
            }
        }
    }
    val sizeText = when {
        sizeBytes <= 0L -> ""
        sizeBytes < 1024L * 1024L -> "${sizeBytes / 1024L} KB"
        else -> String.format(Locale.getDefault(), "%.1f MB", sizeBytes / (1024.0 * 1024.0))
    }
    return name to sizeText
}
