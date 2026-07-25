package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.Booking
import com.example.data.model.ChatMessage
import com.example.data.model.Dispute
import com.example.data.model.Review
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_categories")
    fun getAllCategories(): Flow<List<ServiceCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ServiceCategory>)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM service_providers")
    fun getAllProviders(): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers WHERE profession = :profession")
    fun getProvidersByProfession(profession: String): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers WHERE id = :id")
    suspend fun getProviderById(id: String): ServiceProvider?

    @Query("SELECT * FROM service_providers WHERE isFavorite = 1")
    fun getFavoriteProviders(): Flow<List<ServiceProvider>>

    @Query("UPDATE service_providers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ServiceProvider>)

    @Update
    suspend fun updateProvider(provider: ServiceProvider)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: String): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: String, status: String)

    @Query("UPDATE bookings SET ratingGiven = :rating, reviewGiven = :review WHERE id = :id")
    suspend fun addRatingAndReview(id: String, rating: Float, review: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getMessagesForBooking(bookingId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE providerId = :providerId")
    fun getReviewsForProvider(providerId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<Dispute>>

    @Query("SELECT * FROM disputes WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getDisputeByBookingId(bookingId: String): Dispute?

    @Query("SELECT * FROM disputes WHERE id = :id LIMIT 1")
    suspend fun getDisputeById(id: String): Dispute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: Dispute)

    @Update
    suspend fun updateDispute(dispute: Dispute)
}

@Database(
    entities = [
        ServiceCategory::class,
        ServiceProvider::class,
        Booking::class,
        ChatMessage::class,
        Review::class,
        Dispute::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun providerDao(): ProviderDao
    abstract fun bookingDao(): BookingDao
    abstract fun chatDao(): ChatDao
    abstract fun reviewDao(): ReviewDao
    abstract fun disputeDao(): DisputeDao
}
