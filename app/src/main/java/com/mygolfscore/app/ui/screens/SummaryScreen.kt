package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mygolfscore.app.data.model.GolfScorePayload
import com.mygolfscore.app.data.model.ScoreDistribution
import com.mygolfscore.app.ui.theme.*
import java.util.Locale
import kotlin.math.max

@Composable
fun SummaryScreen(
    payload: GolfScorePayload,
    isUsingOfflineSample: Boolean,
    modifier: Modifier = Modifier,
) {
    val overall = payload.summary.overall
    val yearly = payload.summary.currentYear
    val dist = payload.summary.scoreDistribution

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "대시보드 요약",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = GolfDarkEmerald
                )
                Text(
                    text = "골프 성적 및 라운딩 분석",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Surface(
                color = if (isUsingOfflineSample) Color(0xFFFEF3C7) else GolfGreenLight,
                shape = CircleShape,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (isUsingOfflineSample) GoldAccent else GolfAccent,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (isUsingOfflineSample) "내장 샘플" else "Google Drive 동기화",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUsingOfflineSample) Color(0xFFB45309) else GolfGreenDark
                    )
                }
            }
        }

        // Trendy Hero Card (Overall Stats)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GolfDarkEmerald, GolfGreenMid)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.GolfCourse,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "전체 통계",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Surface(
                            color = GolfAccent.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "총 ${overall.totalRounds}회 라운딩",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "평균 타수",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", overall.avgStrokes),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatToPar(overall.avgToPar),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GolfAccent,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Best score badge
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "최저 타수 (라베)",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${overall.bestScore}타",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = GolfAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "최근 5경기 평균 타수",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", overall.recent5AvgStrokes)}타",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Yearly Stats Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    Text(
                        text = "올해 (${yearly.year}년) 요약",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GolfDarkEmerald
                    )
                    Surface(
                        color = GolfGreenLight,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${yearly.totalRounds}회 출전",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GolfGreenDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricPill("평균 타수", String.format(Locale.getDefault(), "%.1f", yearly.avgStrokes), Modifier.weight(1f))
                    StatMetricPill("파 대비", formatToPar(yearly.avgToPar), Modifier.weight(1f))
                    StatMetricPill("시즌 베스트", "${yearly.bestScore}타", Modifier.weight(1f))
                }
            }
        }

        // Score Distribution Card
        ScoreDistributionCard(dist)
    }
}

@Composable
private fun StatMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = GolfSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GolfDarkEmerald)
        }
    }
}

@Composable
private fun ScoreDistributionCard(dist: ScoreDistribution) {
    val items = listOf(
        Triple("버디", dist.birdie, BirdiePink),
        Triple("파", dist.par, ParGreen),
        Triple("보기", dist.bogey, BogeyOrange),
        Triple("더블보기", dist.doubleBogey, DoubleBogeyRed),
        Triple("트리플+", dist.triplePlus, TripleRed)
    )
    val totalHoles = max(1, items.sumOf { it.second })
    val maxCount = max(1, items.maxOf { it.second })

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "누적 스코어 분포",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GolfDarkEmerald
                )
                Text(
                    text = "총 ${totalHoles}홀",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val barCount = items.size
                val slotWidth = size.width / barCount
                val barWidth = slotWidth * 0.44f
                items.forEachIndexed { idx, (_, count, color) ->
                    val ratio = count.toFloat() / maxCount.toFloat()
                    val barHeight = ratio * (size.height - 10.dp.toPx())
                    val left = (idx * slotWidth) + ((slotWidth - barWidth) / 2f)
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { (label, count, color) ->
                    val pct = (count * 100.0) / totalHoles
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                        Text("${count}홀", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GolfDarkEmerald)
                        Text(
                            String.format(Locale.getDefault(), "%.1f%%", pct),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatToPar(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return "$sign${String.format(Locale.getDefault(), "%.1f", value)}"
}
