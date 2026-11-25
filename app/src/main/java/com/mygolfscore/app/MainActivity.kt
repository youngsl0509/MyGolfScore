package com.mygolfscore.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.mygolfscore.app.ui.GolfScoreViewModel
import com.mygolfscore.app.ui.screens.RoundDetailScreen
import com.mygolfscore.app.ui.screens.RoundsListScreen
import com.mygolfscore.app.ui.screens.SettingsScreen
import com.mygolfscore.app.ui.screens.SummaryScreen
import com.mygolfscore.app.ui.screens.UploadScreen
import com.mygolfscore.app.ui.theme.GolfAccent
import com.mygolfscore.app.ui.theme.GolfDarkEmerald
import com.mygolfscore.app.ui.theme.GolfGreenLight
import com.mygolfscore.app.ui.theme.GolfSurface
import com.mygolfscore.app.ui.theme.MyGolfScoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGolfScoreTheme {
                MyGolfScoreApp()
            }
        }
    }
}

@Composable
fun MyGolfScoreApp(viewModel: GolfScoreViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.onGoogleSignInSuccess(account)
            } catch (e: ApiException) {
                viewModel.onGoogleSignInFailed("코드 ${e.statusCode}")
            }
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive"))
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        signInLauncher.launch(client.signInIntent)
    }

    fun launchGoogleSignOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener {
            viewModel.onGoogleSignOutSuccess()
        }
    }

    Scaffold(
        containerColor = GolfSurface,
        topBar = {
            // Status bar background extends into status bar; text/buttons sit safely below status bar
            Surface(
                color = GolfDarkEmerald,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = GolfAccent.copy(alpha = 0.25f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.GolfCourse,
                                    contentDescription = null,
                                    tint = GolfAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "My Golf Score",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.isSignedIn) {
                            IconButton(
                                onClick = { viewModel.refreshFromDrive() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "새로고침",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (!state.accountPhotoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = state.accountPhotoUrl,
                                    contentDescription = "프로필",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                )
                            }
                        } else {
                            Button(
                                onClick = { launchGoogleSignIn() },
                                colors = ButtonDefaults.buttonColors(containerColor = GolfAccent),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    tint = GolfDarkEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("로그인", color = GolfDarkEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Navigation bar background extends to screen edge; tabs sit cleanly above gesture bar
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            viewModel.selectRound(null)
                            selectedTab = 0
                        },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("요약", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GolfDarkEmerald,
                            selectedTextColor = GolfDarkEmerald,
                            indicatorColor = GolfGreenLight
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("기록", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GolfDarkEmerald,
                            selectedTextColor = GolfDarkEmerald,
                            indicatorColor = GolfGreenLight
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            viewModel.selectRound(null)
                            selectedTab = 2
                        },
                        icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("업로드", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GolfDarkEmerald,
                            selectedTextColor = GolfDarkEmerald,
                            indicatorColor = GolfGreenLight
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            viewModel.selectRound(null)
                            selectedTab = 3
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("설정", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GolfDarkEmerald,
                            selectedTextColor = GolfDarkEmerald,
                            indicatorColor = GolfGreenLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GolfAccent,
                    trackColor = GolfGreenLight
                )
            }
            state.errorMessage?.let { err ->
                Surface(
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> SummaryScreen(
                        payload = state.payload,
                        isUsingOfflineSample = state.isUsingOfflineSample
                    )
                    1 -> {
                        val round = state.selectedRound
                        if (round != null) {
                            RoundDetailScreen(
                                round = round,
                                onBack = { viewModel.selectRound(null) }
                            )
                        } else {
                            RoundsListScreen(
                                rounds = state.payload.rounds,
                                onSelectRound = { viewModel.selectRound(it) }
                            )
                        }
                    }
                    2 -> UploadScreen(
                        isSignedIn = state.isSignedIn,
                        selectedImageUri = state.selectedImageUri,
                        isUploading = state.isUploading,
                        onSelectImage = { viewModel.selectImage(it) },
                        onUploadClick = { viewModel.uploadSelectedImage() }
                    )
                    3 -> SettingsScreen(
                        isSignedIn = state.isSignedIn,
                        accountEmail = state.accountEmail,
                        accountDisplayName = state.accountDisplayName,
                        accountPhotoUrl = state.accountPhotoUrl,
                        isUsingOfflineSample = state.isUsingOfflineSample,
                        totalRoundsCount = state.payload.rounds.size,
                        onSignInClick = { launchGoogleSignIn() },
                        onSignOutClick = { launchGoogleSignOut() },
                        onRefreshClick = { viewModel.refreshFromDrive() }
                    )
                }
            }
        }
    }
}
