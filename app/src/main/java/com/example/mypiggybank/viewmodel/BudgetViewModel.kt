package com.example.mypiggybank.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _budgetInsertResult = MutableLiveData<Boolean>()
    val budgetInsertResult: LiveData<Boolean> = _budgetInsertResult

    val currentBudget = repository.getCurrentBudget()

    fun setNewBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                Log.d("BudgetViewModel", "Setting new budget: $budget")
                repository.setNewBudget(budget)
                _budgetInsertResult.value = true
                Log.d("BudgetViewModel", "Budget set successfully")
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error setting budget", e)
                _budgetInsertResult.value = false
            }
        }
    }

    fun updateSpentAmount(amount: Double) {
        viewModelScope.launch {
            try {
                repository.updateSpentAmount(amount)
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error updating spent amount", e)
            }
        }
    }
} 