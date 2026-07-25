package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.components.getIconForProfession
import com.example.ui.viewmodel.UrbanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBookingsScreen(
    viewModel: UrbanViewModel,
    onTrackBookingClick: (Booking) -> Unit
) {
    val bookings by viewModel.allBookings.collectAsState()
    var selectedTab by remember { mutableStateOf("ACTIVE") } // ACTIVE or COMPLETED

    val activeList = bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
    val historyList = bookings.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }

    var ratingModalBooking by remember { mutableStateOf<Booking?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }

    var disputeModalBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedReason by remember { mutableStateOf("Poor Work Quality") }
    var disputeDetailsText by remember { mutableStateOf("") }
    var disputeEvidenceText by remember { mutableStateOf("") }
    var selectedDesiredResolution by remember { mutableStateOf("Full Refund") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Service Bookings", fontWeight = FontWeight.Bold) },
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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val tabs = listOf("ACTIVE" to "Active Jobs (${activeList.size})", "COMPLETED" to "History (${historyList.size})")
                    tabs.forEach { (tabKey, label) ->
                        val isSelected = selectedTab == tabKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            val currentList = if (selectedTab == "ACTIVE") activeList else historyList

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No bookings found", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Book verified professionals from the Home screen!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { booking ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconForProfession(booking.providerProfession),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(booking.providerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("${booking.categoryName} • ${booking.bookingNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (booking.status) {
                                            "COMPLETED" -> MaterialTheme.colorScheme.secondaryContainer
                                            "ACCEPTED", "EN_ROUTE" -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = booking.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = when (booking.status) {
                                                "COMPLETED" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                "ACCEPTED", "EN_ROUTE" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date & Time: ${booking.scheduledDate} at ${booking.scheduledTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$${String.format("%.2f", booking.totalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                if (booking.disputeStatus != "NONE") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = when (booking.disputeStatus) {
                                            "OPEN" -> Color(0xFFFEF3C7)
                                            "RESOLVED_FULL_REFUND" -> Color(0xFFDCFCE7)
                                            "RESOLVED_PARTIAL_REFUND" -> Color(0xFFE0F2FE)
                                            else -> Color(0xFFF1F5F9)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Gavel,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = when (booking.disputeStatus) {
                                                    "OPEN" -> Color(0xFFD97706)
                                                    "RESOLVED_FULL_REFUND" -> Color(0xFF16A34A)
                                                    "RESOLVED_PARTIAL_REFUND" -> Color(0xFF0284C7)
                                                    else -> Color(0xFF64748B)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (booking.disputeStatus) {
                                                    "OPEN" -> "Dispute Filed • Mediation Under Review"
                                                    "RESOLVED_FULL_REFUND" -> "Dispute Resolved • Full Refund Issued"
                                                    "RESOLVED_PARTIAL_REFUND" -> "Dispute Resolved • Partial Refund Issued"
                                                    else -> "Dispute Closed • No Refund"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (booking.disputeStatus) {
                                                    "OPEN" -> Color(0xFF92400E)
                                                    "RESOLVED_FULL_REFUND" -> Color(0xFF15803D)
                                                    "RESOLVED_PARTIAL_REFUND" -> Color(0xFF0369A1)
                                                    else -> Color(0xFF475569)
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (booking.status == "COMPLETED" && booking.disputeStatus == "NONE") {
                                        OutlinedButton(
                                            onClick = {
                                                selectedReason = "Poor Work Quality"
                                                disputeDetailsText = ""
                                                disputeEvidenceText = ""
                                                selectedDesiredResolution = "Full Refund"
                                                disputeModalBooking = booking
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                        ) {
                                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("File Dispute", fontSize = 11.sp)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (booking.status != "COMPLETED") {
                                            Button(
                                                onClick = { onTrackBookingClick(booking) },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Track Technician")
                                            }
                                        } else {
                                            if (booking.ratingGiven == 0f) {
                                                OutlinedButton(
                                                    onClick = { ratingModalBooking = booking },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rate & Review")
                                                }
                                            } else {
                                                Text("★ Rated ${booking.ratingGiven}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
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
    }

    // Rating & Review Modal
    if (ratingModalBooking != null) {
        AlertDialog(
            onDismissRequest = { ratingModalBooking = null },
            title = { Text("Rate ${ratingModalBooking!!.providerName}") },
            text = {
                Column {
                    Text("How was your experience with ${ratingModalBooking!!.categoryName} service?")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingScore = star.toFloat() }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= ratingScore) Color(0xFFD97706) else Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Write your feedback...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.rateAndReviewBooking(
                        bookingId = ratingModalBooking!!.id,
                        providerId = ratingModalBooking!!.providerId,
                        rating = ratingScore,
                        reviewText = reviewComment
                    )
                    ratingModalBooking = null
                }) {
                    Text("Submit Review")
                }
            }
        )
    }

    // File Dispute Modal
    if (disputeModalBooking != null) {
        val b = disputeModalBooking!!
        val reasons = listOf("Poor Work Quality", "Unfinished Work", "Property Damage", "Overcharged", "Unprofessional Conduct", "Other")
        val resolutions = listOf("Full Refund", "Partial Refund", "Re-service")

        AlertDialog(
            onDismissRequest = { disputeModalBooking = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                }
            },
            title = {
                Text(
                    text = "File Service Dispute",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Booking: ${b.bookingNumber} (${b.categoryName})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Provider: ${b.providerName} • Paid: $${String.format("%.2f", b.totalAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Text("1. Primary Reason for Dispute", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            reasons.chunked(2).forEach { rowReasons ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowReasons.forEach { reason ->
                                        val isSelected = selectedReason == reason
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = reason,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("2. Describe What Happened", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = disputeDetailsText,
                            onValueChange = { disputeDetailsText = it },
                            placeholder = { Text("Provide details about the issue...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }

                    item {
                        Text("3. Evidence / Photos Description", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = disputeEvidenceText,
                            onValueChange = { disputeEvidenceText = it },
                            placeholder = { Text("e.g. Attached photo of leaking valve or damaged paintwork...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    item {
                        Text("4. Desired Mediation Outcome", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            resolutions.forEach { res ->
                                val isSelected = selectedDesiredResolution == res
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = res,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.fileDispute(
                            booking = b,
                            reason = selectedReason,
                            details = disputeDetailsText.ifBlank { "No detailed description provided." },
                            evidenceText = disputeEvidenceText.ifBlank { "Customer noted evidence upon review." },
                            desiredResolution = selectedDesiredResolution
                        )
                        disputeModalBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = disputeDetailsText.isNotBlank() || disputeEvidenceText.isNotBlank()
                ) {
                    Text("Submit Dispute")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { disputeModalBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
