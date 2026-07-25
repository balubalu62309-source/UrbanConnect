package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.getIconForProfession
import com.example.ui.viewmodel.UrbanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBookingTrackingScreen(
    booking: Booking,
    viewModel: UrbanViewModel,
    onBackClick: () -> Unit
) {
    val messagesState = viewModel.getMessagesForBooking(booking.id).collectAsState()
    val messages = messagesState.value

    var chatInput by remember { mutableStateOf("") }
    var showCallDialog by remember { mutableStateOf(false) }
    var showInvoiceModal by remember { mutableStateOf(false) }
    var showRatingModal by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(5f) }
    var reviewText by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Technician Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInvoiceModal = true }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Invoice", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Simulated Visual GPS Tracking Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                // Map Background Grid Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw grid lines simulating roads
                    for (i in 0..6) {
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, height * (i / 6f)),
                            end = Offset(width, height * (i / 6f)),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(width * (i / 6f), 0f),
                            end = Offset(width * (i / 6f), height),
                            strokeWidth = 3f
                        )
                    }

                    // Route line
                    val path = Path().apply {
                        moveTo(width * 0.25f, height * 0.7f)
                        quadraticTo(width * 0.45f, height * 0.3f, width * 0.75f, height * 0.35f)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF2563EB),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    )
                }

                // Technician Pulsing Location Marker
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 70.dp, top = 60.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Technician GPS",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Customer Destination Marker
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 60.dp, top = 20.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDC2626),
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Destination",
                                tint = Color.White
                            )
                        }
                    }
                }

                // ETA Badge Overlay
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (booking.status == "COMPLETED") "Job Completed" else "ETA: 12 mins • En Route",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Technician Summary & Quick Action Buttons
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForProfession(booking.providerProfession),
                                contentDescription = booking.providerName,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(booking.providerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedBadge()
                            }
                            Text("${booking.providerProfession} • Order #${booking.bookingNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Call Technician Button
                    IconButton(
                        onClick = { showCallDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Status Step Progress Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val steps = listOf("Accepted", "En Route", "Arrived", "In Progress", "Completed")
                    val currentStepIndex = when (booking.status) {
                        "PENDING" -> 0
                        "ACCEPTED" -> 0
                        "EN_ROUTE" -> 1
                        "ARRIVED" -> 2
                        "IN_PROGRESS" -> 3
                        "COMPLETED" -> 4
                        else -> 1
                    }

                    steps.forEachIndexed { index, step ->
                        val isDone = index <= currentStepIndex
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(step, fontSize = 10.sp, fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Chat Messages History
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isCustomer = msg.senderType == "CUSTOMER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCustomer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomer) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isCustomer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Message ${booking.providerName}...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendChatMessage(booking.id, chatInput)
                            chatInput = ""
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }

    // Call Dialog Simulation
    if (showCallDialog) {
        AlertDialog(
            onDismissRequest = { showCallDialog = false },
            title = { Text("Call ${booking.providerName}") },
            text = { Text("Simulating in-app encrypted voice call with ${booking.providerName} (${booking.providerPhone}).") },
            confirmButton = {
                Button(onClick = { showCallDialog = false }) {
                    Text("End Call")
                }
            }
        )
    }

    // Digital Invoice Modal
    if (showInvoiceModal) {
        AlertDialog(
            onDismissRequest = { showInvoiceModal = false },
            title = { Text("Digital Invoice #${booking.bookingNumber}") },
            text = {
                Column {
                    Text("Customer: ${booking.customerName}")
                    Text("Provider: ${booking.providerName} (${booking.providerProfession})")
                    Text("Date: ${booking.scheduledDate} at ${booking.scheduledTime}")
                    Text("Address: ${booking.customerAddress}")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Base Cost: $${String.format("%.2f", booking.baseCost)}")
                    if (booking.isEmergency) {
                        Text("Emergency Fee: +$${String.format("%.2f", booking.emergencyFee)}", color = Color(0xFFDC2626))
                    }
                    if (booking.discountAmount > 0) {
                        Text("Discount: -$${String.format("%.2f", booking.discountAmount)}", color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total Paid: $${String.format("%.2f", booking.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                Button(onClick = { showInvoiceModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}
