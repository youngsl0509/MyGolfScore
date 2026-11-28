package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    @Suppress("UNUSED_PARAMETER") isUsingOfflineSample: Boolean,
    modifier: Modifier = Modifier,
) {
    val (overall, yearly, dist) = payload.summary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GolfSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Section 1: 나의 라운딩 통계
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExternalSectionHeader(
                title = "나의 라운딩 통계",
                pillText = "총 ${overall.totalRounds}회 라운딩",
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandDarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "평균 스코어",
                            fontSize = 12.sp,
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", overall.avgStrokes),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 36.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${formatToPar(overall.avgToPar)})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "최근 5경기",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", overall.recent5AvgStrokes),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(34.dp)
                                .background(Color.White.copy(alpha = 0.18f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "베스트",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${overall.bestScore}타",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section 2: 올해 요약
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExternalSectionHeader(
                title = "올해 (${yearly.year}년) 요약",
                pillText = "${yearly.totalRounds}회 출전"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(text = "평균 스코어", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", yearly.avgStrokes),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GolfDarkEmerald
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "(${formatToPar(yearly.avgToPar)})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(HairlineDivider)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "출전 라운딩", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${yearly.totalRounds}회",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GolfDarkEmerald
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(HairlineDivider)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "베스트 스코어", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${yearly.bestScore}타",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GolfDarkEmerald
                        )
                    }
                }
            }
        }

        // Section 3: 누적 스코어 분포 (0건 숨김 + 5행 가로 막대 그래프)
        ScoreDistributionSection(dist)
    }
}

@Composable
private fun ExternalSectionHeader(title: String, pillText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = GolfDarkEmerald
        )
        Surface(
            color = MintTint,
            shape = CircleShape
        ) {
            Text(
                text = pillText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MintText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun ScoreDistributionSection(dist: ScoreDistribution) {
    val allItems = listOf(
        Triple("버디", dist.birdie, BirdieBlue),
        Triple("파", dist.par, ParGreen),
        Triple("보기", dist.bogey, BogeyAmber),
        Triple("더블보기", dist.doubleBogey, DoubleBogeyOrange),
        Triple("트리플+", dist.triplePlus, TripleRed)
    )
    val totalHoles = allItems.sumOf { (_, count) -> count }
    val visibleItems = allItems.filter { (_, count) -> count > 0 }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExternalSectionHeader(
            title = "누적 스코어 분포",
            pillText = "총 ${totalHoles}홀"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (visibleItems.isEmpty()) {
                    Text(
                        text = "표시할 홀 기록이 없습니다",
                        fontSize = 13.sp,
                        color = TextTertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val safeTotal = max(1, totalHoles)
                    visibleItems.forEach { (label, count, color) ->
                        val ratio = (count.toFloat() / safeTotal.toFloat()).coerceIn(0.02f, 1f)
                        val pct = (count * 100.0) / safeTotal

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = GolfDarkEmerald,
                                modifier = Modifier.width(64.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(ratio)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                modifier = Modifier.widthIn(min = 78.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GolfDarkEmerald,
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), " (%.1f%%)", pct),
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
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
