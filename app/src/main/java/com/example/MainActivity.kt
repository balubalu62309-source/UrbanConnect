package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.ServiceProvider
import com.example.data.model.UserRole
import com.example.ui.components.RoleSwitchHeader
import com.example.ui.screens.ActiveBookingTrackingScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.BookingCheckoutScreen
import com.example.ui.screens.CustomerBookingsScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.ProviderDashboardScreen
import com.example.ui.screens.ProviderDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.UrbanViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UrbanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                UrbanConnectApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab {
    HOME,
    BOOKINGS,
    URBAN_AI
}

@Composable
fun UrbanConnectApp(viewModel: UrbanViewModel) {
    val userRole by viewModel.userRole.collectAsState()
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Navigation Stack Details
    var selectedProviderForDetail by remember { mutableStateOf<ServiceProvider?>(null) }
    var checkoutDetails by remember {
        mutableStateOf<CheckoutArgs?>(null)
    }
    var trackingBooking by remember { mutableStateOf<Booking?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            Surface(
                color = Color(0xFFF7F9FC),
                shadowElevation = 0.dp
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    RoleSwitchHeader(
                        currentRole = userRole,
                        onRoleSelected = { role ->
                            viewModel.setRole(role)
                            selectedProviderForDetail = null
                            checkoutDetails = null
                            trackingBooking = null
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (userRole == UserRole.CUSTOMER && selectedProviderForDetail == null && checkoutDetails == null && trackingBooking == null) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color(0xFFE8F0FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.HOME,
                        onClick = { currentTab = NavigationTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.BOOKINGS,
                        onClick = { currentTab = NavigationTab.BOOKINGS },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Bookings") },
                        label = { Text("Bookings") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.URBAN_AI,
                        onClick = { currentTab = NavigationTab.URBAN_AI },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "UrbanAI") },
                        label = { Text("UrbanAI") },
                        colors = navItemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (userRole) {
                UserRole.SERVICE_PROVIDER -> {
                    ProviderDashboardScreen(viewModel = viewModel)
                }
                UserRole.ADMIN -> {
                    AdminDashboardScreen(viewModel = viewModel)
                }
                UserRole.CUSTOMER -> {
                    when {
                        trackingBooking != null -> {
                            ActiveBookingTrackingScreen(
                                booking = trackingBooking!!,
                                viewModel = viewModel,
                                onBackClick = { trackingBooking = null }
                            )
                        }
                        checkoutDetails != null -> {
                            BookingCheckoutScreen(
                                provider = checkoutDetails!!.provider,
                                scheduledDate = checkoutDetails!!.scheduledDate,
                                scheduledTime = checkoutDetails!!.scheduledTime,
                                address = checkoutDetails!!.address,
                                notes = checkoutDetails!!.notes,
                                isEmergency = checkoutDetails!!.isEmergency,
                                viewModel = viewModel,
                                onBackClick = { checkoutDetails = null },
                                onBookingSuccess = { createdBooking ->
                                    checkoutDetails = null
                                    selectedProviderForDetail = null
                                    trackingBooking = createdBooking
                                }
                            )
                        }
                        selectedProviderForDetail != null -> {
                            ProviderDetailScreen(
                                provider = selectedProviderForDetail!!,
                                viewModel = viewModel,
                                onBackClick = { selectedProviderForDetail = null },
                                onProceedToCheckout = { date, time, addr, notes, isEm ->
                                    checkoutDetails = CheckoutArgs(
                                        provider = selectedProviderForDetail!!,
                                        scheduledDate = date,
                                        scheduledTime = time,
                                        address = addr,
                                        notes = notes,
                                        isEmergency = isEm
                                    )
                                }
                            )
                        }
                        else -> {
                            when (currentTab) {
                                NavigationTab.HOME -> {
                                    CustomerHomeScreen(
                                        viewModel = viewModel,
                                        onCategoryClick = { /* Category filtered */ },
                                        onProviderClick = { provider ->
                                            selectedProviderForDetail = provider
                                        },
                                        onEmergencyClick = {
                                            currentTab = NavigationTab.URBAN_AI
                                        },
                                        onAiAssistClick = {
                                            currentTab = NavigationTab.URBAN_AI
                                        },
                                        onBookingClick = { booking ->
                                            trackingBooking = booking
                                        }
                                    )
                                }
                                NavigationTab.BOOKINGS -> {
                                    CustomerBookingsScreen(
                                        viewModel = viewModel,
                                        onTrackBookingClick = { booking ->
                                            trackingBooking = booking
                                        }
                                    )
                                }
                                NavigationTab.URBAN_AI -> {
                                    AiAssistantScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CheckoutArgs(
    val provider: ServiceProvider,
    val scheduledDate: String,
    val scheduledTime: String,
    val address: String,
    val notes: String,
    val isEmergency: Boolean
)
