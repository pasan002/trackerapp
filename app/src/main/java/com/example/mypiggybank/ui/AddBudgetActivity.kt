package com.example.mypiggybank.ui

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import com.example.mypiggybank.viewmodel.BudgetViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddBudgetActivity : AppCompatActivity() {

    private val viewModel: BudgetViewModel by viewModels()
    private var currentBudget: Budget? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val rgPeriod = findViewById<RadioGroup>(R.id.rgPeriod)

        // Observe current budget to pre-fill fields if it exists
        viewModel.currentBudget.observe(this) { budget ->
            currentBudget = budget
            budget?.let {
                etAmount.setText(it.amount.toString())
                rgPeriod.check(when(it.period) {
                    BudgetPeriod.WEEKLY -> R.id.rbWeekly
                    BudgetPeriod.MONTHLY -> R.id.rbMonthly
                })
            }
        }

        // Observe budget operation result
        viewModel.budgetInsertResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val amount = etAmount.text?.toString()?.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val period = when (rgPeriod.checkedRadioButtonId) {
                R.id.rbWeekly -> BudgetPeriod.WEEKLY
                else -> BudgetPeriod.MONTHLY
            }

            val budget = Budget(
                amount = amount,
                period = period,
                startDate = Calendar.getInstance().time
            )

            viewModel.setNewBudget(budget)
        }
    }
} 