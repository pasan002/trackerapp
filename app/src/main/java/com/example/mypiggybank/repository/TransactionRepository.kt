package com.example.mypiggybank.repository

import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.data.dao.TransactionDao
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    suspend fun getTransactionsByType(type: TransactionType) = transactionDao.getTransactionsByType(type)
    
    suspend fun getTransactionsBetweenDates(startDate: Date, endDate: Date) =
        transactionDao.getTransactionsBetweenDates(startDate, endDate)
    
    suspend fun getTotalByType(type: TransactionType) = transactionDao.getTotalByType(type) ?: 0.0
    
    suspend fun getCategorySummary(type: TransactionType) = transactionDao.getCategorySummary(type)
    
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getMonthlySpendingByCategory(month: Int, year: Int): Map<String, Double> {
        val startDate = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val endDate = Calendar.getInstance().apply {
            set(year, month - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val transactions = getTransactionsBetweenDates(startDate, endDate)
            .filter { it.type == TransactionType.EXPENSE }
        
        return transactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }
} 