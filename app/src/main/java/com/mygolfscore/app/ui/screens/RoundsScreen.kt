package com.mygolfscore.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mygolfscore.app.data.model.GolfRound
import com.mygolfscore.app.data.model.HoleScore
import com.mygolfscore.app.ui.theme.*
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun RoundsListScreen(
    rounds: List<GolfRound>,
    onSelectRound: (GolfRound) -> Unit,
    modifier: Modifier = Modifier,
) {
    val years = remember(rounds) {
        rounds.asSequence().mapNotNull { round ->
            val y = round.date.take(4)
            if ((y.length == 4) && y.all { it.isDigit() }) y else null
        }.distinct().sortedDescending().toList()
    }
    var selectedYear by remember { mutableStateOf("전체") }

    LaunchedEffect(years) {
        if ((selectedYear != "전체") && (selectedYear !in years)) {
            selectedYear = "전체"
        }
    }

    val filteredRounds = remember(rounds, selectedYear) {
        if (selectedYear == "전체") rounds
        else rounds.filter { round -> round.date.startsWith(selectedYear) }
    }

    val filterOptions = remember(years) { listOf("전체") + years }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "라운드 히스토리",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = GolfDarkEmerald
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(filterOptions, key = { it }) { option ->
                val isSelected = option == selectedYear
                Surface(
                    color = if (isSelected) BrandPrimary else Color.White,
                    shape = CircleShape,
                    border = if (isSelected) null else BorderStroke(1.dp, HairlineDivider),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { selectedYear = option }
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else GolfDarkEmerald,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }
        }

        if (filteredRounds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 라운딩 기록이 없습니다",
                    fontSize = 14.sp,
                    color = TextTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRounds, key = { round -> round.id }) { round ->
                    RoundSummaryCard(
                        round = round,
                        onClick = { onSelectRound(round) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundSummaryCard(
    round: GolfRound,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = round.courseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GolfDarkEmerald,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subCourses = listOf(round.frontCourseName, round.backCourseName)
                    .asSequence()
                    .filter { it.isNotBlank() }
                    .joinToString(" / ")
                if (subCourses.isNotBlank()) {
                    Text(
                        text = subCourses,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val dateText = if (round.time.isNotBlank()) "${round.date} ${round.time}" else round.date
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    color = MintTint,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    val sign = if (round.toPar > 0) "+" else ""
                    Text(
                        text = "${round.totalStrokes}타 ($sign${round.toPar})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MintText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (round.parTotal > 0) {
                    Text(
                        text = "Par ${round.parTotal}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RoundDetailScreen(
    round: GolfRound,
    isViewerOpen: Boolean,
    isLoadingImage: Boolean,
    viewingImageBytes: ByteArray?,
    imageErrorMessage: String?,
    onBack: () -> Unit,
    onViewOriginalImage: () -> Unit,
    onDismissOriginalImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 상단 서브 헤더: 좌측 ← 라운드 목록 + 우측 원본 이미지 필 버튼 (v1.0.1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "라운드 목록으로 돌아가기",
                    tint = GolfDarkEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "라운드 목록",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GolfDarkEmerald
                )
            }

            if (round.imageFileId.isNotBlank()) {
                OutlinedButton(
                    onClick = onViewOriginalImage,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, ViewerPillBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ViewerPillBg,
                        contentColor = ViewerPillBorder
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "원본 이미지",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        RoundSummaryCard(round = round)

        val frontHoles = round.holes.take(9)
        val backHoles = round.holes.drop(9)

        if (frontHoles.isNotEmpty()) {
            CourseHolesSection(
                title = "전반 코스 — ${round.frontCourseName.ifBlank { "Front 9" }}",
                holes = frontHoles,
                strokes = round.frontStrokes
            )
        }
        if (backHoles.isNotEmpty()) {
            CourseHolesSection(
                title = "후반 코스 — ${round.backCourseName.ifBlank { "Back 9" }}",
                holes = backHoles,
                strokes = round.backStrokes
            )
        }

        if (round.holes.isNotEmpty()) {
            RoundScoreStatisticsCard(round.holes)
        }

        if (round.imageFileId.isNotBlank()) {
            OutlinedButton(
                onClick = onViewOriginalImage,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = BrandPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("원본 스코어카드 이미지 보기", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (isViewerOpen) {
        OriginalImageViewerDialog(
            courseName = round.courseName,
            roundDate = round.date,
            isLoading = isLoadingImage,
            imageBytes = viewingImageBytes,
            errorMessage = imageErrorMessage,
            onRetry = onViewOriginalImage,
            onDismiss = onDismissOriginalImage,
        )
    }
}

@Composable
private fun CourseHolesSection(title: String, holes: List<HoleScore>, strokes: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GolfDarkEmerald,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (strokes > 0) {
                Surface(
                    color = MintTint,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${strokes}타",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MintText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TableHeaderBg)
                        .padding(vertical = 7.dp, horizontal = 8.dp)
                ) {
                    Text("홀", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                    Text("Par", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                    Text("타수", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                    Text("파대비", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                    Text("결과", Modifier.weight(1.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                }

                holes.forEachIndexed { index, h ->
                    val hasScore = (h.strokes > 0) && (h.par > 0)
                    val (bg, fg) = when {
                        !hasScore -> Color.Transparent to TextSecondary
                        h.toPar <= -1 -> BirdieBg to BirdieBlue
                        h.toPar == 0 -> ParBg to ParGreen
                        h.toPar == 1 -> BogeyBg to BogeyAmber
                        h.toPar == 2 -> DoubleBogeyBg to DoubleBogeyOrange
                        else -> TripleBg to TripleRed
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (h.hole > 0) h.hole.toString() else "",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = GolfDarkEmerald
                        )
                        Text(
                            text = if (h.par > 0) h.par.toString() else "",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                        Text(
                            text = if (h.strokes > 0) h.strokes.toString() else "",
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = GolfDarkEmerald
                        )
                        val toParText = if (!hasScore) "" else if (h.toPar > 0) "+${h.toPar}" else h.toPar.toString()
                        Text(
                            text = toParText,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = fg,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier.weight(1.3f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasScore && h.result.isNotBlank()) {
                                Surface(
                                    color = bg,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = h.result,
                                        fontSize = 11.sp,
                                        color = fg,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    if (index < holes.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundScoreStatisticsCard(holes: List<HoleScore>) {
    val validHoles = holes.filter { hole -> (hole.strokes > 0) && (hole.par > 0) }
    val totalHoles = max(1, validHoles.size)
    val birdieCount = validHoles.count { it.toPar <= -1 }
    val parCount = validHoles.count { it.toPar == 0 }
    val bogeyCount = validHoles.count { it.toPar == 1 }
    val doubleBogeyCount = validHoles.count { it.toPar == 2 }
    val triplePlusCount = validHoles.count { it.toPar >= 3 }

    val statItems = listOf(
        Triple("버디", birdieCount, BirdieBg to BirdieBlue),
        Triple("파", parCount, ParBg to ParGreen),
        Triple("보기", bogeyCount, BogeyBg to BogeyAmber),
        Triple("더블보기", doubleBogeyCount, DoubleBogeyBg to DoubleBogeyOrange),
        Triple("트리플+", triplePlusCount, TripleBg to TripleRed)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = GolfDarkEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${validHoles.size}홀 스코어 통계 요약",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GolfDarkEmerald
                    )
                }
                Text(
                    text = "${validHoles.size}홀 플레이",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statItems.forEach { (label, count, colors) ->
                    val (bgColor, fgColor) = colors
                    val pct = (count * 100.0) / totalHoles

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = bgColor,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${count}홀",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = fgColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", pct),
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalImageViewerDialog(
    courseName: String,
    roundDate: String,
    isLoading: Boolean,
    imageBytes: ByteArray?,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val bitmap = remember(imageBytes) {
        imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    var scale by remember(imageBytes) { mutableFloatStateOf(1f) }
    var offsetX by remember(imageBytes) { mutableFloatStateOf(0f) }
    var offsetY by remember(imageBytes) { mutableFloatStateOf(0f) }
    var rotationDegrees by remember(imageBytes) { mutableFloatStateOf(0f) }
    var showOverlays by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ViewerBackdrop)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = BrandPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null || bitmap == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = errorMessage ?: "이미지를 표시할 수 없습니다.",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandPrimary,
                                    contentColor = Color.White
                                ),
                                shape = CircleShape
                            ) {
                                Text("다시 시도", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                shape = CircleShape
                            ) {
                                Text("닫기", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                else -> {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "원본 스코어카드 이미지",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showOverlays = !showOverlays },
                                    onDoubleTap = {
                                        if (scale > 1.05f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2f
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 4f)
                                    scale = newScale
                                    if (newScale > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                                rotationZ = rotationDegrees
                            }
                    )
                }
            }

            // 상단 오버레이 바
            if (showOverlays) {
                Surface(
                    color = ViewerOverlayBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "원본 스코어카드",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$courseName · $roundDate",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 하단 플로팅 컨트롤 바
            if (showOverlays && !isLoading && errorMessage == null && bitmap != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = ViewerControlBarBg,
                        shape = CircleShape,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val nextScale = (scale - 0.5f).coerceIn(1f, 4f)
                                    scale = nextScale
                                    if (nextScale <= 1f) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "축소",
                                    tint = Color.White
                                )
                            }

                            Surface(
                                color = ViewerZoomBadgeBg,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${(scale * 100f).roundToInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    scale = (scale + 0.5f).coerceIn(1f, 4f)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "확대",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = {
                                    rotationDegrees = (rotationDegrees + 90f) % 360f
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                    contentDescription = "90도 회전",
                                    tint = Color.White
                                )
                            }

                            TextButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotationDegrees = 0f
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = "1:1",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "두 손가락으로 확대·이동 · 더블 탭으로 2배 확대",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
