package com.example.mypiggybank.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionCategory
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.ActivityAddTransactionBinding
import com.example.mypiggybank.viewmodel.TransactionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val viewModel: TransactionViewModel by viewModels()
    private var selectedDate: Date = Date()
    private var existingTransaction: Transaction? = null
    private var transactionType = TransactionType.EXPENSE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingTransaction = intent.getParcelableExtra("transaction")

        setupToolbar()
        setupCategorySpinner()
        setupTypeSpinner()
        setupSaveButton()

        existingTransaction?.let { populateFields(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (existingTransaction == null) "Add Transaction" else "Edit Transaction"
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupCategorySpinner() {
        val categories = TransactionCategory.values().map { it.displayName }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        (binding.spinnerCategory as? AutoCompleteTextView)?.let { autoComplete ->
            autoComplete.setAdapter(categoryAdapter)
            autoComplete.threshold = 1
            if (categories.isNotEmpty()) {
                autoComplete.setText(categories[0], false)
            }
        }
    }

    private fun setupTypeSpinner() {
        val types = arrayOf("Expense", "Income")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        (binding.spinnerType as? AutoCompleteTextView)?.let { autoComplete ->
            autoComplete.setAdapter(typeAdapter)
            autoComplete.threshold = 1
            autoComplete.setText(types[0], false)
            autoComplete.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                transactionType = if (position == 0) TransactionType.EXPENSE else TransactionType.INCOME
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etDescription.text.isNullOrBlank()) {
            binding.etDescription.error = "Description is required"
            isValid = false
        }

        if (binding.etAmount.text.isNullOrBlank()) {
            binding.etAmount.error = "Amount is required"
            isValid = false
        }

        if ((binding.spinnerCategory as? AutoCompleteTextView)?.text.isNullOrBlank()) {
            binding.categoryLayout.error = "Category is required"
            isValid = false
        } else {
            binding.categoryLayout.error = null
        }

        return isValid
    }

    private fun saveTransaction() {
        val description = binding.etDescription.text.toString()
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val category = (binding.spinnerCategory as AutoCompleteTextView).text.toString()

        val transaction = Transaction(
            id = existingTransaction?.id ?: 0,
            title = description,
            amount = amount,
            description = description,
            category = category,
            date = selectedDate,
            type = transactionType,
            isIncome = transactionType == TransactionType.INCOME
        )

        if (existingTransaction != null) {
            viewModel.updateTransaction(transaction)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertTransaction(transaction)
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun populateFields(transaction: Transaction) {
        binding.etDescription.setText(transaction.description)
        binding.etAmount.setText(transaction.amount.toString())
        
        (binding.spinnerCategory as? AutoCompleteTextView)?.setText(transaction.category, false)
        selectedDate = transaction.date
        
        transactionType = transaction.type
        (binding.spinnerType as? AutoCompleteTextView)?.setText(
            if (transaction.type == TransactionType.INCOME) "Income" else "Expense",
            false
        )
    }
} 