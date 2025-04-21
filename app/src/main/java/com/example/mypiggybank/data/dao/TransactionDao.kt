package com.example.mypiggybank.data.dao

import androidx.room.*
import com.example.mypiggybank.data.CategorySummary
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE strftime('%m', datetime(date/1000, 'unixepoch')) = :month AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year ORDER BY date DESC")
    fun getTransactionsForMonth(month: Int, year: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    suspend fun getTransactionsByType(type: TransactionType): List<Transaction>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsBetweenDates(startDate: Date, endDate: Date): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    suspend fun getTotalByType(type: TransactionType): Double?

    @Query("""
        SELECT 
            category,
            SUM(amount) as total,
            type
        FROM transactions 
        WHERE type = :type 
        GROUP BY category, type
    """)
    suspend fun getCategorySummaryList(type: TransactionType): List<CategorySummary>

    // Helper function to convert List<CategorySummary> to Map<String, Double>
    suspend fun getCategorySummary(type: TransactionType): Map<String, Double> {
        return getCategorySummaryList(type).associate { it.category to it.total }
    }

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

@DatabaseView("SELECT category, SUM(amount) as total FROM transactions GROUP BY category")
data class CategorySummary(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Double
) 