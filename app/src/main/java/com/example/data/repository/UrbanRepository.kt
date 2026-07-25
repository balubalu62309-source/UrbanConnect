package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Booking
import com.example.data.model.ChatMessage
import com.example.data.model.Dispute
import com.example.data.model.Review
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UrbanRepository(private val db: AppDatabase) {

    val allCategories: Flow<List<ServiceCategory>> = db.serviceDao().getAllCategories()
    val allProviders: Flow<List<ServiceProvider>> = db.providerDao().getAllProviders()
    val favoriteProviders: Flow<List<ServiceProvider>> = db.providerDao().getFavoriteProviders()
    val allBookings: Flow<List<Booking>> = db.bookingDao().getAllBookings()
    val allDisputes: Flow<List<Dispute>> = db.disputeDao().getAllDisputes()

    fun getProvidersByProfession(profession: String): Flow<List<ServiceProvider>> {
        return db.providerDao().getProvidersByProfession(profession)
    }

    suspend fun getProviderById(id: String): ServiceProvider? {
        return db.providerDao().getProviderById(id)
    }

    suspend fun toggleFavorite(providerId: String, isFavorite: Boolean) {
        db.providerDao().updateFavorite(providerId, isFavorite)
    }

    suspend fun createBooking(booking: Booking) {
        db.bookingDao().insertBooking(booking)
    }

    suspend fun updateBookingStatus(id: String, status: String) {
        db.bookingDao().updateBookingStatus(id, status)
    }

    suspend fun addRatingAndReview(bookingId: String, providerId: String, rating: Float, reviewText: String, customerName: String) {
        db.bookingDao().addRatingAndReview(bookingId, rating, reviewText)
        val review = Review(
            id = "REV-${System.currentTimeMillis()}",
            providerId = providerId,
            customerName = customerName,
            rating = rating,
            comment = reviewText,
            date = "Today"
        )
        db.reviewDao().insertReview(review)
    }

    fun getMessagesForBooking(bookingId: String): Flow<List<ChatMessage>> {
        return db.chatDao().getMessagesForBooking(bookingId)
    }

    suspend fun sendMessage(message: ChatMessage) {
        db.chatDao().insertMessage(message)
    }

    fun getReviewsForProvider(providerId: String): Flow<List<Review>> {
        return db.reviewDao().getReviewsForProvider(providerId)
    }

    suspend fun updateProviderAvailability(provider: ServiceProvider) {
        db.providerDao().updateProvider(provider)
    }

    suspend fun fileDispute(dispute: Dispute) {
        db.disputeDao().insertDispute(dispute)
        val booking = db.bookingDao().getBookingById(dispute.bookingId)
        if (booking != null) {
            db.bookingDao().updateBooking(booking.copy(disputeStatus = "OPEN"))
        }
        // Send notification log in chat
        val systemNote = ChatMessage(
            bookingId = dispute.bookingId,
            senderType = "AI",
            senderName = "UrbanConnect Mediator",
            text = "Dispute filed (#${dispute.id}). Reason: ${dispute.reason}. Details: ${dispute.details}. Our support team is reviewing the case."
        )
        db.chatDao().insertMessage(systemNote)
    }

    suspend fun resolveDispute(
        disputeId: String,
        status: String, // RESOLVED_FULL_REFUND, RESOLVED_PARTIAL_REFUND, RESOLVED_NO_REFUND
        refundAmount: Double,
        adminNotes: String
    ) {
        val dispute = db.disputeDao().getDisputeById(disputeId) ?: return
        val updatedDispute = dispute.copy(
            status = status,
            refundAmount = refundAmount,
            adminNotes = adminNotes,
            resolvedAt = System.currentTimeMillis()
        )
        db.disputeDao().updateDispute(updatedDispute)

        val booking = db.bookingDao().getBookingById(dispute.bookingId)
        if (booking != null) {
            val paymentStatusUpdate = when (status) {
                "RESOLVED_FULL_REFUND" -> "REFUNDED"
                "RESOLVED_PARTIAL_REFUND" -> "PARTIALLY_REFUNDED"
                else -> booking.paymentStatus
            }
            db.bookingDao().updateBooking(
                booking.copy(
                    disputeStatus = status,
                    paymentStatus = paymentStatusUpdate
                )
            )
        }

        // Send mediation decision to chat
        val decisionText = when (status) {
            "RESOLVED_FULL_REFUND" -> "Mediation complete: Full refund of $${String.format("%.2f", refundAmount)} has been issued to the customer. Admin note: $adminNotes"
            "RESOLVED_PARTIAL_REFUND" -> "Mediation complete: Partial refund of $${String.format("%.2f", refundAmount)} has been issued to the customer. Admin note: $adminNotes"
            else -> "Mediation complete: Dispute reviewed. No refund awarded. Admin note: $adminNotes"
        }
        val systemNote = ChatMessage(
            bookingId = dispute.bookingId,
            senderType = "AI",
            senderName = "UrbanConnect Mediator",
            text = decisionText
        )
        db.chatDao().insertMessage(systemNote)
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        // Categories seed
        val categories = listOf(
            ServiceCategory("cat_elec", "Electrician", "Electrician", "Short circuits, wiring, fixture installation & electrical safety", "bolt", 35.0, true),
            ServiceCategory("cat_plumb", "Plumber", "Plumber", "Pipe leaks, drain unblocking, bathroom fitting & water pumps", "water_drop", 40.0, true),
            ServiceCategory("cat_carp", "Carpenter", "Carpenter", "Furniture repair, custom woodwork, door fitting & locks", "build", 38.0, true),
            ServiceCategory("cat_ac", "AC Technician", "AC technician", "AC installation, gas filling, deep cleaning & repair", "ac_unit", 50.0, true),
            ServiceCategory("cat_paint", "Painters", "Painter", "Interior & exterior house painting, waterproof coating", "format_paint", 80.0, false),
            ServiceCategory("cat_clean", "Cleaners", "Cleaner", "Full home deep cleaning, sofa cleaning & disinfection", "cleaning_services", 30.0, true),
            ServiceCategory("cat_tutor", "Tutors", "Tutor", "Academic tutoring, language classes & music lessons at home", "school", 25.0, false),
            ServiceCategory("cat_beauty", "Beauticians", "Beautician", "Salon at home, hair styling, bridal makeup & spa", "face", 45.0, true),
            ServiceCategory("cat_appliance", "Appliance Repair", "Appliance repair technician", "Washing machine, fridge, microwave & TV repair", "home_repair_service", 42.0, false),
            ServiceCategory("cat_mover", "Movers", "Mover", "Home relocation, packing, heavy lifting & transport", "local_shipping", 120.0, false),
            ServiceCategory("cat_gardener", "Gardeners", "Gardener", "Lawn mowing, plant pruning, landscaping & pest control", "grass", 35.0, false),
            ServiceCategory("cat_free", "Freelancers", "Freelancer", "Home IT support, photography, interior design & odd jobs", "work", 40.0, false)
        )
        db.serviceDao().insertCategories(categories)

        // Seed Providers
        val providers = listOf(
            ServiceProvider(
                id = "p_elec_1",
                name = "Marcus Vance",
                profession = "Electrician",
                rating = 4.9f,
                reviewCount = 142,
                hourlyRate = 45.0,
                experienceYears = 8,
                bio = "Licensed master electrician specializing in home automation, breaker panels, and emergency short circuit repair.",
                location = "Downtown Metro (1.2 km)",
                distanceKm = 1.2,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 234-8901",
                email = "marcus.vance@urbanconnect.com",
                completedJobsCount = 280,
                portfolioPhotos = "Panel Wiring|Smart Switch Install|Lighting Fixture",
                isFeatured = true,
                isFavorite = true
            ),
            ServiceProvider(
                id = "p_plumb_1",
                name = "David Chen",
                profession = "Plumber",
                rating = 4.8f,
                reviewCount = 98,
                hourlyRate = 50.0,
                experienceYears = 10,
                bio = "Certified master plumber available for high-pressure pipe repairs, clog removal, and instant emergency leak fixing.",
                location = "Westside District (2.5 km)",
                distanceKm = 2.5,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 345-6789",
                email = "david.chen@urbanconnect.com",
                completedJobsCount = 310,
                portfolioPhotos = "Copper Piping|Bathroom Fitting|Water Heater",
                isFeatured = true,
                isFavorite = false
            ),
            ServiceProvider(
                id = "p_ac_1",
                name = "Aisha Patel",
                profession = "AC technician",
                rating = 4.95f,
                reviewCount = 215,
                hourlyRate = 55.0,
                experienceYears = 7,
                bio = "HVAC technician expert in inverter AC servicing, Freon gas refilling, and central cooling maintenance.",
                location = "Greenwood Heights (1.8 km)",
                distanceKm = 1.8,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 890-1234",
                email = "aisha.patel@urbanconnect.com",
                completedJobsCount = 420,
                portfolioPhotos = "Split AC Service|Gas Refill|Compressor Check",
                isFeatured = true,
                isFavorite = true
            ),
            ServiceProvider(
                id = "p_clean_1",
                name = "Elena Rostova",
                profession = "Cleaner",
                rating = 4.9f,
                reviewCount = 180,
                hourlyRate = 35.0,
                experienceYears = 6,
                bio = "Professional deep cleaning team leader using eco-friendly non-toxic agents for sanitization and carpet shampooing.",
                location = "Eastside Parks (3.1 km)",
                distanceKm = 3.1,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 567-8901",
                email = "elena.clean@urbanconnect.com",
                completedJobsCount = 350,
                portfolioPhotos = "Kitchen Deep Clean|Sofa Shampooing|Window Wash",
                isFeatured = false,
                isFavorite = false
            ),
            ServiceProvider(
                id = "p_carp_1",
                name = "Robert Thorne",
                profession = "Carpenter",
                rating = 4.7f,
                reviewCount = 76,
                hourlyRate = 42.0,
                experienceYears = 12,
                bio = "Master woodworker for custom modular cabinets, wooden door repair, furniture assembly, and lock installation.",
                location = "Central Ave (0.9 km)",
                distanceKm = 0.9,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 678-9012",
                email = "robert.thorne@urbanconnect.com",
                completedJobsCount = 190,
                portfolioPhotos = "Custom Wardrobe|Door Restoration|Kitchen Cabinet",
                isFeatured = false,
                isFavorite = false
            ),
            ServiceProvider(
                id = "p_paint_1",
                name = "Samuel Jackson",
                profession = "Painter",
                rating = 4.85f,
                reviewCount = 112,
                hourlyRate = 60.0,
                experienceYears = 9,
                bio = "Architectural painter offering stencil art, waterproof wall coating, texture painting, and drywall patching.",
                location = "North Hills (4.0 km)",
                distanceKm = 4.0,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 789-0123",
                email = "samuel.paint@urbanconnect.com",
                completedJobsCount = 210,
                portfolioPhotos = "Accent Wall|Exterior Paint|Waterproof Coating",
                isFeatured = false,
                isFavorite = false
            ),
            ServiceProvider(
                id = "p_beauty_1",
                name = "Sophia Martinez",
                profession = "Beautician",
                rating = 5.0f,
                reviewCount = 310,
                hourlyRate = 50.0,
                experienceYears = 8,
                bio = "Salon-at-home expert offering facial therapies, bridal glam, hair spa, manicure & pedicures with luxury products.",
                location = "Bayside Villa (1.5 km)",
                distanceKm = 1.5,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 432-1098",
                email = "sophia.beauty@urbanconnect.com",
                completedJobsCount = 520,
                portfolioPhotos = "Hydra Facial|Bridal Makeup|Hair Treatment",
                isFeatured = true,
                isFavorite = true
            ),
            ServiceProvider(
                id = "p_tutor_1",
                name = "Dr. Alan Turing",
                profession = "Tutor",
                rating = 4.9f,
                reviewCount = 89,
                hourlyRate = 30.0,
                experienceYears = 11,
                bio = "STEM and mathematics educator specializing in High School & College Physics, Calculus, and Computer Science.",
                location = "University District (2.0 km)",
                distanceKm = 2.0,
                isVerified = true,
                isAvailable = true,
                phone = "+1 (555) 123-4567",
                email = "alan.tutor@urbanconnect.com",
                completedJobsCount = 160,
                portfolioPhotos = "Calculus Session|Physics Lab Demo",
                isFeatured = false,
                isFavorite = false
            )
        )
        db.providerDao().insertProviders(providers)

        // Seed sample active & completed bookings
        val sampleBookings = listOf(
            Booking(
                id = "BK-80921",
                bookingNumber = "UC-2026-80921",
                categoryName = "Electrician",
                providerId = "p_elec_1",
                providerName = "Marcus Vance",
                providerProfession = "Electrician",
                providerPhone = "+1 (555) 234-8901",
                customerName = "Alex Mercer",
                customerAddress = "742 Evergreen Terrace, Apt 4B",
                scheduledDate = "Today",
                scheduledTime = "14:30 PM",
                status = "EN_ROUTE",
                baseCost = 45.0,
                emergencyFee = 15.0,
                discountAmount = 5.0,
                totalAmount = 55.0,
                isEmergency = true,
                paymentStatus = "PAID",
                paymentMethod = "Google Pay",
                notes = "Main circuit breaker keeps tripping when AC is turned on.",
                timestamp = System.currentTimeMillis() - 1000000
            ),
            Booking(
                id = "BK-80890",
                bookingNumber = "UC-2026-80890",
                categoryName = "AC Technician",
                providerId = "p_ac_1",
                providerName = "Aisha Patel",
                providerProfession = "AC technician",
                providerPhone = "+1 (555) 890-1234",
                customerName = "Alex Mercer",
                customerAddress = "742 Evergreen Terrace, Apt 4B",
                scheduledDate = "Yesterday",
                scheduledTime = "10:00 AM",
                status = "COMPLETED",
                baseCost = 55.0,
                emergencyFee = 0.0,
                discountAmount = 0.0,
                totalAmount = 55.0,
                isEmergency = false,
                paymentStatus = "PAID",
                paymentMethod = "Credit Card",
                notes = "Annual maintenance and filter cleaning for 2 Split ACs.",
                ratingGiven = 5.0f,
                reviewGiven = "Aisha was prompt, clean, and extremely knowledgeable! My AC is freezing cold again.",
                disputeStatus = "NONE",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            Booking(
                id = "BK-80712",
                bookingNumber = "UC-2026-80712",
                categoryName = "Plumber",
                providerId = "p_plumb_1",
                providerName = "David Chen",
                providerProfession = "Plumber",
                providerPhone = "+1 (555) 345-6789",
                customerName = "Alex Mercer",
                customerAddress = "742 Evergreen Terrace, Apt 4B",
                scheduledDate = "3 days ago",
                scheduledTime = "11:30 AM",
                status = "COMPLETED",
                baseCost = 80.0,
                emergencyFee = 0.0,
                discountAmount = 10.0,
                totalAmount = 70.0,
                isEmergency = false,
                paymentStatus = "PAID",
                paymentMethod = "Google Pay",
                notes = "Kitchen sink drainage pipe installation.",
                ratingGiven = 2.0f,
                reviewGiven = "Sink pipe started leaking again 2 hours after technician left.",
                disputeStatus = "OPEN",
                timestamp = System.currentTimeMillis() - 259200000
            )
        )
        for (b in sampleBookings) {
            db.bookingDao().insertBooking(b)
        }

        // Seed initial sample dispute
        val sampleDispute = Dispute(
            id = "DSP-1002",
            bookingId = "BK-80712",
            bookingNumber = "UC-2026-80712",
            customerName = "Alex Mercer",
            providerId = "p_plumb_1",
            providerName = "David Chen",
            categoryName = "Plumber",
            totalAmount = 70.0,
            reason = "Poor Quality & Leaking",
            details = "The kitchen sink drainage joint was not sealed properly with teflon tape. Water leaked into the wooden cabinet causing minor warping.",
            evidenceText = "Uploaded 2 photos showing wet cabinet floor and dripping PVC joint connection.",
            desiredResolution = "Partial Refund ($35) or Full Refund ($70)",
            status = "OPEN",
            createdAt = System.currentTimeMillis() - 172800000
        )
        db.disputeDao().insertDispute(sampleDispute)

        // Seed initial chat messages for active booking
        val initialMessages = listOf(
            ChatMessage(
                bookingId = "BK-80921",
                senderType = "PROVIDER",
                senderName = "Marcus Vance",
                text = "Hello Alex! I have accepted your emergency request and I'm currently on my way to Evergreen Terrace. ETA 12 minutes.",
                timestamp = System.currentTimeMillis() - 600000
            ),
            ChatMessage(
                bookingId = "BK-80921",
                senderType = "CUSTOMER",
                senderName = "Alex Mercer",
                text = "Great thanks Marcus! I have switched off the main breaker switch just to be safe.",
                timestamp = System.currentTimeMillis() - 300000
            )
        )
        for (m in initialMessages) {
            db.chatDao().insertMessage(m)
        }
    }
}
