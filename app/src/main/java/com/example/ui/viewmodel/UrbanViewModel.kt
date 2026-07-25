package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.Booking
import com.example.data.model.ChatMessage
import com.example.data.model.Coupon
import com.example.data.model.PriceEstimate
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.UserRole
import com.example.data.remote.GeminiAiService
import com.example.data.repository.UrbanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UrbanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "urbanconnect_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = UrbanRepository(db)
    private val aiService = GeminiAiService()

    private val _userRole = MutableStateFlow(UserRole.CUSTOMER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLocation = MutableStateFlow("Downtown Metro")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    val allCategories: StateFlow<List<ServiceCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProviders: StateFlow<List<ServiceProvider>> = repository.favoriteProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDisputes: StateFlow<List<com.example.data.model.Dispute>> = repository.allDisputes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Providers based on category and search query
    val filteredProviders: StateFlow<List<ServiceProvider>> = combine(
        repository.allProviders,
        _selectedCategory,
        _searchQuery
    ) { providers: List<ServiceProvider>, cat: String?, query: String ->
        providers.filter { p ->
            val matchesCategory = cat == null || p.profession.equals(cat, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.profession.contains(query, ignoreCase = true) ||
                    p.bio.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeBookingForTracking = MutableStateFlow<Booking?>(null)
    val activeBookingForTracking: StateFlow<Booking?> = _activeBookingForTracking.asStateFlow()

    // AI States
    private val _aiChatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                bookingId = "AI_ASSISTANT",
                senderType = "AI",
                senderName = "UrbanAI Assistant",
                text = "Hello! 👋 I am UrbanAI, your smart assistant on UrbanConnect. Need help choosing a service, getting price estimates, or troubleshooting home repair issues? Ask me anything!"
            )
        )
    )
    val aiChatMessages: StateFlow<List<ChatMessage>> = _aiChatMessages.asStateFlow()

    private val _priceEstimateState = MutableStateFlow<PriceEstimate?>(null)
    val priceEstimateState: StateFlow<PriceEstimate?> = _priceEstimateState.asStateFlow()

    private val _isAiEstimating = MutableStateFlow(false)
    val isAiEstimating: StateFlow<Boolean> = _isAiEstimating.asStateFlow()

    private val _isAiChatLoading = MutableStateFlow(false)
    val isAiChatLoading: StateFlow<Boolean> = _isAiChatLoading.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    val availableCoupons = listOf(
        Coupon("URBAN100", 10, "Get 10% off on all home services"),
        Coupon("FIRST50", 15, "15% off for first time bookings"),
        Coupon("EMERGENCY5", 5, "Flat $5 discount on priority emergency requests")
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun setRole(role: UserRole) {
        _userRole.value = role
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setLocation(location: String) {
        _selectedLocation.value = location
    }

    fun toggleFavorite(providerId: String, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(providerId, !currentFav)
        }
    }

    fun createBooking(
        provider: ServiceProvider,
        scheduledDate: String,
        scheduledTime: String,
        address: String,
        notes: String,
        isEmergency: Boolean,
        paymentMethod: String,
        onSuccess: (Booking) -> Unit
    ) {
        viewModelScope.launch {
            val discount = if (_appliedCoupon.value != null) {
                (provider.hourlyRate * (_appliedCoupon.value!!.discountPercent / 100.0))
            } else 0.0

            val emergencyFee = if (isEmergency) 15.0 else 0.0
            val total = (provider.hourlyRate + emergencyFee - discount).coerceAtLeast(10.0)

            val bookingId = "BK-${(10000..99999).random()}"
            val newBooking = Booking(
                id = bookingId,
                bookingNumber = "UC-2026-${(10000..99999).random()}",
                categoryName = provider.profession,
                providerId = provider.id,
                providerName = provider.name,
                providerProfession = provider.profession,
                providerPhone = provider.phone,
                customerName = "Alex Mercer",
                customerAddress = address,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                status = if (isEmergency) "ACCEPTED" else "PENDING",
                baseCost = provider.hourlyRate,
                emergencyFee = emergencyFee,
                discountAmount = discount,
                totalAmount = total,
                isEmergency = isEmergency,
                paymentStatus = "PAID",
                paymentMethod = paymentMethod,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )

            repository.createBooking(newBooking)

            // Seed initial greeting message from provider
            repository.sendMessage(
                ChatMessage(
                    bookingId = bookingId,
                    senderType = "PROVIDER",
                    senderName = provider.name,
                    text = "Hi Alex! Thank you for booking. I have received your order for $scheduledDate at $scheduledTime."
                )
            )

            _activeBookingForTracking.value = newBooking
            onSuccess(newBooking)
        }
    }

    fun selectBookingForTracking(booking: Booking) {
        _activeBookingForTracking.value = booking
    }

    fun updateBookingStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(id, status)
            if (_activeBookingForTracking.value?.id == id) {
                _activeBookingForTracking.value = _activeBookingForTracking.value?.copy(status = status)
            }
        }
    }

    fun rateAndReviewBooking(bookingId: String, providerId: String, rating: Float, reviewText: String) {
        viewModelScope.launch {
            repository.addRatingAndReview(bookingId, providerId, rating, reviewText, "Alex Mercer")
        }
    }

    fun getMessagesForBooking(bookingId: String): StateFlow<List<ChatMessage>> {
        val flow = MutableStateFlow<List<ChatMessage>>(emptyList())
        viewModelScope.launch {
            repository.getMessagesForBooking(bookingId).collect {
                flow.value = it
            }
        }
        return flow
    }

    fun sendChatMessage(bookingId: String, text: String, senderType: String = "CUSTOMER") {
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = ChatMessage(
                bookingId = bookingId,
                senderType = senderType,
                senderName = if (senderType == "CUSTOMER") "Alex Mercer" else "Service Technician",
                text = text
            )
            repository.sendMessage(msg)
        }
    }

    fun sendAiChatQuery(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(
            bookingId = "AI_ASSISTANT",
            senderType = "CUSTOMER",
            senderName = "Alex Mercer",
            text = query
        )
        _aiChatMessages.value = _aiChatMessages.value + userMsg
        _isAiChatLoading.value = true

        viewModelScope.launch {
            val aiResponseText = aiService.getChatResponse(query, _selectedCategory.value)
            val aiMsg = ChatMessage(
                bookingId = "AI_ASSISTANT",
                senderType = "AI",
                senderName = "UrbanAI Assistant",
                text = aiResponseText
            )
            _aiChatMessages.value = _aiChatMessages.value + aiMsg
            _isAiChatLoading.value = false
        }
    }

    fun estimatePriceWithAi(category: String, details: String, isEmergency: Boolean) {
        _isAiEstimating.value = true
        viewModelScope.launch {
            val est = aiService.estimatePrice(category, details, isEmergency)
            _priceEstimateState.value = est
            _isAiEstimating.value = false
        }
    }

    fun applyCoupon(code: String): Boolean {
        val found = availableCoupons.find { it.code.equals(code, ignoreCase = true) }
        return if (found != null) {
            _appliedCoupon.value = found
            true
        } else {
            false
        }
    }

    fun clearCoupon() {
        _appliedCoupon.value = null
    }

    fun verifyProvider(providerId: String, isVerified: Boolean) {
        viewModelScope.launch {
            val provider = repository.getProviderById(providerId)
            if (provider != null) {
                repository.updateProviderAvailability(provider.copy(isVerified = isVerified))
            }
        }
    }

    fun fileDispute(
        booking: Booking,
        reason: String,
        details: String,
        evidenceText: String,
        desiredResolution: String
    ) {
        viewModelScope.launch {
            val dispute = com.example.data.model.Dispute(
                id = "DSP-${(1000..9999).random()}",
                bookingId = booking.id,
                bookingNumber = booking.bookingNumber,
                customerName = booking.customerName,
                providerId = booking.providerId,
                providerName = booking.providerName,
                categoryName = booking.categoryName,
                totalAmount = booking.totalAmount,
                reason = reason,
                details = details,
                evidenceText = evidenceText,
                desiredResolution = desiredResolution,
                status = "OPEN",
                createdAt = System.currentTimeMillis()
            )
            repository.fileDispute(dispute)
        }
    }

    fun resolveDispute(
        disputeId: String,
        resolutionType: String, // RESOLVED_FULL_REFUND, RESOLVED_PARTIAL_REFUND, RESOLVED_NO_REFUND
        refundAmount: Double,
        adminNotes: String
    ) {
        viewModelScope.launch {
            repository.resolveDispute(disputeId, resolutionType, refundAmount, adminNotes)
        }
    }
}
