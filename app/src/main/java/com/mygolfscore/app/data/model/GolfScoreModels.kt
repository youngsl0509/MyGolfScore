package com.mygolfscore.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GolfScorePayload(
    val schemaVersion: Int = 1,
    val updatedAt: String = "",
    val player: String = "",
    val driveConfig: DriveFolderConfig = DriveFolderConfig(),
    val summary: SummarySection = SummarySection(),
    val rounds: List<GolfRound> = emptyList(),
)

@Serializable
data class DriveFolderConfig(
    val rootFolderId: String = "1_zoAQx3O_ydAIryb6s00z_Bh-gzGbqvz",
    val imageFolderId: String = "1KbhKnGiyqpZUQR3ArsnHxu7JoUg3ZiD6",
    val jsonFileName: String = "myGolfScore.json"
)

@Serializable
data class SummarySection(
    val overall: OverallStats = OverallStats(),
    val currentYear: YearlyStats = YearlyStats(),
    val scoreDistribution: ScoreDistribution = ScoreDistribution()
)

@Serializable
data class OverallStats(
    val totalRounds: Int = 0,
    val avgStrokes: Double = 0.0,
    val avgToPar: Double = 0.0,
    val recent5AvgStrokes: Double = 0.0,
    val bestScore: Int = 0
)

@Serializable
data class YearlyStats(
    val year: Int = 2026,
    val totalRounds: Int = 0,
    val avgStrokes: Double = 0.0,
    val avgToPar: Double = 0.0,
    val bestScore: Int = 0
)

@Serializable
data class ScoreDistribution(
    val birdie: Int = 0,
    val par: Int = 0,
    val bogey: Int = 0,
    val doubleBogey: Int = 0,
    val triplePlus: Int = 0
)

@Serializable
data class GolfRound(
    val id: String,
    val date: String,
    val time: String = "",
    val courseName: String,
    val player: String,
    val frontCourseName: String,
    val backCourseName: String,
    val parTotal: Int,
    val frontStrokes: Int,
    val backStrokes: Int,
    val totalStrokes: Int,
    val toPar: Int,
    val source: String = "",
    val imageFileName: String = "",
    val imageFileId: String = "",
    val distribution: ScoreDistribution = ScoreDistribution(),
    val holes: List<HoleScore> = emptyList()
)

@Serializable
data class HoleScore(
    val hole: Int,
    val course: String,
    val par: Int,
    val strokes: Int,
    val toPar: Int,
    val result: String
)
