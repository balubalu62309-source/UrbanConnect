package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    SERVICE_PROVIDER,
    ADMIN
}

@Entity(tableName = "service_categories")
data class ServiceCategory(
    @PrimaryKey val id: String,
    val name: String,
    val professionKey: String,
    val description: String,
    val iconName: String,
    val basePrice: Double,
    val isPopular: Boolean = false
)

@Entity(tableName = "service_providers")
data class ServiceProvider(
    @PrimaryKey val id: String,
    val name: String,
    val profession: String, // Electrician, Plumber, Carpenter, AC technician, Painter, Cleaner, Tutor, Beautician, Appliance repair technician, Mover, Gardener, Freelancer
    val rating: Float,
    val reviewCount: Int,
    val hourlyRate: Double,
    val experienceYears: Int,
    val bio: String,
    val location: String,
    val distanceKm: Double,
    val isVerified: Boolean = true,
    val isAvailable: Boolean = true,
    val phone: String,
    val email: String,
    val completedJobsCount: Int,
    val portfolioPhotos: String, // Pipe-separated list of image titles or URLs
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val bookingNumber: String,
    val categoryName: String,
    val providerId: String,
    val providerName: String,
    val providerProfession: String,
    val providerPhone: String,
    val customerName: String,
    val customerAddress: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val status: String, // PENDING, ACCEPTED, EN_ROUTE, IN_PROGRESS, COMPLETED, CANCELLED
    val baseCost: Double,
    val emergencyFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val isEmergency: Boolean = false,
    val paymentStatus: String, // PAID, PENDING, REFUNDED
    val paymentMethod: String, // Google Pay, Card, Cash
    val notes: String = "",
    val ratingGiven: Float = 0f,
    val reviewGiven: String = "",
    val disputeStatus: String = "NONE", // NONE, OPEN, RESOLVED_FULL_REFUND, RESOLVED_PARTIAL_REFUND, RESOLVED_NO_REFUND
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class Dispute(
    @PrimaryKey val id: String,
    val bookingId: String,
    val bookingNumber: String,
    val customerName: String,
    val providerId: String,
    val providerName: String,
    val categoryName: String,
    val totalAmount: Double,
    val reason: String, // Poor Quality, Incomplete Work, Damage, Overcharged, Unprofessional, Other
    val details: String,
    val evidenceText: String,
    val desiredResolution: String, // Full Refund, Partial Refund, Re-service
    val status: String = "OPEN", // OPEN, RESOLVED_FULL_REFUND, RESOLVED_PARTIAL_REFUND, RESOLVED_NO_REFUND
    val refundAmount: Double = 0.0,
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: String,
    val senderType: String, // CUSTOMER, PROVIDER, AI
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val providerId: String,
    val customerName: String,
    val rating: Float,
    val comment: String,
    val date: String
)

data class PriceEstimate(
    val category: String,
    val estimatedLow: Double,
    val estimatedHigh: Double,
    val recommendedDurationHours: Float,
    val AIReasoning: String,
    val breakdownItems: List<String>
)

data class Coupon(
    val code: String,
    val discountPercent: Int,
    val description: String
)
