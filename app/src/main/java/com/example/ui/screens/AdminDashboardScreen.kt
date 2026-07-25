package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.example.data.model.Dispute
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.UrbanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: UrbanViewModel
) {
    val providers by viewModel.filteredProviders.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()
    val disputes by viewModel.allDisputes.collectAsState()

    var commissionRate by remember { mutableStateOf("10%") }
    var selectedDisputeFilter by remember { mutableStateOf("OPEN") } // OPEN, RESOLVED, ALL

    var activeMediationDispute by remember { mutableStateOf<Dispute?>(null) }
    var selectedResolutionType by remember { mutableStateOf("FULL_REFUND") } // FULL_REFUND, PARTIAL_REFUND, NO_REFUND
    var customRefundAmountText by remember { mutableStateOf("") }
    var adminNotesText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UrbanConnect Admin Control Panel", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Platform Financial & Performance Stats
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Platform Key Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("$42,850.00", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Platform Commission", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                Text("$4,285.00 ($commissionRate)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Verified Providers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                Text("${providers.size} Technicians", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Bookings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${bookings.size} Orders", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Dispute Resolution & Mediation Section
            item {
                val openCount = disputes.count { it.status == "OPEN" }
                val resolvedCount = disputes.count { it.status != "OPEN" }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dispute & Refund Mediation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Review claims, audit evidence & mediate refunds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (openCount > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (openCount > 0) "$openCount Action Required" else "All Clear",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (openCount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Dispute Filter Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        val filterTabs = listOf(
                            "OPEN" to "Open Cases ($openCount)",
                            "RESOLVED" to "Resolved ($resolvedCount)",
                            "ALL" to "All (${disputes.size})"
                        )
                        filterTabs.forEach { (key, label) ->
                            val isSelected = selectedDisputeFilter == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF2563EB) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedDisputeFilter = key }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    val filteredDisputes = disputes.filter { d ->
                        when (selectedDisputeFilter) {
                            "OPEN" -> d.status == "OPEN"
                            "RESOLVED" -> d.status != "OPEN"
                            else -> true
                        }
                    }

                    if (filteredDisputes.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No disputes matching this filter.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        filteredDisputes.forEach { dispute ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Case #${dispute.id} • Order #${dispute.bookingNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Customer: ${dispute.customerName} | Provider: ${dispute.providerName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (dispute.status) {
                                                "OPEN" -> Color(0xFFFEF3C7)
                                                "RESOLVED_FULL_REFUND" -> Color(0xFFDCFCE7)
                                                "RESOLVED_PARTIAL_REFUND" -> Color(0xFFE0F2FE)
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        ) {
                                            Text(
                                                text = when (dispute.status) {
                                                    "OPEN" -> "AWAITING MEDIATION"
                                                    "RESOLVED_FULL_REFUND" -> "FULL REFUND"
                                                    "RESOLVED_PARTIAL_REFUND" -> "PARTIAL REFUND"
                                                    else -> "CLOSED (NO REFUND)"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (dispute.status) {
                                                    "OPEN" -> Color(0xFF92400E)
                                                    "RESOLVED_FULL_REFUND" -> Color(0xFF15803D)
                                                    "RESOLVED_PARTIAL_REFUND" -> Color(0xFF0369A1)
                                                    else -> Color(0xFF475569)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Reason: ${dispute.reason}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                                Text("Amount Paid: $${String.format("%.2f", dispute.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Details: ${dispute.details}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Evidence: ${dispute.evidenceText}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Requested Resolution: ${dispute.desiredResolution}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                                        }
                                    }

                                    if (dispute.status == "OPEN") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                activeMediationDispute = dispute
                                                selectedResolutionType = "FULL_REFUND"
                                                customRefundAmountText = String.format("%.2f", dispute.totalAmount)
                                                adminNotesText = ""
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                        ) {
                                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Review & Mediate Dispute", fontSize = 12.sp)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column {
                                            Text("Mediation Decision: Refunded $${String.format("%.2f", dispute.refundAmount)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF16A34A))
                                            if (dispute.adminNotes.isNotBlank()) {
                                                Text("Admin Rationale: ${dispute.adminNotes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Professional Verification Queue Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Professional Identity Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Identity Protection Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Technicians Verification Approval Items
            items(providers) { provider ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (provider.isVerified) VerifiedBadge()
                            }
                            Text("${provider.profession} • ID Ref: #VER-${provider.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Submitted Docs: Gov ID, Trade License, Criminal Check Clear", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!provider.isVerified) {
                                Button(
                                    onClick = { viewModel.verifyProvider(provider.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve", fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.verifyProvider(provider.id, false) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Revoke", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // Promotions & Coupons Management
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Discount, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Platform Promotions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        viewModel.availableCoupons.forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• ${c.code} (${c.discountPercent}% OFF)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(c.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Dispute Mediation Dialog
    if (activeMediationDispute != null) {
        val disp = activeMediationDispute!!
        val maxAmount = disp.totalAmount

        AlertDialog(
            onDismissRequest = { activeMediationDispute = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF2563EB)
                    )
                }
            },
            title = {
                Text(
                    text = "Mediate Dispute #${disp.id}",
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
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Booking: #${disp.bookingNumber} (${disp.categoryName})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Customer: ${disp.customerName} • Provider: ${disp.providerName}", fontSize = 11.sp)
                                Text("Total Order Value: $${String.format("%.2f", disp.totalAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Reason: ${disp.reason}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFDC2626))
                                Text("Claim: ${disp.details}", fontSize = 11.sp)
                                Text("Evidence: ${disp.evidenceText}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    item {
                        Text("Select Mediation Decision:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Full Refund Option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedResolutionType == "FULL_REFUND") Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedResolutionType = "FULL_REFUND"
                                        customRefundAmountText = String.format("%.2f", maxAmount)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedResolutionType == "FULL_REFUND",
                                        onClick = {
                                            selectedResolutionType = "FULL_REFUND"
                                            customRefundAmountText = String.format("%.2f", maxAmount)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Full Refund ($${String.format("%.2f", maxAmount)})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                        Text("Fully refund the customer from provider payout.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // Partial Refund Option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedResolutionType == "PARTIAL_REFUND") Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedResolutionType = "PARTIAL_REFUND"
                                        if (customRefundAmountText == String.format("%.2f", maxAmount) || customRefundAmountText == "0.00") {
                                            customRefundAmountText = String.format("%.2f", maxAmount / 2)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedResolutionType == "PARTIAL_REFUND",
                                        onClick = {
                                            selectedResolutionType = "PARTIAL_REFUND"
                                            if (customRefundAmountText == String.format("%.2f", maxAmount) || customRefundAmountText == "0.00") {
                                                customRefundAmountText = String.format("%.2f", maxAmount / 2)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Partial Refund", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0369A1))
                                        Text("Refund a split portion of total payment.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            if (selectedResolutionType == "PARTIAL_REFUND") {
                                OutlinedTextField(
                                    value = customRefundAmountText,
                                    onValueChange = { customRefundAmountText = it },
                                    label = { Text("Enter Refund Amount ($)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            // No Refund Option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedResolutionType == "NO_REFUND") Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedResolutionType = "NO_REFUND"
                                        customRefundAmountText = "0.00"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedResolutionType == "NO_REFUND",
                                        onClick = {
                                            selectedResolutionType = "NO_REFUND"
                                            customRefundAmountText = "0.00"
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Reject Dispute (No Refund)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569))
                                        Text("Keep original payment and complete order.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Admin Mediation Notes & Rationale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = adminNotesText,
                            onValueChange = { adminNotesText = it },
                            placeholder = { Text("e.g. Verified evidence photos showing unfinished work. Partial refund approved.", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedRefund = customRefundAmountText.toDoubleOrNull() ?: when (selectedResolutionType) {
                            "FULL_REFUND" -> maxAmount
                            "PARTIAL_REFUND" -> maxAmount / 2
                            else -> 0.0
                        }
                        val finalStatus = when (selectedResolutionType) {
                            "FULL_REFUND" -> "RESOLVED_FULL_REFUND"
                            "PARTIAL_REFUND" -> "RESOLVED_PARTIAL_REFUND"
                            else -> "RESOLVED_NO_REFUND"
                        }
                        val finalAmount = parsedRefund.coerceIn(0.0, maxAmount)

                        viewModel.resolveDispute(
                            disputeId = disp.id,
                            resolutionType = finalStatus,
                            refundAmount = finalAmount,
                            adminNotes = adminNotesText.ifBlank { "Admin reviewed dispute and issued final decision." }
                        )

                        activeMediationDispute = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Issue Decision & Process")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { activeMediationDispute = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
