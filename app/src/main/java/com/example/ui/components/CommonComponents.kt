package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole

@Composable
fun RoleSwitchHeader(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roles = listOf(
                UserRole.CUSTOMER to "Customer",
                UserRole.SERVICE_PROVIDER to "Technician",
                UserRole.ADMIN to "Admin"
            )

            roles.forEach { (role, label) ->
                val isSelected = currentRole == role
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF2563EB)
                            else Color.Transparent
                        )
                        .clickable { onRoleSelected(role) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White
                        else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verified",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "100% Verified",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun EmergencyChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEE2E2))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Emergency",
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Emergency 24/7",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF991B1B)
        )
    }
}

@Composable
fun RatingChip(rating: Float, reviewCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF3C7))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = Color(0xFFD97706),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$rating",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF92400E)
        )
        Text(
            text = " ($reviewCount)",
            fontSize = 11.sp,
            color = Color(0xFFB45309)
        )
    }
}

fun getIconForProfession(profession: String): ImageVector {
    return when (profession.lowercase()) {
        "electrician" -> Icons.Default.Bolt
        "plumber" -> Icons.Default.WaterDrop
        "carpenter" -> Icons.Default.Build
        "ac technician" -> Icons.Default.AcUnit
        "painter" -> Icons.Default.FormatPaint
        "cleaner" -> Icons.Default.CleaningServices
        "tutor" -> Icons.Default.School
        "beautician" -> Icons.Default.Face
        "appliance repair technician", "appliance repair" -> Icons.Default.HomeRepairService
        "mover" -> Icons.Default.LocalShipping
        "gardener" -> Icons.Default.Grass
        else -> Icons.Default.Work
    }
}

fun getCategoryColors(professionKey: String): Pair<Color, Color> {
    return when (professionKey.lowercase()) {
        "electrician" -> Pair(com.example.ui.theme.CategoryElectricianBg, com.example.ui.theme.CategoryElectricianIcon)
        "plumber" -> Pair(com.example.ui.theme.CategoryPlumbingBg, com.example.ui.theme.CategoryPlumbingIcon)
        "cleaner" -> Pair(com.example.ui.theme.CategoryCleaningBg, com.example.ui.theme.CategoryCleaningIcon)
        "beautician" -> Pair(com.example.ui.theme.CategoryBeautyBg, com.example.ui.theme.CategoryBeautyIcon)
        "tutor" -> Pair(com.example.ui.theme.CategoryTutorBg, com.example.ui.theme.CategoryTutorIcon)
        "ac technician" -> Pair(com.example.ui.theme.CategoryAcRepairBg, com.example.ui.theme.CategoryAcRepairIcon)
        "mover" -> Pair(com.example.ui.theme.CategoryMoverBg, com.example.ui.theme.CategoryMoverIcon)
        else -> Pair(com.example.ui.theme.CategoryDefaultBg, com.example.ui.theme.CategoryDefaultIcon)
    }
}

