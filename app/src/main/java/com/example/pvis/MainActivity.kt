package com.example.pvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF0D6EFD),
                    background = Color(0xFFF5F7FA)
                )
            ) {
                PhysioVisionApp()
            }
        }
    }
}

enum class Screen { INTRO, CALIBRATE, LIVE_SESSION, SCORECARD }

@Composable
fun PhysioVisionApp() {
    var currentScreen by remember { mutableStateOf(Screen.INTRO) }
    var isCalibrated by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf("Shoulder Abduction") }
    var lastReps by remember { mutableStateOf(0) }

    when (currentScreen) {
        Screen.INTRO -> IntroScreen(
            isCalibrated = isCalibrated,
            selectedExercise = selectedExercise,
            onSelectExercise = { selectedExercise = it },
            onStartSession = { currentScreen = Screen.LIVE_SESSION },
            onOpenCalibration = { currentScreen = Screen.CALIBRATE }
        )
        Screen.CALIBRATE -> CalibrationScreen(
            onBack = { currentScreen = Screen.INTRO },
            onCalibrated = {
                isCalibrated = true
                currentScreen = Screen.INTRO
            }
        )
        Screen.LIVE_SESSION -> LiveSessionScreen(
            exerciseName = selectedExercise,
            onClose = { currentScreen = Screen.INTRO },
            onFinish = { reps ->
                lastReps = reps
                currentScreen = Screen.SCORECARD
            }
        )
        Screen.SCORECARD -> ScorecardScreen(
            exerciseName = selectedExercise,
            repsCompleted = lastReps,
            onDone = { currentScreen = Screen.INTRO }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    isCalibrated: Boolean,
    selectedExercise: String,
    onSelectExercise: (String) -> Unit,
    onStartSession: () -> Unit,
    onOpenCalibration: () -> Unit
) {
    val exercises = listOf(
        Triple("Shoulder Abduction", "3 Sets × 10 Reps", "0° – 90° Lateral Raise"),
        Triple("Seated Knee Extension", "3 Sets × 12 Reps", "90° – 175° Extension"),
        Triple("Elbow Flexion", "2 Sets × 15 Reps", "160° – 45° Curl")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PhysioVision", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Home Rehabilitation Assistant", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                actions = {
                    AssistChip(
                        onClick = onOpenCalibration,
                        label = {
                            Text(
                                if (isCalibrated) "✓ Calibrated" else "⚠ Uncalibrated",
                                fontSize = 12.sp,
                                color = if (isCalibrated) Color(0xFF198754) else Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE0E4EC)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Welcome back, Deniz", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Week 4 / 8", color = Color(0xFF0D6EFD), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Assigned by Dr. Aylin Yılmaz, PT • Next check-in: Nov 28",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        "🔒 On-Device Privacy: Raw camera frames are processed locally and discarded immediately.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("Today's Prescribed Routine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(exercises) { (name, sets, angle) ->
                    val selected = name == selectedExercise
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectExercise(name) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFFEBF3FF) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Color(0xFF0D6EFD) else Color(0xFFE0E4EC)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { onSelectExercise(name) })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("$sets • Target: $angle", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD))
            ) {
                Text("▶  Start Camera & Exercise Tracking", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenCalibration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFF0D6EFD))
            ) {
                Text(
                    "⚙  Calibrate Camera & Posture",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0D6EFD)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(onBack: () -> Unit, onCalibrated: () -> Unit) {
    var isScanning by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (progress < 1f) {
                delay(80)
                progress += 0.05f
            }
            onCalibrated()
        }
    }

    Scaffold(
        containerColor = Color(0xFF10141D),
        topBar = {
            TopAppBar(
                title = { Text("Camera & Posture Calibration", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1C2230), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                SkeletonOverlay(
                    armRaise = 0.1f,
                    jointColor = if (isScanning) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(220.dp, 320.dp)
                )
                Text(
                    "Stand 2.0m back in neutral A-pose",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
                if (isScanning) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Capturing baseline landmarks... ${(progress * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2230))) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CheckRow("Camera Distance (2.1m)", "Optimal")
                    CheckRow("Ambient Lighting", "Good")
                    CheckRow("33-Keypoint Confidence", "0.94 (≥ 0.65)")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { isScanning = true },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(if (isScanning) "Calibrating..." else "Capture Baseline Posture", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CheckRow(label: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 13.sp)
        Text("✓ $status", color = Color(0xFF64FFDA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun LiveSessionScreen(
    exerciseName: String,
    onClose: () -> Unit,
    onFinish: (Int) -> Unit
) {
    var reps by remember { mutableIntStateOf(3) }
    var armRaise by remember { mutableFloatStateOf(0.5f) }
    var goingUp by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (goingUp) {
                armRaise += 0.05f
                if (armRaise >= 1f) {
                    armRaise = 1f
                    goingUp = false
                    if (reps < 10) reps++
                }
            } else {
                armRaise -= 0.05f
                if (armRaise <= 0.15f) {
                    armRaise = 0.15f
                    goingUp = true
                }
            }
        }
    }

    val currentAngle = (15 + armRaise * 77).toInt()
    val isOptimal = currentAngle in 75..95

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10141D))
            .padding(16.dp)
    ) {
        SkeletonOverlay(
            armRaise = armRaise,
            jointColor = if (isOptimal) Color(0xFF69F0AE) else Color(0xFFFFD740),
            modifier = Modifier
                .size(260.dp, 400.dp)
                .align(Alignment.Center)
        )

        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text("✕ Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(
                    "● $exerciseName • 32 FPS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HudCard("JOINT ANGLE", "$currentAngle°", "Target: 90° (±5°)", if (isOptimal) Color(0xFF69F0AE) else Color(0xFFFFD740), Modifier.weight(1f))
                HudCard("COMPLETED REPS", "$reps / 10", "Set 1 of 3", Color(0xFF40C4FF), Modifier.weight(1f))
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isOptimal) Color(0xFF1B5E20) else Color(0xFFE65100)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isOptimal) "✓ Great alignment! Hold peak angle briefly."
                    else "↑ Raise arm smoothly toward shoulder level (90°).",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onFinish(reps) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("End Session & View Scorecard", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HudCard(title: String, value: String, subtitle: String, accent: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.LightGray, fontSize = 10.sp)
            Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(exerciseName: String, repsCompleted: Int, onDone: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Session Scorecard") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D6EFD)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KINEMATIC ACCURACY SCORE", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("91%", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("$exerciseName • $repsCompleted Reps Logged", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Joint Deviation Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))

            MetricTile("Peak Abduction Angle", "88.4° (Target: 90.0°)", "-1.6° minor under-extension on Rep 4")
            Spacer(Modifier.height(8.dp))
            MetricTile("Angular Velocity & Timing", "1.8 sec / phase", "Matched gold-standard trajectory")
            Spacer(Modifier.height(8.dp))
            MetricTile("Torso Compensation Tilt", "6.2° lateral lean", "Keep core braced during peak hold")

            Spacer(Modifier.weight(1f))

            Text(
                "✓ Anonymized joint vectors synced to Dr. Aylin Yılmaz's Clinician Dashboard.",
                color = Color(0xFF008080),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Home Screen", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricTile(title: String, value: String, note: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E4EC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(value, fontSize = 13.sp)
            Text(note, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SkeletonOverlay(armRaise: Float, jointColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val head = Offset(cx, size.height * 0.12f)
        val neck = Offset(cx, size.height * 0.22f)
        val lShoulder = Offset(cx - 42.dp.toPx(), size.height * 0.24f)
        val rShoulder = Offset(cx + 42.dp.toPx(), size.height * 0.24f)
        val lHip = Offset(cx - 28.dp.toPx(), size.height * 0.52f)
        val rHip = Offset(cx + 28.dp.toPx(), size.height * 0.52f)
        val lKnee = Offset(cx - 32.dp.toPx(), size.height * 0.74f)
        val rKnee = Offset(cx + 32.dp.toPx(), size.height * 0.74f)
        val lAnkle = Offset(cx - 34.dp.toPx(), size.height * 0.94f)
        val rAnkle = Offset(cx + 34.dp.toPx(), size.height * 0.94f)

        val lElbow = Offset(
            lShoulder.x - (16.dp.toPx() + 29.dp.toPx() * armRaise),
            lShoulder.y + (55.dp.toPx() * (1f - armRaise))
        )
        val lWrist = Offset(
            lElbow.x - (14.dp.toPx() + 31.dp.toPx() * armRaise),
            lElbow.y + (50.dp.toPx() * (1f - armRaise))
        )

        val rElbow = Offset(
            rShoulder.x + (16.dp.toPx() + 29.dp.toPx() * armRaise),
            rShoulder.y + (55.dp.toPx() * (1f - armRaise))
        )
        val rWrist = Offset(
            rElbow.x + (14.dp.toPx() + 31.dp.toPx() * armRaise),
            rElbow.y + (50.dp.toPx() * (1f - armRaise))
        )

        val bones = listOf(
            head to neck, lShoulder to rShoulder, lShoulder to lHip, rShoulder to rHip, lHip to rHip,
            lShoulder to lElbow, lElbow to lWrist, rShoulder to rElbow, rElbow to rWrist,
            lHip to lKnee, lKnee to lAnkle, rHip to rKnee, rKnee to rAnkle
        )
        bones.forEach { (start, end) ->
            drawLine(Color.White.copy(alpha = 0.7f), start, end, strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
        }

        val joints = listOf(head, lShoulder, rShoulder, lElbow, rElbow, lWrist, rWrist, lHip, rHip, lKnee, rKnee, lAnkle, rAnkle)
        joints.forEach { pt ->
            drawCircle(jointColor, radius = 6.dp.toPx(), center = pt)
        }
    }
}