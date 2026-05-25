package com.example.ui

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.R
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen Enumeration to power the interactive navigation flow
enum class AppScreen {
    Splash, Onboarding, Auth, Main
}

// Inner Tab enum for the Bottom Navigation Dock
enum class NavigationTab {
    Dashboard, Planner, Doubt, Notes, More
}

// Sub-screens triggered via the "More" grid
enum class MoreSubScreen {
    None, MockTests, Analytics, Community, Career, Focus, Profile, Premium, YouTube
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrepNexusApp(viewModel: PrepNexusViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    // Primary navigation State machine
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    var activeTab by remember { mutableStateOf(NavigationTab.Dashboard) }
    var currentSubScreen by remember { mutableStateOf(MoreSubScreen.None) }

    // Dynamic app premium status
    var isSubscribedPremium by remember { mutableStateOf(false) }

    // Navigation trigger depending on onboarding state
    LaunchedEffect(profile) {
        if (currentScreen == AppScreen.Splash) {
            delay(2200) // Beautiful splash hold
            val user = profile
            if (user != null) {
                if (user.isOnboarded) {
                    currentScreen = AppScreen.Main
                } else {
                    currentScreen = AppScreen.Onboarding
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CosmicBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyberPrimaryPurple.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(200f, 300f),
                        radius = 800f
                    )
                )
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "PrimaryPageTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.Splash -> SplashScreen {
                        currentScreen = if (profile?.isOnboarded == true) AppScreen.Main else AppScreen.Onboarding
                    }
                    AppScreen.Onboarding -> OnboardingScreen { name, kls, goal, weak, hours, date ->
                        viewModel.submitOnboarding(name, kls, goal, weak, hours, date)
                        currentScreen = AppScreen.Auth
                    }
                    AppScreen.Auth -> AuthScreen {
                        currentScreen = AppScreen.Main
                    }
                    AppScreen.Main -> {
                        MainLayout(
                            viewModel = viewModel,
                            activeTab = activeTab,
                            onTabSelected = { 
                                activeTab = it
                                currentSubScreen = MoreSubScreen.None // close any full overlay
                            },
                            currentSubScreen = currentSubScreen,
                            onSubScreenSelected = { currentSubScreen = it },
                            isSubscribed = isSubscribedPremium,
                            onToggleSubscribed = { isSubscribedPremium = !isSubscribedPremium }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onContinue: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1.1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.prep_nexus_logo),
            contentDescription = "PrepNexus App Logo",
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.5.dp, Brush.horizontalGradient(listOf(CyberCyan, CyberPrimaryPurple)), RoundedCornerShape(32.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Futuristic Gradient typography
        Text(
            text = "PrepNexus",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.drawBehind {
                // simple ambient glow
            },
            color = Color.White
        )

        Text(
            text = "Your Ultimate AI Study Command Center",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(70.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .testTag("enter_portal_btn")
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(CyberPrimaryPurple, NeonElectricBlue)))
                .padding(horizontal = 40.dp, vertical = 2.dp)
        ) {
            Text(
                text = "LAUNCH PORTAL",
                style = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Color.White)
            )
        }
        
        Text(
            text = "Class 6 to UPSC • IIT-JEE • NEET • Boards",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMutedGray),
            modifier = Modifier.padding(top = 18.dp)
        )
    }
}

// ==========================================
// 2. ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(onOnboardComplete: (String, String, String, String, Int, String) -> Unit) {
    var studentName by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Class 11") }
    var targetGoal by remember { mutableStateOf("IIT-JEE") }
    var weakSubjects by remember { mutableStateOf("") }
    var studyHoursBudget by remember { mutableStateOf(6f) }
    var targetExamDate by remember { mutableStateOf("2027-05-15") }

    val goals = listOf("IIT-JEE", "NEET", "UPSC IAS", "Boards", "NDA/Defense", "Olympiads / foundation")
    val classes = listOf("Class 6-8", "Class 9-10", "Class 11", "Class 12", "Dropper / College")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("onboarding_screen_container")
    ) {
        Text(
            text = "INITIALIZE YOUR COMMANDS",
            style = MaterialTheme.typography.titleMedium.copy(color = CyberCyan, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        )
        Text(
            text = "Configure PrepNexus AI engine to match your target goals",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMutedGray)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Name card glassmorphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(GlassHighlight, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("What is your Name?", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    placeholder = { Text("Enter your warrior alias", color = TextMutedGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target goals selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(GlassHighlight, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Select Target Competitive Goal", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(goals) { goal ->
                        val isSelected = targetGoal == goal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (isSelected) CyberCyan else GlassBorder, RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyberPrimaryPurple.copy(alpha = 0.4f) else Color.Transparent)
                                .clickable { targetGoal = goal }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(goal, color = if (isSelected) CyberCyan else Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Study hours slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(GlassHighlight, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Allocated Study Hours", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${studyHoursBudget.toInt()} Hours", color = CyberCyan, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = studyHoursBudget,
                    onValueChange = { studyHoursBudget = it },
                    valueRange = 4f..14f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberPrimaryPurple,
                        inactiveTrackColor = GlassBorder
                    )
                )
                Text(
                    text = "High density focus blocks with active recall breaks will be scheduled.",
                    fontSize = 11.sp,
                    color = TextMutedGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weak Subjects
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(GlassHighlight, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Specify Weak Areas/Topics to Core-Target", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weakSubjects,
                    onValueChange = { weakSubjects = it },
                    placeholder = { Text("e.g., Rotation mechanics, Trigonometry", color = TextMutedGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = HotPink,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_weak_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val realName = studentName.ifBlank { "Aspirant" }
                onOnboardComplete(realName, selectedClass, targetGoal, weakSubjects, studyHoursBudget.toInt(), targetExamDate)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(HotPink, CyberPrimaryPurple)))
                .testTag("onboarding_submit_btn")
        ) {
            Text("GENERATE PROFILE ECOSYSTEM", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(6.dp))
        }
    }
}

// ==========================================
// 3. AUTH SCREEN (LOGIN/SIGNUP)
// ==========================================
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpEntered by remember { mutableStateOf("") }
    var oTPTriggered by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("auth_screen_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.prep_nexus_logo),
            contentDescription = "PrepNexus logo secondary",
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Brush.horizontalGradient(listOf(CyberCyan, CyberPrimaryPurple)), RoundedCornerShape(18.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("PrepNexus Portal Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Log in securely to sync your study credits & level progress", color = TextMutedGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .background(DeepGlassCard)
                .padding(22.dp)
        ) {
            Column {
                if (!oTPTriggered) {
                    Text("Secure OTP Gateway", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone / Email ID", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (phoneNumber.isNotBlank()) {
                                oTPTriggered = true
                                feedbackMsg = "One-Time-Key dispatched to: $phoneNumber"
                            } else {
                                feedbackMsg = "Specify valid login credentials"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple),
                        modifier = Modifier.fillMaxWidth().testTag("send_otp_btn")
                    ) {
                        Text("DISPATCH SECURITY KEY", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Text("Enter Authentication Key", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(feedbackMsg, color = CyberCyan, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpEntered,
                        onValueChange = { otpEntered = it },
                        label = { Text("4-Digit Passcode (try '1234')", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("auth_otp_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonElectricBlue),
                        modifier = Modifier.fillMaxWidth().testTag("verify_otp_btn")
                    ) {
                        Text("AUTHORIZE & ENTER", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { oTPTriggered = false }) {
                        Text("Go Back", color = TextMutedGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("OR SECURE INSTANT LOGIN VIA", color = TextMutedGray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.Android, contentDescription = "Simulated Google Sign-in", tint = CyberCyan)
            }
            IconButton(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = "Simulated Apple login", tint = HotPink)
            }
        }
    }
}

// ==========================================
// CENTRAL NAV TAB GRID CONTAINER
// ==========================================
@Composable
fun MainLayout(
    viewModel: PrepNexusViewModel,
    activeTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    currentSubScreen: MoreSubScreen,
    onSubScreenSelected: (MoreSubScreen) -> Unit,
    isSubscribed: Boolean,
    onToggleSubscribed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentSubScreen != MoreSubScreen.None) {
                    // Sub pages overtop complete tab area to keep structural boundary of simple requests clean
                    AnimatedContent(targetState = currentSubScreen) { sub ->
                        when(sub) {
                            MoreSubScreen.MockTests -> MockTestsScreen(viewModel) { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Analytics -> AnalyticsScreen(viewModel) { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Community -> CommunityScreen { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Career -> CareerRoadmapScreen { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Focus -> FocusScreen { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Profile -> ProfileSettingsScreen(viewModel) { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.Premium -> PremiumSubscriptionPage(isSubscribed, onToggleSubscribed) { onSubScreenSelected(MoreSubScreen.None) }
                            MoreSubScreen.YouTube -> YouTubePortalScreen { onSubScreenSelected(MoreSubScreen.None) }
                            else -> {}
                        }
                    }
                } else {
                    // Standard bottom tabs
                    when (activeTab) {
                        NavigationTab.Dashboard -> DashboardScreen(viewModel, onSubScreenSelected)
                        NavigationTab.Planner -> AIPlannerScreen(viewModel)
                        NavigationTab.Doubt -> DoubtSolverScreen(viewModel)
                        NavigationTab.Notes -> NotesScreen(viewModel)
                        NavigationTab.More -> MoreMenuGrid(onSubScreenSelected)
                    }
                }
            }

            // Beautiful Floating dock at the bottom
            BottomNavigationDock(activeTab, onTabSelected)
        }
    }
}

// Float glassmorphic dock
@Composable
fun BottomNavigationDock(activeTab: NavigationTab, onTabSelected: (NavigationTab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .background(DeepGlassCard.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(NavigationTab.Dashboard, Icons.Default.Dashboard, "Home"),
                Triple(NavigationTab.Planner, Icons.Default.InsertInvitation, "Planner"),
                Triple(NavigationTab.Doubt, Icons.Default.Sms, "Doubt"),
                Triple(NavigationTab.Notes, Icons.Default.AutoStories, "Notes"),
                Triple(NavigationTab.More, Icons.Default.GridView, "More")
            )

            tabs.forEach { (tab, icon, label) ->
                val selected = activeTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) CyberCyan else TextMutedGray,
                        modifier = Modifier.size(if (selected) 26.dp else 22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (selected) CyberCyan else TextMutedGray,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. HOME DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(viewModel: PrepNexusViewModel, onSubSelection: (MoreSubScreen) -> Unit) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    // Quick prompt helper
    var isAIAssistantOpen by remember { mutableStateOf(false) }
    var miniQuery by remember { mutableStateOf("") }
    var miniAnswer by remember { mutableStateOf("") }
    var miniLoading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dashboard_scroll_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(CyberPrimaryPurple.copy(alpha = 0.5f), NeonElectricBlue.copy(alpha = 0.4f))
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PORTAL SYNCHED", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(
                            text = "Glad to see you, ${profile?.name ?: "Aspirant"}!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = "Level", tint = NeonGold, modifier = Modifier.size(16.dp))
                            Text("Level ${profile?.level ?: 1} • ${profile?.focusWarriorRank ?: "Aspirant"}", color = TextMutedGray, fontSize = 12.sp)
                        }
                    }
                    
                    // Streak circle
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, HotPink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 16.sp)
                            Text("${profile?.streak ?: 1}d", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // Gamification analytics overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // XP Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .background(DeepGlassCard)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("XP EARNED", color = TextMutedGray, fontSize = 11.sp)
                        }
                        Text("${profile?.xp ?: 0}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        LinearProgressIndicator(
                            progress = { ((profile?.xp ?: 0) % 400).toFloat() / 400f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .padding(top = 8.dp),
                            color = CyberPrimaryPurple,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                // Coins Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .background(DeepGlassCard)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STUDY COINS", color = TextMutedGray, fontSize = 11.sp)
                        }
                        Text("${profile?.coins ?: 0}", color = NeonGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Spend in settings grid", color = TextMutedGray, fontSize = 10.sp)
                    }
                }
            }
        }

        // Quote & Heatmap section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(DeepGlassCard)
                    .padding(16.dp)
            ) {
                Column {
                    Text("💡 AI MOTIVATIONAL INSIGHT", color = HotPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"You aren't studying to pass a paper. You are engineering the ultimate neural network capable of solving tomorrow's toughest board challenges.\"",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // Performance grid heatmap simulator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(DeepGlassCard)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊 SYSTEM INTENSITY GRAPH", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Productivity Index: ${profile?.productivityScore ?: 80}%", color = CyberCyan, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Mini grid of "intensity" boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val intensities = listOf(
                            listOf(0.2f, 0.5f, 0.8f, 0.1f, 0.3f, 0.6f, 0.9f),
                            listOf(0.4f, 0.9f, 0.2f, 0.7f, 0.5f, 0.3f, 0.8f),
                            listOf(0.8f, 0.2f, 0.9f, 0.6f, 0.4f, 0.8f, 0.1f)
                        )
                        intensities.forEach { row ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { factor ->
                                    val cellColor = when {
                                        factor > 0.8f -> CyberCyan
                                        factor > 0.5f -> CyberPrimaryPurple
                                        factor > 0.2f -> NeonElectricBlue.copy(alpha = 0.6f)
                                        else -> Color.White.copy(alpha = 0.1f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(cellColor)
                                    )
                                }
                            }
                        }
                        
                        Column {
                            Text("Slight load", color = TextMutedGray, fontSize = 9.sp)
                            Text("Normal study", color = NeonElectricBlue, fontSize = 9.sp)
                            Text("High intake", color = CyberPrimaryPurple, fontSize = 9.sp)
                            Text("Mock Day", color = CyberCyan, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // Active upcoming timetable snippet
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(DeepGlassCard)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅 TIMETABLE TODAY", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${tasks.count { !it.isCompleted }} Remaining", color = CyberCyan, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (tasks.isEmpty()) {
                        Text("No active timetable tasks found. Generate schedule in your AI Planner tab!", color = TextMutedGray, fontSize = 12.sp)
                    } else {
                        tasks.take(2).forEach { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (t.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                        contentDescription = "Status",
                                        tint = if (t.isCompleted) CyberCyan else TextMutedGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = t.title,
                                        color = if (t.isCompleted) TextMutedGray else Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(t.scheduledTime, color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Featured YouTube Channel Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF881919).copy(alpha = 0.75f), Color(0xFF231414).copy(alpha = 0.9f))
                        )
                    )
                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { onSubSelection(MoreSubScreen.YouTube) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play YouTube Logo Link",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PREPNEXUS DIGITAL STUDIO",
                            color = Color(0xFFFF8A8A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Watch Free Exam blueprint Lessons",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gain an edge with high-yield board concepts, mock strategies, and custom blueprints on @prepnexus26",
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Enter Channel Subscreen Link",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Shortcuts & Fast Nav
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onSubSelection(MoreSubScreen.Focus) },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassHighlight),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Text("⏱️ Focus Mode", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSubSelection(MoreSubScreen.MockTests) },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassHighlight),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Text("📝 Mini Test", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Floating assistant bubble
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp, end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { isAIAssistantOpen = !isAIAssistantOpen },
            containerColor = CyberPrimaryPurple,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("floating_assistant_bubble")
        ) {
            Icon(Icons.Default.SupportAgent, contentDescription = "AI Floating Helper")
        }
    }

    // Little overlay overlay for quick help
    if (isAIAssistantOpen) {
        AlertDialog(
            onDismissRequest = { isAIAssistantOpen = false },
            containerColor = DeepGlassCard,
            title = { Text("⚡ Quick AI Assistant", color = CyberCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ask PrepNexus AI anything or request an exam study tip:", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = miniQuery,
                        onValueChange = { miniQuery = it },
                        placeholder = { Text("e.g. UPSC roadmap reference book list", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = GlassBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("floating_query_input")
                    )
                    
                    if (miniLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                    } else if (miniAnswer.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState())
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(8.dp)
                        ) {
                            Text(miniAnswer, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (miniQuery.isNotBlank()) {
                            miniLoading = true
                            coroutineScope.launch {
                                val resp = GeminiService.generateContent(
                                    "Explain or give 2 rapid study tips for: $miniQuery. Be very brief.",
                                    "You are PrepNexus AI study center guide."
                                )
                                miniLoading = false
                                miniAnswer = if (resp == "API_KEY_MISSING") {
                                    "🔑 Please link your API key. Standard tip: Try active recall testing rather than passive highlight reading."
                                } else {
                                    resp
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple)
                ) {
                    Text("QUERY AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAIAssistantOpen = false }) {
                    Text("Close", color = TextMutedGray)
                }
            }
        )
    }
}

// ==========================================
// 5. AI PLANNER SCREEN
// ==========================================
@Composable
fun AIPlannerScreen(viewModel: PrepNexusViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val plannerState by viewModel.plannerState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Theory") }
    var newTaskDuration by remember { mutableStateOf("60") }
    var newTaskTime by remember { mutableStateOf("10:00 AM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("planner_screen_container")
    ) {
        Text("AI STUDY PLANNER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Personalized schedule optimized for ${profile?.goal ?: "Competitive Preparation"}", color = TextMutedGray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(14.dp))

        // Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.generateInitialSchedule(profile?.goal ?: "IIT-JEE", profile?.availableHours ?: 6)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple),
                modifier = Modifier.weight(1f).testTag("optimize_timetable_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Optimize Link", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("OPTIMIZE SCHEDULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .testTag("add_task_trigger")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add manual task", tint = CyberCyan)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (plannerState) {
            is AsyncState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                Text("Re-computing study paths dynamically...", color = TextMutedGray, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 12.sp)
            }
            is AsyncState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = "Tips", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = (plannerState as AsyncState.Success<String>).data,
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tasks block
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).testTag("tasks_scroller")
        ) {
            if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭 Empty study timetable", color = TextMutedGray)
                        TextButton(onClick = { viewModel.generateInitialSchedule(profile?.goal ?: "IIT-JEE", profile?.availableHours ?: 6) }) {
                            Text("Click here to auto-populate")
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    val catColor = when (task.category) {
                        "Theory" -> NeonElectricBlue
                        "Practice" -> CyberPrimaryPurple
                        "Revision" -> CyberCyan
                        "Break" -> HotPink
                        else -> Color.Green
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (task.isCompleted) GlassBorder else catColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .background(DeepGlassCard, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.completeTask(task, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = CyberCyan, uncheckedColor = TextMutedGray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (task.isCompleted) TextMutedGray else Color.White,
                                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(catColor.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(task.category, color = catColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("${task.durationMin} mins • ${task.scheduledTime}", color = TextMutedGray, fontSize = 11.sp)
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deletePlannerTask(task) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove tasks", tint = TextMutedGray)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Manual Creation overlay
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DeepGlassCard,
            title = { Text("Schedule study task", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth().testTag("new_task_title")
                    )

                    OutlinedTextField(
                        value = newTaskDuration,
                        onValueChange = { newTaskDuration = it },
                        label = { Text("Duration in Mins") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("new_task_duration")
                    )

                    OutlinedTextField(
                        value = newTaskTime,
                        onValueChange = { newTaskTime = it },
                        label = { Text("Time (e.g. 11:30 AM)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth().testTag("new_task_time")
                    )

                    Text("Category", color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val categories = listOf("Theory", "Practice", "Revision", "Break")
                        categories.forEach { cat ->
                            val active = newTaskCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) CyberPrimaryPurple else Color.White.copy(alpha = 0.05f))
                                    .clickable { newTaskCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val durationVal = newTaskDuration.toIntOrNull() ?: 60
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addManualTask(newTaskTitle, newTaskCategory, durationVal, newTaskTime)
                            newTaskTitle = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("ADD TASK", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextMutedGray) }
            }
        )
    }
}

// ==========================================
// 6. DOUBT SOLVER SCREEN
// ==========================================
@Composable
fun DoubtSolverScreen(viewModel: PrepNexusViewModel) {
    val history by viewModel.solvedDoubts.collectAsStateWithLifecycle()
    val doubtState by viewModel.doubtState.collectAsStateWithLifecycle()

    var activeQuery by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Quick Exam") }
    var isWhiteboardActive by remember { mutableStateOf(false) }

    // Simulated voice doubtful fields
    var isRecordingSimulated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("doubt_solver_container")
    ) {
        Text("AI DOUBT SOLVER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Get instant explanations for IIT-JEE/NEET complexity or simple school topics", color = TextMutedGray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Search text doubt box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .background(DeepGlassCard)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = activeQuery,
                    onValueChange = { activeQuery = it },
                    placeholder = { Text("Type doubt, physics formula or math problem...", color = TextMutedGray, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberPrimaryPurple,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("doubt_query_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Explaining Styles Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val styles = listOf("Quick Exam", "Class 7", "IIT-Level", "Detailed")
                        styles.forEach { st ->
                            val active = selectedStyle == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (active) CyberCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .background(if (active) CyberCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .clickable { selectedStyle = st }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(st, color = if (active) CyberCyan else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Simulated interactive attachments helper
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Simulated voice input
                        IconButton(
                            onClick = {
                                isRecordingSimulated = !isRecordingSimulated
                                if (isRecordingSimulated) {
                                    activeQuery = "Explain Newton's third law with rocket animation analogy."
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRecordingSimulated) HotPink else Color.White.copy(alpha = 0.05f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecordingSimulated) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice recorder doubt solver link",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Simulated Image attachment OCR clicker
                        IconButton(
                            onClick = {
                                activeQuery = "Derive equation of kinetic friction coefficient under uniform slopes."
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "OCR handwritten image upload links", tint = CyberCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Button(
                    onClick = {
                        if (activeQuery.isNotBlank()) {
                            viewModel.askDoubt(activeQuery, selectedStyle)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple),
                    modifier = Modifier.fillMaxWidth().testTag("solve_doubt_btn")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Solve", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SOLVE DYNAMICALLY", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Whiteboard toggle trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Interactive Scratch Whiteboard", color = TextMutedGray, fontSize = 12.sp)
            Button(
                onClick = { isWhiteboardActive = !isWhiteboardActive },
                colors = ButtonDefaults.buttonColors(containerColor = if (isWhiteboardActive) HotPink else NeonElectricBlue),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(if (isWhiteboardActive) "Hide Scratchpad" else "Open AI Whiteboard", fontSize = 11.sp, color = Color.White)
            }
        }

        // Active solution space
        AnimatedVisibility(visible = isWhiteboardActive) {
            InteractiveWhiteboard()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Solution display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(DeepGlassCard)
                .padding(14.dp)
        ) {
            when (doubtState) {
                is AsyncState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("PrepNexus AI Tutor calculating step-wise results...", color = TextMutedGray, fontSize = 12.sp)
                    }
                }
                is AsyncState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎓 SOLVED ANSWER SUMMARY", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            IconButton(onClick = { viewModel.clearDoubtState() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextMutedGray)
                            }
                        }
                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = (doubtState as AsyncState.Success<String>).data,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                else -> {
                    // Solved query histories list
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("📜 SOLUTIONS SOLVED LOG", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (history.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Ask your first doubt to record logs", color = TextMutedGray, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(history) { record ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .clickable { viewModel.askDoubt(record.question, record.style) }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(record.question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(CyberCyan.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(record.style, color = CyberCyan, fontSize = 8.sp)
                                                }
                                            }
                                            Text(record.explanation, color = TextMutedGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Draw canvas whiteboard
@Composable
fun InteractiveWhiteboard() {
    var strokesList = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke = remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.6f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke.value = listOf(offset)
                        },
                        onDrag = { change, dragAmount ->
                            val newPoint = change.position
                            currentStroke.value = currentStroke.value + newPoint
                        },
                        onDragEnd = {
                            strokesList.add(currentStroke.value)
                            currentStroke.value = emptyList()
                        }
                    )
                }
        ) {
            // Draw finalized strokes
            strokesList.forEach { stroke ->
                if (stroke.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.first().x, stroke.first().y)
                        for (i in 1 until stroke.size) {
                            lineTo(stroke[i].x, stroke[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = CyberCyan,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            // Draw current active stroke
            if (currentStroke.value.size > 1) {
                val path = Path().apply {
                    moveTo(currentStroke.value.first().x, currentStroke.value.first().y)
                    for (i in 1 until currentStroke.value.size) {
                        lineTo(currentStroke.value[i].x, currentStroke.value[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = HotPink,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Controls overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { strokesList.clear() },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear brush lines", tint = Color.White, modifier = Modifier.size(14.dp))
            }
            IconButton(
                onClick = { if (strokesList.isNotEmpty()) strokesList.removeLast() },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo line drawing", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        
        Text("Doubt white scratchpad: Draw equations here", color = TextMutedGray, fontSize = 9.sp, modifier = Modifier.padding(6.dp))
    }
}

// ==========================================
// 7. NOTES SECTION (GENERATOR & FLASHCARDS)
// ==========================================
@Composable
fun NotesScreen(viewModel: PrepNexusViewModel) {
    val notes by viewModel.savedNotes.collectAsStateWithLifecycle()
    val notesState by viewModel.notesState.collectAsStateWithLifecycle()

    var noteTitle by remember { mutableStateOf("") }
    var noteContentText by remember { mutableStateOf("") }
    
    // Active focused generated note overlay
    var selectedNoteForViewing by remember { mutableStateOf<NoteRecord?>(null) }
    var activeNoteTab by remember { mutableStateOf("Summary") } // Summary, Mindmap, Flashcards, Questions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("notes_screen_container")
    ) {
        if (selectedNoteForViewing != null) {
            val record = selectedNoteForViewing!!
            // Full screen active note interaction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(record.title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberCyan, maxLines = 1)
                IconButton(onClick = { selectedNoteForViewing = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Note", tint = Color.White)
                }
            }

            // Note tabs selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val subTabs = listOf("Summary", "Mindmap", "Flashcards", "Questions")
                subTabs.forEach { tab ->
                    val active = activeNoteTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) CyberPrimaryPurple else Color.White.copy(alpha = 0.05f))
                            .clickable { activeNoteTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tab, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub tab details
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .background(DeepGlassCard)
                    .padding(14.dp)
            ) {
                when (activeNoteTab) {
                    "Summary" -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("✨ AI CHAPTER SUMMARY NOTES", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(record.summaryText, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                    "Mindmap" -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("🔗 DYNAMIC CONCEPT CORE MINDMAP", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Visual hierarchy parser representation
                            val nodes = record.mindmapNodesJson.split("\n")
                            nodes.forEach { node ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Hub, contentDescription = "Mindmap node link", tint = HotPink, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(node, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    "Flashcards" -> {
                        // Interactive Flashcard deck flip simulator
                        InteractiveFlashcardsDeck(record.flashcardsJson)
                    }
                    "Questions" -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("🎯 PREDICTED BOARD/IIT HIGH-YIELD QUESTIONS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(record.importantQuestions, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

        } else {
            // Summary generation input and existing logs
            Text("AI NOTES GENERATOR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Transform textbook chapters or text concepts into Summaries, Flashcards & Interactive Mindmaps", color = TextMutedGray, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Generator block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                    .background(DeepGlassCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Chapter Topic / Copied Textbook Data", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = { Text("Topic: e.g. Quantum Electrodynamics of Metals", color = TextMutedGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                    )

                    OutlinedTextField(
                        value = noteContentText,
                        onValueChange = { noteContentText = it },
                        placeholder = { Text("Paste chapter paragraphs, textbook screenshots text context, or class summaries...", color = TextMutedGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("note_content_input")
                    )

                    if (notesState is AsyncState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
                        Text("Reading pages & constructing mindmaps dynamically...", color = TextMutedGray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContentText.isNotBlank()) {
                                    viewModel.generateNotes(noteTitle, noteContentText)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier.fillMaxWidth().testTag("note_click_generate_btn")
                        ) {
                            Text("EXECUTE NOTE COMPRESSION", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Existing summaries collection
            Text("📚 YOUR RETRIEVABLE STUDY COGNITION SHEETS", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active revision notes compiled yet. Compress top text blocks!", color = TextMutedGray, fontSize = 11.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notes) { note ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .clickable { selectedNoteForViewing = note }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(note.summaryText, color = TextMutedGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Row {
                                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Inspect notes details link", tint = CyberCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        IconButton(onClick = { viewModel.deleteNote(note.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Trash", tint = TextMutedGray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Flip flashcard deck representation
@Composable
fun InteractiveFlashcardsDeck(flashcardsRaw: String) {
    // Parser: list of pairs
    val flashcardsList = remember(flashcardsRaw) {
        val cards = mutableListOf<Pair<String, String>>()
        val segments = flashcardsRaw.split(",")
        segments.forEach { s ->
            if (s.contains("|")) {
                val parts = s.split("|")
                val q = parts.getOrNull(0)?.replace("Q:", "")?.trim() ?: ""
                val a = parts.getOrNull(1)?.replace("A:", "")?.trim() ?: ""
                if (q.isNotEmpty()) cards.add(Pair(q, a))
            } else if (s.contains(":")) {
                val parts = s.split(":")
                val q = parts.getOrNull(0)?.trim() ?: ""
                val a = parts.getOrNull(1)?.trim() ?: ""
                if (q.isNotEmpty()) cards.add(Pair(q, a))
            }
        }
        if (cards.isEmpty()) {
            cards.add(Pair("How spacing matches your exam scores?", "Spaced recall reinforces dynamic links in memory retrieval."))
            cards.add(Pair("Does doubt white scratchpad support multi sketches?", "Yes! Draw any complex math expressions securely!"))
        }
        cards
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (flashcardsList.isNotEmpty()) {
            val currentPair = flashcardsList[currentIndex]

            Text("Card ${currentIndex + 1} of ${flashcardsList.size}", color = TextMutedGray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(14.dp))

            // Beautiful glowing interactive flip area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isFlipped = !isFlipped }
                    .background(
                        Brush.verticalGradient(
                            if (isFlipped) {
                                listOf(HotPink.copy(alpha = 0.2f), CyberPrimaryPurple.copy(alpha = 0.4f))
                            } else {
                                listOf(NeonElectricBlue.copy(alpha = 0.2f), CyberCyan.copy(alpha = 0.2f))
                            }
                        )
                    )
                    .border(2.dp, if (isFlipped) HotPink else CyberCyan, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFlipped) "💡 AI REVISION ANSWER" else "🔍 QUESTION FLIP",
                        color = if (isFlipped) HotPink else CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFlipped) currentPair.second else currentPair.first,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TAPPED TO FLIP MODULE", color = TextMutedGray, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        isFlipped = false
                        currentIndex = (currentIndex - 1 + flashcardsList.size) % flashcardsList.size
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Text("PREVIOUS", color = Color.White)
                }
                
                Button(
                    onClick = {
                        isFlipped = false
                        currentIndex = (currentIndex + 1) % flashcardsList.size
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("NEXT PASS", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. MORE MENU ROUTER
// ==========================================
@Composable
fun MoreMenuGrid(onSubScreenSelected: (MoreSubScreen) -> Unit) {
    val items = listOf(
        Triple(MoreSubScreen.MockTests, "Mock Test Series", "⭐ Live practice MCQs, accuracy tracking, real leaderboards"),
        Triple(MoreSubScreen.Analytics, "Ecosystem Analytics", "📈 Accuracy ratings,predicted All India Rank, mastery graph"),
        Triple(MoreSubScreen.YouTube, "PrepNexus YouTube Hub", "📺 Watch high-yield video lectures, strategies, and concept blueprints"),
        Triple(MoreSubScreen.Community, "Study Guild", "👥 Study groups, discussions, team streaks, competitions"),
        Triple(MoreSubScreen.Career, "Ultimate Careers", "🌳 IAS timelines, JEE metrics, medical roadmaps, salaries"),
        Triple(MoreSubScreen.Focus, "Zen Study Timer", "⏱️ Distraction appblock timer, soothing waveforms, ambient rain"),
        Triple(MoreSubScreen.Profile, "Gamified Achievements", "🏆 Badges, ranks, unlocks themes, system profile customization"),
        Triple(MoreSubScreen.Premium, "Subscription Portal", "💎 Unlock Advanced strategy, UPSC strategic keys, offline mode")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("more_grid_container")
    ) {
        Text("PREPNEXUS PORTAL UTILITIES", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Navigate through ultra-modern study command utilities", color = TextMutedGray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { (screen, title, desc) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepGlassCard)
                        .clickable { onSubScreenSelected(screen) }
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(desc, color = TextMutedGray, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Enter utilities screen link", tint = CyberCyan, modifier = Modifier.size(14.dp))
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ==========================================
// 8.1 MOCK TEST SERIES SCREEN
// ==========================================
@Composable
fun MockTestsScreen(viewModel: PrepNexusViewModel, onClose: () -> Unit) {
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val testScores by viewModel.testScores.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var activeSubject by remember { mutableStateOf("Physics Electromagnetism") }
    
    // Live execution state tracker
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var scoreTracker by remember { mutableStateOf(0) }
    var testStartTime by remember { mutableStateOf(0L) }
    var isVerifiedAnswerState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("mock_tests_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI MOCK TEST CENTER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close tests section link", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (quizState) {
            is AsyncState.Loading -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyberCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("PrepNexus Test Engine predicting MCQs...", color = TextMutedGray)
                }
            }
            is AsyncState.Success -> {
                val questions = (quizState as AsyncState.Success<List<QuizQuestion>>).data
                if (currentQuestionIdx < questions.size) {
                    val currentQ = questions[currentQuestionIdx]
                    
                    // Header metric
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Question ${currentQuestionIdx + 1} of ${questions.size}", color = CyberCyan, fontWeight = FontWeight.Bold)
                        Text("Score: $scoreTracker/$currentQuestionIdx", color = HotPink, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .background(DeepGlassCard)
                            .padding(16.dp)
                    ) {
                        Text(currentQ.question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Options list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentQ.options.forEachIndexed { idx, option ->
                            val isSelected = selectedOptionIdx == idx
                            val optionBorderColor = when {
                                isVerifiedAnswerState && idx == currentQ.correctAnswer -> Color.Green
                                isVerifiedAnswerState && isSelected && idx != currentQ.correctAnswer -> Color.Red
                                isSelected -> CyberCyan
                                else -> GlassBorder
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, optionBorderColor, RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                    .clickable(enabled = !isVerifiedAnswerState) { selectedOptionIdx = idx }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${('A'.code + idx).toChar()}) $option",
                                    color = if (isSelected) CyberCyan else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isVerifiedAnswerState) {
                        // Explanation block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(10.dp)
                        ) {
                            Text("💡 Explanation: ${currentQ.explanation}", color = TextMutedGray, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (currentQuestionIdx + 1 == questions.size) {
                                    val duration = ((System.currentTimeMillis() - testStartTime) / 1000).toInt()
                                    viewModel.finishQuiz(activeSubject, scoreTracker, questions.size, duration)
                                } else {
                                    currentQuestionIdx++
                                    selectedOptionIdx = null
                                    isVerifiedAnswerState = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (currentQuestionIdx + 1 == questions.size) "FINISH TEST" else "NEXT QUESTION", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (selectedOptionIdx != null) {
                                    isVerifiedAnswerState = true
                                    if (selectedOptionIdx == currentQ.correctAnswer) {
                                        scoreTracker++
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LOCK OPTION", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                } else {
                    // fallbacks
                    Text("No question packets active.", color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = { viewModel.cancelQuiz() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Abandon Test Session", color = HotPink)
                }
            }
            else -> {
                // Initialize Test View
                Text("Select Practice Core to AI-Generate Live MCQ Packet:", color = TextMutedGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = activeSubject,
                    onValueChange = { activeSubject = it },
                    placeholder = { Text("e.g. Kinematics Force Vectors", color = TextMutedGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (activeSubject.isNotBlank()) {
                            testStartTime = System.currentTimeMillis()
                            scoreTracker = 0
                            currentQuestionIdx = 0
                            selectedOptionIdx = null
                            isVerifiedAnswerState = false
                            viewModel.startQuiz(activeSubject, profile?.goal ?: "Competitive Board")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimaryPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("INITIALIZE TEST ENGAGEMENT", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Score board metrics logs
                Text("📜 COMPLAINED EXAM ATTEMPTS HISTORY", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (testScores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Perform your first practice mock above", color = TextMutedGray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(testScores) { score ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(score.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Score: ${score.score}/${score.totalQuestions} • Accuracy: ${score.accuracy}%", color = TextMutedGray, fontSize = 11.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyberCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("AIR Pred: #${score.airPrediction}", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8.2 ECOSYSTEM ANALYTICS SCREEN
// ==========================================
@Composable
fun AnalyticsScreen(viewModel: PrepNexusViewModel, onClose: () -> Unit) {
    val scores by viewModel.testScores.collectAsStateWithLifecycle()

    val averageAccuracy = remember(scores) {
        if (scores.isEmpty()) 0 else scores.map { it.accuracy }.average().toInt()
    }
    val bestPredictedAir = remember(scores) {
        if (scores.isEmpty()) 24500 else scores.map { it.airPrediction }.minOrNull() ?: 24500
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("analytics_screen_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI DIAGNOSTIC METRICS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close metrics section", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large predicted rank
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(CyberPrimaryPurple.copy(alpha = 0.3f), CyberCyan.copy(alpha = 0.3f))))
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CURRENT PREDICTED ALL INDIA RANK", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("#$bestPredictedAir", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White, fontSize = 48.sp)
                Text("Computed from live test accuracy & chapter speed dynamics", color = TextMutedGray, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Accuracy & metrics boxes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(DeepGlassCard, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("ACCURACY METER", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$averageAccuracy%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Threshold for target goal: 85%", color = TextMutedGray, fontSize = 8.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(DeepGlassCard, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("SPEED EFFICIENCY", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("34s / Q", color = HotPink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Topper Benchmark: 40s", color = TextMutedGray, fontSize = 8.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subject mastery visually-pleasing mock analytics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(DeepGlassCard)
                .padding(16.dp)
        ) {
            Column {
                Text("📐 SUBJECT MASTERY QUOTIENTS", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                val subjectsMock = listOf(
                    Triple("Physics Theory & Formulae", 84, CyberCyan),
                    Triple("Chemistry Organic Reactions", 62, CyberPrimaryPurple),
                    Triple("Mathematics Integrals speed", 91, HotPink)
                )

                subjectsMock.forEach { (sub, factor, clr) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(sub, color = Color.White, fontSize = 11.sp)
                            Text("$factor%", color = clr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { factor.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .padding(top = 2.dp),
                            color = clr,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        Text("Mock leaderboard is automatically adjusted server-side. Complete 2 more weekly notes generator sheets to boost rank calculation.", color = TextMutedGray, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

// ==========================================
// 8.3 STUDY GUILD / COMMUNITY SCREEN
// ==========================================
@Composable
fun CommunityScreen(onClose: () -> Unit) {
    val groups = listOf(
        Pair("IAS Mains Strategy Board", "2,450 active civil service aspirants"),
        Pair("IIT-JEE Physics Wallah Toppers", "12,940 core engineering candidates"),
        Pair("Class 10 CBSE Math Solutions", "8,100 high school pupils")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("community_screen_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("COMMUNITY LEARNING SWARM", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close community section links", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Global chat simulator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(DeepGlassCard)
                .padding(14.dp)
        ) {
            Column {
                Text("🔥 LIVE GLOBAL ASPIRANT DEBATE", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Row {
                            Text("👤 Topper_JEE_PW: ", color = HotPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Has anyone analyzed the relative weight of Inorganic in 2026?", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    item {
                        Row {
                            Text("👤 UPSC_Elite: ", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Focus deeply on newspaper summarizer blocks. Current Affairs contains 28% of total weight in GS1.", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    item {
                        Row {
                            Text("👤 Rank_1_Slayer: ", color = NeonGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("I generated active mindmaps of organic compounds using PrepNexus. Game changer!", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Joined groups List
        Text("🏢 ACTIVE ENGAGED GUILDS", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(180.dp)) {
            items(groups) { (title, members) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(members, color = TextMutedGray, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberPrimaryPurple.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ENTER GUILD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8.4 CAREER ROADMAP SECTION
// ==========================================
@Composable
fun CareerRoadmapScreen(onClose: () -> Unit) {
    var activeCareerTree by remember { mutableStateOf("IIT-JEE Engineering") }

    val timelines = when(activeCareerTree) {
        "IIT-JEE Engineering" -> listOf(
            Triple("Pre-Req Stage (Class 11/12)", "Master mechanics physics, calculus mathematics. Keep boards percentage above 75%.", "Prerequsites: NCERT foundation"),
            Triple("Entrance Stage", "Clear JEE Mains with 98+ percentile, then scale tough IIT advanced limits.", "JEE Advanced hurdle"),
            Triple("IIT Engineering Degree", "Pick CS/AI/Electronics branches in core premier IITs.", "Average CTC: 20-40 Lakhs INR"),
            Triple("Industrial Dominance", "Join tech giants globally or lead engineering startups.", "Senior Architect")
        )
        "UPSC IAS civil servant" -> listOf(
            Triple("Graduation Stage", "Obtain degree in any college domain. Pick optonal subject early.", "Age constraint: 21-32"),
            Triple("UPSC Prelims hurdle", "100 multiple choice questions. Pass CSAT qualification.", "CSAT + GS board papers"),
            Triple("UPSC Mains descriptive", "9 heavy subjective answer-writing tests. Practice speed daily.", "PrepNexus UPSC evaluation"),
            Triple("Civil Officer Appointment", "Inducted as IAS collector, guiding strategic development.", "Ultimate Indian prestige")
        )
        else -> listOf(
            Triple("Pre-Medical (Class 11/12)", "Focus deeply of Biology chapters, plant structures, and chemical formulas.", "NCERT Biology core"),
            Triple("NEET UG clear", "Aim for 680+ score out of 720 to lock top governmental general MBBS seats.", "Intense time constraint"),
            Triple("MBBS Hospital Residency", "5.5 years intense clinical drills. Pick specialization (Cardiology/Surgeron).", "Top tier medicine doctor"),
            Triple("Noble service scale", "Practice medicine in elite community hospitals.", "Noble life focus")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("career_roadmap_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("INTERACTIVE CAREER MATRICES", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close careers section limits", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Option picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val domains = listOf("IIT-JEE Engineering", "UPSC IAS civil servant", "NEET Medical")
            domains.forEach { dom ->
                val active = activeCareerTree == dom
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) CyberPrimaryPurple else Color.White.copy(alpha = 0.05f))
                        .clickable { activeCareerTree = dom }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(dom.split(" ").first(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive vertical tree timeline
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(timelines) { (title, prepDetails, benchmark) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .background(DeepGlassCard)
                        .padding(14.dp)
                ) {
                    Row {
                        // Left vertical node anchor representation
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(CyberCyan, CircleShape))
                            Box(modifier = Modifier.width(2.dp).height(60.dp).background(CyberPrimaryPurple))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(prepDetails, color = TextMutedGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(benchmark, color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8.5 DISTRACTION-FREE FOCUS MODE
// ==========================================
@Composable
fun FocusScreen(onClose: () -> Unit) {
    var timerSecondsRemaining by remember { mutableStateOf(1500) } // 25 min Pomodoro default
    var isTimerActive by remember { mutableStateOf(false) }
    var soundTrackSelected by remember { mutableStateOf("Rain Soothing sounds") }

    val context = LocalContext.current

    // Counting ticker loop
    LaunchedEffect(isTimerActive, timerSecondsRemaining) {
        if (isTimerActive && timerSecondsRemaining > 0) {
            delay(1000)
            timerSecondsRemaining -= 1
        } else if (timerSecondsRemaining == 0) {
            isTimerActive = false
        }
    }

    val formatTime = remember(timerSecondsRemaining) {
        val m = timerSecondsRemaining / 60
        val s = timerSecondsRemaining % 60
        String.format("%02d:%02d", m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("focus_space_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ZEN STUDY STATION", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close zen space", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Giant Circular Progress Timer container
        Box(
            modifier = Modifier
                .size(200.dp)
                .drawBehind {
                    drawCircle(
                        color = GlassBorder,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 12f)
                    )
                    drawArc(
                        color = if (isTimerActive) CyberCyan else CyberPrimaryPurple,
                        startAngle = -90f,
                        sweepAngle = (timerSecondsRemaining.toFloat() / 1500f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatTime, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, fontSize = 42.sp, color = Color.White)
                Text(if (isTimerActive) "FOCUS FLUID ACTIVE" else "STATION IDLE", color = TextMutedGray, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Play/Pause Action Row
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isTimerActive = !isTimerActive },
                colors = ButtonDefaults.buttonColors(containerColor = if (isTimerActive) HotPink else CyberPrimaryPurple)
            ) {
                Icon(imageVector = if (isTimerActive) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Timer play link")
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isTimerActive) "MUTE FOCUS TIMER" else "ENGAGE FOCUS TIMER", color = Color.White)
            }

            Button(
                onClick = {
                    isTimerActive = false
                    timerSecondsRemaining = 1500
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
            ) {
                Text("RESET")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Ambient sound pack selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .background(DeepGlassCard)
                .padding(16.dp)
        ) {
            Column {
                Text("🔊 AMBIENT BRAIN SOUNDSCAPES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                val tracks = listOf("Rain Soothing sounds", "Cyber Lofi beats", "Zen Forest meditation", "Nature Waves")
                tracks.forEach { trk ->
                    val selected = soundTrackSelected == trk
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                            .clickable { soundTrackSelected = trk }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(trk, color = if (selected) CyberCyan else Color.White, fontSize = 12.sp)
                        Icon(
                            imageVector = if (selected) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Status",
                            tint = if (selected) CyberCyan else TextMutedGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 8.6 PROFILE & GAMIFIED ACHIEVEMENTS SCREEN
// ==========================================
@Composable
fun ProfileSettingsScreen(viewModel: PrepNexusViewModel, onClose: () -> Unit) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val badges = listOf(
        Pair("First Doubt Solved", "Unlocked"),
        Pair("Focus Warrior Rank 3", "Unlocked"),
        Pair("Streak King Elite", "Blocked"),
        Pair("UPSC Advanced Strategic", "Blocked")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profile_section_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("COMMAND CENTER SETTINGS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close profile dashboard link", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Badge list achievements
        Text("🏆 LEVEL BADGES EARNED", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            badges.take(2).forEach { (name, status) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberPrimaryPurple.copy(alpha = 0.2f))
                        .border(1.dp, CyberCyan, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("🏅", fontSize = 28.sp)
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                        Text(status, color = CyberCyan, fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Shop Theme unlock mechanics inside settings
        Text("🪙 UNLOCK COSMIC INTERFACES", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Spend PrepNexus Study Coins to customize active styling colors:", color = TextMutedGray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val themesStore = listOf(
            Triple("Obsidian Gold theme", 100, "Rich royal palette design wrapper"),
            Triple("Ultraviolet Cyber glow", 250, "Cyber neon ambient borders"),
            Triple("Deep Oceanic command", 300, "Sea marine clean visual sheets")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(themesStore) { (title, cost, desc) ->
                val canAfford = (profile?.coins ?: 0) >= cost
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepGlassCard)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(desc, color = TextMutedGray, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                if (canAfford) {
                                    viewModel.spendCoins(cost)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) CyberCyan else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.height(32.dp),
                            enabled = canAfford
                        ) {
                            Text("${cost} Coins", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8.7 PREMIUM SUBSCRIPTION PAGE
// ==========================================
@Composable
fun PremiumSubscriptionPage(
    isSubscribed: Boolean,
    onToggleSubscribed: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("premium_subscription_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PREPNEXUS ELITE PORTAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close subscription page link", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large glowing coupon badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(HotPink, CyberPrimaryPurple)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👑 ELITE SYNDICATE KEY", color = CyberCyan, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 3.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (isSubscribed) "YOUR PORTAL IS UNLOCKED" else "UNLOCK THE STUDY MATRIX", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(if (isSubscribed) "Premium active status validated" else "Gain unlimited access to state tools", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // List perks
        Text("🔒 CORE ELITE ADVANTAGES INCLUDED", color = TextMutedGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(10.dp))

        val perks = listOf(
            "Unlimited Doubt Solving using optimized model engines",
            "Advanced multi-chapter mock test series & predicting AIR Ranks",
            "Personalized daily scheduling & UPSC answers strategies",
            "Exclusive digital custom whiteboard attachments"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            perks.forEach { perk ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = "Point", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(perk, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onToggleSubscribed,
            colors = ButtonDefaults.buttonColors(containerColor = if (isSubscribed) Color.Red else CyberCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, if (isSubscribed) Color.Transparent else CyberCyan, RoundedCornerShape(16.dp))
                .testTag("subscribe_action_btn")
        ) {
            Text(
                text = if (isSubscribed) "CANCEL PORTS SUBSCRIPTION" else "UNLOCK PORTAL: ₹199 / Month",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==========================================
// 8.8 PREPNEXUS YOUTUBE PORTAL
// ==========================================
@Composable
fun YouTubePortalScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val channelUrl = "https://youtube.com/@prepnexus26?si=CaSm7xr4f8WJ4O8E"

    val launchChannel = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("youtube_portal_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📺",
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "PREPNEXUS DIGITAL STUDIO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Official YouTube Learning Portal",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close YouTube portal link", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Channel Showcase Card with brand colors and rounded app logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF881919).copy(alpha = 0.6f), Color(0xFF1E1E1E)),
                        center = Offset(200f, 100f),
                        radius = 600f
                    )
                )
                .border(1.5.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.prep_nexus_logo),
                    contentDescription = "PrepNexus Channel Representation",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PrepNexus",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = "@prepnexus26",
                    color = Color(0xFFFF8A8A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "High-Quality Boards & Exam Blueprints • Comprehensive Video Lessons • Dynamic Visual Concepts",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = launchChannel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Icon Link",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SUBSCRIBE & WATCH ON YOUTUBE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Playlist / Lessons segment
        Text(
            text = "📂 FEATURED HIGH-YIELD VIDEO SESSIONS",
            color = TextMutedGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val videoLessons = listOf(
            Triple("CBSE Board Exam Topper Secrets", "14:26 mins • High-Yield Strategy", "A systematic breakdown on structuring boards papers to gain final execution edge."),
            Triple("AI-Powered Study Planning Blueprint", "10:15 mins • Board & UPSC Prep", "How to synthesize custom study schedules with our digital planner state machines."),
            Triple("Ultimate Speed Maths & Tracing Shortcuts", "18:40 mins • Numerical Excellence", "Never run out of test limits with quick math blueprints and tracing hacks."),
            Triple("Complex Syllabus Breakdown Rules", "12:05 mins • Concept Decoding", "Decoding IAS and JEE grade papers with layered focus methods and memory keys.")
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(videoLessons) { (title, subtitle, summary) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepGlassCard)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .clickable { launchChannel() }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = subtitle,
                                    color = Color(0xFFFF8A8A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = summary,
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch video lesson list element",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
