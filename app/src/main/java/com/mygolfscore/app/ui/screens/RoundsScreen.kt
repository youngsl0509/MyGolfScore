package com.mygolfscore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.PieChart
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
import com.mygolfscore.app.data.model.GolfRound
import com.mygolfscore.app.data.model.HoleScore
import com.mygolfscore.app.ui.theme.*
import java.util.Locale
import kotlin.math.max

@Composable
fun RoundsListScreen(
    rounds: List<GolfRound>,
    onSelectRound: (GolfRound) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("라운드 히스토리", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = GolfDarkEmerald)
                Text("총 ${rounds.size}개의 기록이 동기화되었습니다", fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                color = GolfGreenLight,
                shape = CircleShape
            ) {
                Text(
                    text = "${rounds.size} 경기",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GolfGreenDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rounds, key = { it.id }) { round ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRound(round) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GolfGreenLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.GolfCourse,
                                    contentDescription = null,
                                    tint = GolfGreenDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = round.courseName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GolfDarkEmerald
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val dateText = if (round.time.isNotBlank()) "${round.date} ${round.time}" else round.date
                            Text(
                                text = dateText,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${round.frontCourseName} / ${round.backCourseName}  (Par ${round.parTotal})",
                                fontSize = 11.sp,
                                color = GolfGreenMid,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (round.toPar <= 0) GolfGreenLight else GolfSurface,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${round.totalStrokes}타",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = GolfDarkEmerald
                                    )
                                    val sign = if (round.toPar > 0) "+" else ""
                                    Text(
                                        text = "($sign${round.toPar})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (round.toPar <= 0) ParGreen else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoundDetailScreen(
    round: GolfRound,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = GolfDarkEmerald)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(round.courseName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = GolfDarkEmerald)
                        val dateText = if (round.time.isNotBlank()) "${round.date} ${round.time}" else round.date
                        Text(dateText, fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(
                        color = GolfGreenLight,
                        shape = CircleShape
                    ) {
                        val sign = if (round.toPar > 0) "+" else ""
                        Text(
                            text = "${round.totalStrokes}타 ($sign${round.toPar})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GolfGreenDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        val frontHoles = round.holes.take(9)
        val backHoles = round.holes.drop(9)

        if (frontHoles.isNotEmpty()) {
            CourseHolesCard("전반 코스 — ${round.frontCourseName.ifBlank { "Front 9" }}", frontHoles, round.frontStrokes)
        }
        if (backHoles.isNotEmpty()) {
            CourseHolesCard("후반 코스 — ${round.backCourseName.ifBlank { "Back 9" }}", backHoles, round.backStrokes)
        }

        // 18홀 스코어 통계 기록 Card
        if (round.holes.isNotEmpty()) {
            RoundScoreStatisticsCard(round.holes)
        }
    }
}

@Composable
private fun CourseHolesCard(title: String, holes: List<HoleScore>, subtotal: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GolfDarkEmerald)
                Surface(
                    color = GolfSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "서브토탈 ${subtotal}타",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GolfGreenMid,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GolfGreenLight)
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text("홀", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                Text("Par", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                Text("타수", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                Text("파대비", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                Text("결과", Modifier.weight(1.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
            }

            holes.forEach { h ->
                val (bg, fg) = when {
                    h.toPar <= -1 -> BirdieBg to BirdiePink
                    h.toPar == 0 -> ParBg to ParGreen
                    h.toPar == 1 -> BogeyBg to BogeyOrange
                    h.toPar == 2 -> DoubleBogeyBg to DoubleBogeyRed
                    else -> TripleBg to TripleRed
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(h.hole.toString(), Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    Text(h.par.toString(), Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.Center, color = Color.Gray)
                    Text(h.strokes.toString(), Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GolfDarkEmerald)
                    val sign = if (h.toPar > 0) "+" else ""
                    Text("$sign${h.toPar}", Modifier.weight(1f), fontSize = 13.sp, color = fg, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    Box(
                        modifier = Modifier.weight(1.3f),
                        contentAlignment = Alignment.Center
                    ) {
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
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun RoundScoreStatisticsCard(holes: List<HoleScore>) {
    val totalHoles = max(1, holes.size)
    val birdieCount = holes.count { it.toPar <= -1 }
    val parCount = holes.count { it.toPar == 0 }
    val bogeyCount = holes.count { it.toPar == 1 }
    val doubleBogeyCount = holes.count { it.toPar == 2 }
    val triplePlusCount = holes.count { it.toPar >= 3 }

    val statItems = listOf(
        Triple("버디", birdieCount, BirdieBg to BirdiePink),
        Triple("파", parCount, ParBg to ParGreen),
        Triple("보기", bogeyCount, BogeyBg to BogeyOrange),
        Triple("더블보기", doubleBogeyCount, DoubleBogeyBg to DoubleBogeyRed),
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
                        text = "18홀 스코어 통계 요약",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GolfDarkEmerald
                    )
                }
                Text(
                    text = "${totalHoles}홀 플레이",
                    fontSize = 12.sp,
                    color = Color.Gray,
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
                            color = Color.DarkGray
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", pct),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
