package com.example.mypiggybank.utils

import android.content.Context
import androidx.lifecycle.asFlow
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.repository.BudgetRepository
import com.example.mypiggybank.repository.TransactionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    private val gson = Gson()
    private val backupDir = File(context.filesDir, "backups")

    init {
        backupDir.mkdirs()
    }

    suspend fun createBackup(): Boolean {
        return try {
            val transactions = transactionRepository.getAllTransactions().first()
            val currentBudget = budgetRepository.getCurrentBudget().value
            val budgets = listOfNotNull(currentBudget)

            val backup = BackupData(transactions, budgets)
            val json = gson.toJson(backup)
            
            val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.json")
            backupFile.writeText(json)
            
            // Keep only last 5 backups
            backupDir.listFiles()
                ?.sortedBy { it.lastModified() }
                ?.dropLast(5)
                ?.forEach { it.delete() }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreFromLatestBackup(): Boolean {
        return try {
            val latestBackup = backupDir.listFiles()
                ?.maxByOrNull { it.lastModified() }
                ?: return false

            val json = latestBackup.readText()
            val backupData = gson.fromJson<BackupData>(
                json,
                object : TypeToken<BackupData>() {}.type
            )

            // Clear existing data and restore from backup
            backupData.transactions.forEach { transactionRepository.insertTransaction(it) }
            backupData.budgets.firstOrNull()?.let { budgetRepository.setNewBudget(it) }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAvailableBackups(): List<BackupInfo> {
        return backupDir.listFiles()
            ?.map { file ->
                BackupInfo(
                    fileName = file.name,
                    date = java.util.Date(file.lastModified())
                )
            }
            ?.sortedByDescending { it.date }
            ?: emptyList()
    }

    private data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>
    )

    data class BackupInfo(
        val fileName: String,
        val date: java.util.Date
    )
} 