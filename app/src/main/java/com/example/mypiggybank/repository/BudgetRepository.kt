package com.example.mypiggybank.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.dao.BudgetDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getCurrentBudget(): LiveData<Budget?> {
        return budgetDao.getCurrentBudget()
    }

    suspend fun setNewBudget(budget: Budget) {
        try {
            Log.d("BudgetRepository", "Setting new budget: $budget")
            budgetDao.setNewBudget(budget)
            Log.d("BudgetRepository", "Budget set successfully")
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error setting budget", e)
            throw e
        }
    }

    suspend fun updateBudget(budget: Budget) {
        try {
            budgetDao.updateBudget(budget)
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error updating budget", e)
            throw e
        }
    }

    suspend fun updateSpentAmount(amount: Double) {
        try {
            budgetDao.updateSpentAmount(amount)
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error updating spent amount", e)
            throw e
        }
    }
} 