package com.example.mypiggybank.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                // Get all transactions instead of just monthly ones for now
                transactionRepository.getAllTransactions()
                    .catch { e -> 
                        // Handle error
                        _transactions.value = emptyList()
                        _totalIncome.value = 0.0
                        _totalExpenses.value = 0.0
                        _currentBalance.value = 0.0
                    }
                    .collect { transactions ->
                        _transactions.value = transactions
                        calculateSummary(transactions)
                    }
            } catch (e: Exception) {
                // Handle any other errors
                _transactions.value = emptyList()
                _totalIncome.value = 0.0
                _totalExpenses.value = 0.0
                _currentBalance.value = 0.0
            }
        }
    }

    private fun calculateSummary(transactions: List<Transaction>) {
        var income = 0.0
        var expenses = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> income += transaction.amount
                TransactionType.EXPENSE -> expenses += transaction.amount
            }
        }

        _totalIncome.postValue(income)
        _totalExpenses.postValue(expenses)
        _currentBalance.postValue(income - expenses)
    }

    fun refreshData() {
        loadTransactions()
    }
} 