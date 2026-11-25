package com.mygolfscore.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.mygolfscore.app.ui.theme.GolfAppIconGreen
import com.mygolfscore.app.ui.theme.GolfDarkEmerald
import com.mygolfscore.app.ui.theme.GolfGreenLight
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
    val snackbarHostState = remember { SnackbarHostState() }

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

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.onAuthConsentResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(state.authConsentIntent) {
        val intent = state.authConsentIntent ?: return@LaunchedEffect
        try {
            consentLauncher.launch(intent)
        } catch (_: Throwable) {
            viewModel.onAuthConsentResult(false)
        } finally {
            viewModel.consumeAuthConsentIntent()
        }
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        viewModel.clearStatusMessage()
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
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Status bar background extends into status bar; text/buttons sit safely below status bar
            Surface(
                color = GolfAppIconGreen,
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
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.GolfCourse,
                                    contentDescription = null,
                                    tint = Color.White,
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
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
            // Navigation bar background extends to screen edge; tabs sit cleanly above device navigation bar
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 4.dp)
                            .selectableGroup(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactBottomTabItem(
                            selected = selectedTab == 0,
                            onClick = {
                                viewModel.selectRound(null)
                                selectedTab = 0
                            },
                            icon = Icons.Default.Assessment,
                            label = "요약",
                            modifier = Modifier.weight(1f)
                        )
                        CompactBottomTabItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                            label = "기록",
                            modifier = Modifier.weight(1f)
                        )
                        CompactBottomTabItem(
                            selected = selectedTab == 2,
                            onClick = {
                                viewModel.selectRound(null)
                                selectedTab = 2
                            },
                            icon = Icons.Default.CloudUpload,
                            label = "업로드",
                            modifier = Modifier.weight(1f)
                        )
                        CompactBottomTabItem(
                            selected = selectedTab == 3,
                            onClick = {
                                viewModel.selectRound(null)
                                selectedTab = 3
                            },
                            icon = Icons.Default.Settings,
                            label = "설정",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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

@Composable
private fun CompactBottomTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) GolfDarkEmerald else Color(0xFF64748B)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (selected) GolfGreenLight else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
    }
}
