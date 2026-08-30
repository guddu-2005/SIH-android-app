package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// Simple App UI State for routing and dialog management
sealed class ActiveTab {
    object Home : ActiveTab()
    object Appts : ActiveTab()
    object Records : ActiveTab()
    object Support : ActiveTab()
}

class MainViewModel : ViewModel() {
    // Active navigation tab
    private val _activeTab = MutableStateFlow<ActiveTab>(ActiveTab.Home)
    val activeTab: StateFlow<ActiveTab> = _activeTab

    // Video call state
    private val _isInVideoCall = MutableStateFlow(false)
    val isInVideoCall: StateFlow<Boolean> = _isInVideoCall

    private val _videoCallDoctor = MutableStateFlow("Dr. Ananya Sharma")
    val videoCallDoctor: StateFlow<String> = _videoCallDoctor

    // Dialog state
    private val _activeDialog = MutableStateFlow<String?>(null)
    val activeDialog: StateFlow<String?> = _activeDialog

    // Quick Actions Counter / Interaction records
    private val _appointments = MutableStateFlow(listOf(
        Appointment("Dr. Ananya Sharma", "Today, 10:30 AM", "Cardiology Follow-up", "Completed")
    ))
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _symptomReport = MutableStateFlow<String?>(null)
    val symptomReport: StateFlow<String?> = _symptomReport

    private val _medsCount = MutableStateFlow(5)
    val medsCount: StateFlow<Int> = _medsCount

    fun changeTab(tab: ActiveTab) {
        _activeTab.value = tab
    }

    fun setVideoCall(active: Boolean) {
        _isInVideoCall.value = active
    }

    fun setVideoCall(active: Boolean, doctor: String) {
        _videoCallDoctor.value = doctor
        _isInVideoCall.value = active
    }

    fun showDialog(type: String?) {
        _activeDialog.value = type
    }

    fun addAppointment(doctor: String, time: String, reason: String) {
        val newList = _appointments.value.toMutableList()
        newList.add(0, Appointment(doctor, time, reason, "Scheduled"))
        _appointments.value = newList
    }

    fun submitSymptoms(symptoms: List<String>) {
        if (symptoms.isEmpty()) {
            _symptomReport.value = null
            return
        }
        val advice = when {
            symptoms.contains("Chest Pain") -> "Urgent: Please join the Video Call immediately or call emergency! Dr. Ananya is ready."
            symptoms.contains("Shortness of Breath") -> "Highly recommended to consult with our on-call cardiologist immediately."
            else -> "Drink plenty of water, rest, and keep tracking. Schedule a follow-up if symptoms persist for 24 hours."
        }
        _symptomReport.value = advice
    }

    fun refillMeds() {
        _medsCount.value = _medsCount.value + 10
    }
}

data class Appointment(
    val doctor: String,
    val time: String,
    val reason: String,
    val status: String
)

@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsState()
    val isInVideoCall by viewModel.isInVideoCall.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isInVideoCall) {
                BottomNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.changeTab(it) }
                )
            }
        },
        floatingActionButton = {
            if (!isInVideoCall && activeTab is ActiveTab.Home) {
                FloatingActionButton(
                    onClick = { viewModel.showDialog("emergency") },
                    containerColor = EmergencyRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(64.dp)
                        .testTag("emergency_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergency SOS Hot-line",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
                .padding(innerPadding)
        ) {
            // Main views based on Tab selection
            when (activeTab) {
                is ActiveTab.Home -> DashboardScreen(viewModel)
                is ActiveTab.Appts -> AppointmentsScreen(viewModel)
                is ActiveTab.Records -> RecordsScreen(viewModel)
                is ActiveTab.Support -> SupportScreen()
            }

            // High-fidelity full-screen immersive video call overlay
            AnimatedVisibility(
                visible = isInVideoCall,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                val videoCallDoctor by viewModel.videoCallDoctor.collectAsState()
                val (docImage, caption) = if (videoCallDoctor == "Dr. Sarah Jenkins") {
                    Pair(
                        R.drawable.dr_sarah_jenkins_1788084471314,
                        "Dr. Sarah: \"Hi Rahul, nice to see you. I have been analyzing your continuous heart rate monitors and ECG wave records. Your cardiovascular resilience is looking exceptionally solid. Let's examine your active stamina today.\""
                    )
                } else {
                    Pair(
                        R.drawable.img_doctor_avatar_1788083872163,
                        "Dr. Ananya: \"Hello Rahul, thank you for joining. I reviewed your recent complete blood count results. Everything looks optimal! How has your daily stamina been since the medication adjustment?\""
                    )
                }
                VideoCallOverlay(
                    doctorName = videoCallDoctor,
                    doctorImageRes = docImage,
                    captionText = caption,
                    onEndCall = { viewModel.setVideoCall(false) }
                )
            }

            // Dialog overlay managers
            when (activeDialog) {
                "emergency" -> EmergencyDialog(onDismiss = { viewModel.showDialog(null) })
                "book_appt" -> BookAppointmentDialog(viewModel = viewModel, onDismiss = { viewModel.showDialog(null) })
                "symptoms" -> SymptomsDialog(viewModel = viewModel, onDismiss = { viewModel.showDialog(null) })
                "records" -> RecordsDialog(onDismiss = { viewModel.showDialog(null) })
                "order_meds" -> OrderMedsDialog(viewModel = viewModel, onDismiss = { viewModel.showDialog(null) })
                "activity_cbc" -> ActivityDetailDialog(
                    title = "Complete Blood Count",
                    date = "Yesterday",
                    content = "Your CBC metrics are all within standard healthy ranges. White blood count (WBC), Red blood count (RBC), and Platelets are optimal.",
                    status = "Normal",
                    onDismiss = { viewModel.showDialog(null) }
                )
                "activity_refill" -> ActivityDetailDialog(
                    title = "Atorvastatin Refill Approved",
                    date = "3 days ago",
                    content = "Prescription updated. 10mg dosage refilled for 30 days. Ready for collection or express dispatch.",
                    status = "Ready",
                    onDismiss = { viewModel.showDialog(null) }
                )
            }
        }
    }
}

// Custom Top Navigation Header
@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Custom Plus Icon with leaf accent
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, CardActionBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(AccentTealBg, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Plus Logo",
                        tint = SecondaryCobalt,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Serene Care Home",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCobalt,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // Circular User Profile Icon (using the generated asset!)
        Image(
            painter = painterResource(id = R.drawable.img_user_avatar_1788083860628),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

// 1. Dashboard Tab Screen
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            DashboardHeader()
        }

        // Welcome & Daily Health Summary Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, Rahul",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Here is your daily health summary.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextGrey
                        )
                    )
                }

                // "All Good" Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(AccentTealBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All Good",
                        tint = AccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        )
                        Text(
                            text = "Good",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        )
                    }
                }
            }
        }

        // Upcoming Appointment Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("appointment_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box {
                    // Left border highlight
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(200.dp)
                            .background(SecondaryCobalt)
                            .align(Alignment.CenterStart)
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
                    ) {
                        // Title & Time Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UPCOMING APPOINTMENT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryCobalt,
                                    letterSpacing = 1.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AlertPink)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "In 15 mins",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AlertRedText
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Doctor info Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_doctor_avatar_1788083872163),
                                contentDescription = "Dr. Ananya Sharma",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CardActionBg, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Dr. Ananya Sharma",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkBlue
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Time",
                                        tint = TextGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Today, 10:30 AM",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextDarkBlue
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cardiology Follow-up",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextGrey,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Video Call Action Button
                        Button(
                            onClick = { viewModel.setVideoCall(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("join_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Join Video Call",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section Title
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkBlue
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // Quick Actions 2x2 Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        title = "Book Appt",
                        icon = Icons.Default.CalendarMonth,
                        iconColor = SecondaryCobalt,
                        modifier = Modifier.weight(1f).testTag("action_book_appt"),
                        onClick = { viewModel.showDialog("book_appt") }
                    )
                    QuickActionCard(
                        title = "Symptoms",
                        icon = Icons.Default.Healing,
                        iconColor = AccentTeal,
                        modifier = Modifier.weight(1f).testTag("action_symptoms"),
                        onClick = { viewModel.showDialog("symptoms") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        title = "Records",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = Color(0xFFFFB200),
                        modifier = Modifier.weight(1f).testTag("action_records"),
                        onClick = { viewModel.showDialog("records") }
                    )
                    QuickActionCard(
                        title = "Order Meds",
                        icon = Icons.Default.Medication,
                        iconColor = PrimaryNavy,
                        modifier = Modifier.weight(1f).testTag("action_order_meds"),
                        onClick = { viewModel.showDialog("order_meds") }
                    )
                }
            }
        }

        // Recent Activity Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkBlue
                    )
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryCobalt,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.changeTab(ActiveTab.Records) }
                )
            }
        }

        // Recent Activity Items
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showDialog("activity_cbc") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(CardActionBg, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Biotech,
                            contentDescription = "Blood Test",
                            tint = SecondaryCobalt,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Complete Blood Count",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Results ready • Yesterday",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextGrey
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = TextGrey
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showDialog("activity_refill") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AccentTealBg, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "Medication Refill",
                            tint = AccentTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Atorvastatin Refill",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prescription updated • 3 days ago",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextGrey
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = TextGrey
                    )
                }
            }
        }
    }
}

// Composable for grid cards in Quick Actions
@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardActionBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkBlue
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Custom Bottom Navigation Bar
@Composable
fun BottomNavigationBar(
    activeTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = activeTab is ActiveTab.Home,
            onClick = { onTabSelected(ActiveTab.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryCobalt,
                unselectedIconColor = TextGrey,
                selectedTextColor = SecondaryCobalt,
                unselectedTextColor = TextGrey,
                indicatorColor = CardActionBg
            )
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.Appts,
            onClick = { onTabSelected(ActiveTab.Appts) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Appts") },
            label = { Text("Appts") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryCobalt,
                unselectedIconColor = TextGrey,
                selectedTextColor = SecondaryCobalt,
                unselectedTextColor = TextGrey,
                indicatorColor = CardActionBg
            )
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.Records,
            onClick = { onTabSelected(ActiveTab.Records) },
            icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Records") },
            label = { Text("Records") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryCobalt,
                unselectedIconColor = TextGrey,
                selectedTextColor = SecondaryCobalt,
                unselectedTextColor = TextGrey,
                indicatorColor = CardActionBg
            )
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.Support,
            onClick = { onTabSelected(ActiveTab.Support) },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Support") },
            label = { Text("Support") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryCobalt,
                unselectedIconColor = TextGrey,
                selectedTextColor = SecondaryCobalt,
                unselectedTextColor = TextGrey,
                indicatorColor = CardActionBg
            )
        )
    }
}

// 2. Appointments Screen View
@Composable
fun AppointmentsScreen(viewModel: MainViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    var selectedTab by remember { mutableStateOf("Upcoming") }
    
    // States for custom diagnostic checks and interactive states
    var showTestConnectionDialog by remember { mutableStateOf(false) }
    var showRobertMenu by remember { mutableStateOf(false) }
    var showRobertChenCancelled by remember { mutableStateOf(false) }
    var selectedDoctorForAction by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        // High-Fidelity Header
        AppointmentsHeader()

        // Tab Segmented Control Capsule (Upcoming / Past)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(Color(0xFFEBF2FF), shape = RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == "Upcoming") Color.White else Color.Transparent)
                    .clickable { selectedTab = "Upcoming" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Upcoming",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "Upcoming") SecondaryCobalt else TextGrey
                    )
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == "Past") Color.White else Color.Transparent)
                    .clickable { selectedTab = "Past" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Past",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "Past") SecondaryCobalt else TextGrey
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == "Upcoming") {
                // 1. "Telehealth Ready" Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecondaryCobalt)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.15f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Signal Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Telehealth Ready",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Our video consults are optimized for low bandwidth (2G/3G) to ensure you always connect.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showTestConnectionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF2FF)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = SecondaryCobalt,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test Connection",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryCobalt
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Card 1: Dr. Sarah Jenkins (With left vertical border and full action suite)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box {
                            // Thick accent side strip
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(260.dp) // estimated height, or let matchParentSize dynamically
                                    .background(SecondaryCobalt)
                                    .align(Alignment.CenterStart)
                            )

                            Column(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Confirmed Badge
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AccentTealBg)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Confirmed",
                                            tint = AccentTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Confirmed",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = AccentTeal
                                            )
                                        )
                                    }

                                    // Doctor Photo (Generated high-fidelity!)
                                    Image(
                                        painter = painterResource(id = R.drawable.dr_sarah_jenkins_1788084471314),
                                        contentDescription = "Dr. Sarah Jenkins",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, CardActionBg, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Dr. Sarah Jenkins",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkBlue
                                    )
                                )
                                Text(
                                    text = "Cardiology Specialist",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Light ice-blue details container
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEBF2FF), shape = RoundedCornerShape(12.dp))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Date",
                                            tint = SecondaryCobalt,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tomorrow, 10:30 AM",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextDarkBlue
                                            )
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = "Type",
                                            tint = SecondaryCobalt,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Teleconsultation",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Button block
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.setVideoCall(true, "Dr. Sarah Jenkins") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Join Call",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            selectedDoctorForAction = "Dr. Sarah Jenkins"
                                            viewModel.showDialog("book_appt")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF2FF)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "Reschedule",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextDarkBlue
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Card 2: Dr. Robert Chen (Standard list-item format)
                if (!showRobertChenCancelled) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dr_robert_chen_1788084494476),
                                    contentDescription = "Dr. Robert Chen",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, CardActionBg, CircleShape)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dr. Robert Chen",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextDarkBlue
                                        )
                                    )
                                    Text(
                                        text = "General Physician",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = "Date",
                                            tint = TextGrey,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Oct 24, 2:00 PM",
                                            style = MaterialTheme.typography.labelMedium.copy(color = TextGrey)
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.labelMedium.copy(color = TextGrey)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Apartment,
                                            contentDescription = "Type",
                                            tint = TextGrey,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Clinic Visit",
                                            style = MaterialTheme.typography.labelMedium.copy(color = TextGrey)
                                        )
                                    }
                                }

                                Box {
                                    IconButton(onClick = { showRobertMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = TextDarkBlue
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showRobertMenu,
                                        onDismissRequest = { showRobertMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Reschedule Visit") },
                                            onClick = {
                                                showRobertMenu = false
                                                selectedDoctorForAction = "Dr. Robert Chen"
                                                viewModel.showDialog("book_appt")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Request Records") },
                                            onClick = {
                                                showRobertMenu = false
                                                viewModel.showDialog("records")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Cancel Visit") },
                                            onClick = {
                                                showRobertMenu = false
                                                showRobertChenCancelled = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Render dynamically created scheduled appointments
                val scheduledAppts = appointments.filter { it.status == "Scheduled" }
                if (scheduledAppts.isNotEmpty()) {
                    items(scheduledAppts) { appt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(AccentTealBg, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Scheduled Appt",
                                        tint = AccentTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appt.doctor,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextDarkBlue
                                        )
                                    )
                                    Text(
                                        text = appt.time,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                    )
                                    Text(
                                        text = appt.reason,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SecondaryCobalt,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardActionBg)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Scheduled",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SecondaryCobalt
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Past Tab selected
                val completedAppts = appointments.filter { it.status == "Completed" }

                // Default static Past Dr. Ananya
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_doctor_avatar_1788083872163),
                                contentDescription = "Dr. Ananya Sharma",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CardActionBg, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dr. Ananya Sharma",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkBlue
                                    )
                                )
                                Text(
                                    text = "Cardiology Follow-up",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                )
                                Text(
                                    text = "Yesterday, 10:30 AM",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGrey)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTealBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentTeal
                                    )
                                )
                            }
                        }
                    }
                }

                // Default static Past Dr. Sarah
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.dr_sarah_jenkins_1788084471314),
                                contentDescription = "Dr. Sarah Jenkins",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CardActionBg, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dr. Sarah Jenkins",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkBlue
                                    )
                                )
                                Text(
                                    text = "Routine Wellness Check",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                )
                                Text(
                                    text = "2 weeks ago",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGrey)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTealBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentTeal
                                    )
                                )
                            }
                        }
                    }
                }

                // Dynamically completed ones from VM
                items(completedAppts) { appt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(AccentTealBg, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed Appt",
                                    tint = AccentTeal
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appt.doctor,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkBlue
                                    )
                                )
                                Text(
                                    text = appt.reason,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                                )
                                Text(
                                    text = appt.time,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGrey)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTealBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentTeal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bandwidth diagnostic dialog
    if (showTestConnectionDialog) {
        Dialog(onDismissRequest = { showTestConnectionDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bandwidth Diagnostic",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    var currentStepText by remember { mutableStateOf("Checking signal quality...") }
                    var connectionStable by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(1200)
                        currentStepText = "Measuring packet latency (RTT)..."
                        delay(1200)
                        currentStepText = "Optimizing audio-visual compression..."
                        delay(1200)
                        currentStepText = "Completed. Optimized for 2G/3G low bandwidth!"
                        connectionStable = true
                    }

                    if (!connectionStable) {
                        CircularProgressIndicator(
                            color = SecondaryCobalt,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentStepText,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = AccentTeal,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connection Diagnosed!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkBlue
                            )
                        )
                        Text(
                            text = "Your connection is fully optimized. Experience crystal-clear, lag-free telehealth consultation smoothly.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showTestConnectionDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Awesome")
                        }
                    }
                }
            }
        }
    }
}

// Custom Top Navigation Header for Appointments Screen
@Composable
fun AppointmentsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, CardActionBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(AccentTealBg, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Plus Logo",
                        tint = SecondaryCobalt,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Appointments",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCobalt,
                    fontSize = 24.sp
                )
            )
        }

        // Circular User Profile Icon
        Image(
            painter = painterResource(id = R.drawable.img_user_avatar_1788083860628),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

// 3. Records Screen View (with Cholesterol Tracker Canvas chart)
@Composable
fun RecordsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Medical History & Lab Results",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextDarkBlue
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Track your bio-metrics and access clinical documentation.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick cholesterol trend card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total Cholesterol Trend (mg/dL)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkBlue
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Custom Graphic drawing for dynamic bio-metric tracking
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(AppBg, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid lines
                        drawLine(Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, height * 0.2f), end = androidx.compose.ui.geometry.Offset(width, height * 0.2f), strokeWidth = 1f)
                        drawLine(Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, height * 0.5f), end = androidx.compose.ui.geometry.Offset(width, height * 0.5f), strokeWidth = 1f)
                        drawLine(Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, height * 0.8f), end = androidx.compose.ui.geometry.Offset(width, height * 0.8f), strokeWidth = 1f)

                        // Data Points for Cholesterol: Jan (240), Feb (220), Mar (190), Apr (180), Today (165)
                        val points = listOf(
                            androidx.compose.ui.geometry.Offset(width * 0.05f, height * 0.15f),
                            androidx.compose.ui.geometry.Offset(width * 0.28f, height * 0.35f),
                            androidx.compose.ui.geometry.Offset(width * 0.51f, height * 0.65f),
                            androidx.compose.ui.geometry.Offset(width * 0.74f, height * 0.75f),
                            androidx.compose.ui.geometry.Offset(width * 0.95f, height * 0.85f)
                        )

                        // Draw trend line
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = SecondaryCobalt,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 6f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }

                        // Draw points
                        points.forEach { pt ->
                            drawCircle(color = AccentTeal, radius = 10f, center = pt)
                            drawCircle(color = Color.White, radius = 5f, center = pt)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jan", style = MaterialTheme.typography.labelSmall.copy(color = TextGrey))
                    Text("Feb", style = MaterialTheme.typography.labelSmall.copy(color = TextGrey))
                    Text("Mar", style = MaterialTheme.typography.labelSmall.copy(color = TextGrey))
                    Text("Apr", style = MaterialTheme.typography.labelSmall.copy(color = TextGrey))
                    Text("Today (165)", style = MaterialTheme.typography.labelSmall.copy(color = AccentTeal, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Document library list
        Text(
            text = "Active Documents",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextDarkBlue
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                DocumentItem("Lipid Panel Bloodwork Report", "PDF • 145 KB", "May 2026")
            }
            item {
                DocumentItem("Cardiology Specialist Consultation Note", "PDF • 2.1 MB", "Apr 2026")
            }
            item {
                DocumentItem("Annual Wellness Plan Outline", "PDF • 950 KB", "Jan 2026")
            }
        }
    }
}

@Composable
fun DocumentItem(title: String, type: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Document",
                tint = SecondaryCobalt,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkBlue
                    )
                )
                Text(text = "$type • $date", style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey))
            }
            Icon(Icons.Default.Download, contentDescription = "Download", tint = SecondaryCobalt)
        }
    }
}

// 4. Support Tab View
@Composable
fun SupportScreen() {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("Hello Rahul! Welcome to Serene Care Home support. How can we help you today?", false),
            ChatMessage("I would like to know if Dr. Ananya is available for a follow up chat tomorrow.", true),
            ChatMessage("Dr. Ananya is available from 9 AM to 12 PM tomorrow. You can tap 'Book Appt' on the home dashboard to schedule a time!", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Support Desk",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextDarkBlue
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Chat with our medical assistant and administrative staff.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Message board
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, CardActionBg, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 0.dp,
                                        bottomEnd = if (msg.isUser) 0.dp else 16.dp
                                    )
                                )
                                .background(if (msg.isUser) SecondaryCobalt else CardActionBg)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (msg.isUser) Color.White else TextDarkBlue,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ask support...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        chatMessages.add(ChatMessage(messageText, true))
                        val reply = "Thank you. An coordinator has been notified of your message: '$messageText'. We will respond within 15 minutes."
                        messageText = ""
                        chatMessages.add(ChatMessage(reply, false))
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SecondaryCobalt)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

// High-fidelity Immersive Video Call Screen
@Composable
fun VideoCallOverlay(
    doctorName: String = "Dr. Ananya Sharma",
    doctorImageRes: Int = R.drawable.img_doctor_avatar_1788083872163,
    captionText: String = "Dr. Ananya: \"Hello Rahul, thank you for joining. I reviewed your recent complete blood count results. Everything looks optimal! How has your daily stamina been since the medication adjustment?\"",
    onEndCall: () -> Unit
) {
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    val formatTime = String.format("%02d:%02d", seconds / 60, seconds % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen main video: Doctor's avatar rendered elegantly
        Image(
            painter = painterResource(id = doctorImageRes),
            contentDescription = "$doctorName Video Stream",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle gradient overlay on top of video stream for readable texts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Upper HUD: Doctor details and active timer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = doctorName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Cardiology Tele-Consultation",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE $formatTime",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Mini PIP Video: User avatar (representing your own camera feed)
        Card(
            modifier = Modifier
                .size(width = 110.dp, height = 150.dp)
                .align(Alignment.TopEnd)
                .padding(top = 110.dp, end = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_user_avatar_1788083860628),
                    contentDescription = "User front camera feed",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f))
                )
                Text(
                    text = "You",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Caption Track for realism
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 24.dp, end = 24.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = captionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Control Buttons HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Video off", tint = Color.White)
            }

            // End Call Red Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .background(EmergencyRed, CircleShape)
                    .testTag("end_call_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Video Call", tint = Color.White)
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
            }
        }
    }
}

// Dialogs

// A: Emergency Dialog
@Composable
fun EmergencyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AlertPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Alert",
                        tint = EmergencyRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "EMERGENCY SOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRed
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You are about to dial Serene Care emergency response unit. Our 24/7 on-call medical coordinator will connect instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextDarkBlue
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextDarkBlue)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text("Call Now", color = Color.White)
                    }
                }
            }
        }
    }
}

// B: Book Appointment Dialog
@Composable
fun BookAppointmentDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var doctorSelection by remember { mutableStateOf("Dr. Ananya Sharma") }
    var reasonText by remember { mutableStateOf("Cardiology Follow-up") }
    var showSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (!showSuccess) {
                    Text(
                        text = "Book Appointment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Select Specialist", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue))
                    Spacer(modifier = Modifier.height(8.dp))

                    val doctors = listOf("Dr. Ananya Sharma (Cardiology)", "Dr. Rajesh Kumar (General)", "Dr. Priya Patel (Pediatrics)")
                    doctors.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { doctorSelection = doc.substringBefore(" (") }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = doctorSelection == doc.substringBefore(" ("),
                                onClick = { doctorSelection = doc.substringBefore(" (") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(doc, color = TextDarkBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reason for Visit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue))
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel", color = TextDarkBlue)
                        }
                        Button(
                            onClick = {
                                viewModel.addAppointment(doctorSelection, "Tomorrow, 11:00 AM", reasonText)
                                showSuccess = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                        ) {
                            Text("Book")
                        }
                    }
                } else {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(AccentTealBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = AccentTeal, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Booked Successfully!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AccentTeal)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your appointment with $doctorSelection is confirmed for Tomorrow at 11:00 AM.",
                            textAlign = TextAlign.Center,
                            color = TextDarkBlue
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                        ) {
                            Text("Great")
                        }
                    }
                }
            }
        }
    }
}

// C: Symptoms Checker Dialog
@Composable
fun SymptomsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val symptomsList = listOf("Headache", "Fever", "Chest Pain", "Cough", "Fatigue")
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    val symptomReport by viewModel.symptomReport.collectAsState()

    Dialog(onDismissRequest = {
        viewModel.submitSymptoms(emptyList()) // clear
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Symptom Tracker",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select all the symptoms you are currently experiencing for guidance.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                )
                Spacer(modifier = Modifier.height(16.dp))

                symptomsList.forEach { symptom ->
                    val isChecked = selectedSymptoms.contains(symptom)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isChecked) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(symptom, color = TextDarkBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (symptomReport != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (symptomReport!!.startsWith("Urgent")) AlertPink else AccentTealBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = symptomReport!!,
                            color = if (symptomReport!!.startsWith("Urgent")) AlertRedText else AccentTeal,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.submitSymptoms(emptyList())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = TextDarkBlue)
                    }
                    if (symptomReport == null) {
                        Button(
                            onClick = { viewModel.submitSymptoms(selectedSymptoms) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                        ) {
                            Text("Analyze")
                        }
                    }
                }
            }
        }
    }
}

// D: Records Dialog
@Composable
fun RecordsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Lab Certificates",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verify your medical reports via encrypted blockchain QR codes.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Custom Canvas QR Code Drawing
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .align(Alignment.CenterHorizontally)
                        .border(1.dp, CardActionBg, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val size = size.width
                        // Draw mock QR pixels
                        val steps = 8
                        val stepSize = size / steps
                        for (i in 0 until steps) {
                            for (j in 0 until steps) {
                                if ((i + j) % 2 == 0 || (i == 0 && j == 0) || (i == steps - 1 && j == 0) || (i == 0 && j == steps - 1) || (i == steps - 1 && j == steps - 1)) {
                                    drawRect(
                                        color = TextDarkBlue,
                                        topLeft = androidx.compose.ui.geometry.Offset(i * stepSize, j * stepSize),
                                        size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// E: Order Meds Dialog
@Composable
fun OrderMedsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val medsCount by viewModel.medsCount.collectAsState()
    var refilled by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Medication Refill",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Atorvastatin 10mg",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                )
                Text(
                    text = "Active prescription: Take 1 capsule daily before bed.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remaining Capsules:", color = TextDarkBlue, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (medsCount <= 5) AlertPink else AccentTealBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$medsCount Capsules",
                            color = if (medsCount <= 5) AlertRedText else AccentTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!refilled) {
                    Button(
                        onClick = {
                            viewModel.refillMeds()
                            refilled = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                    ) {
                        Text("Order Express Refill (+10 Day Supply)")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentTealBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Refill request approved and dispatched. Your capsules count is updated to $medsCount.",
                            color = AccentTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// F: Generic Activity Detail Dialog
@Composable
fun ActivityDetailDialog(
    title: String,
    date: String,
    content: String,
    status: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity Log",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextGrey, fontWeight = FontWeight.Bold)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentTealBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status,
                            color = AccentTeal,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDarkBlue)
                )
                Text(
                    text = "Logged $date",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextDarkBlue, lineHeight = 24.sp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCobalt)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    MyApplicationTheme {
        MainAppScreen()
    }
}
