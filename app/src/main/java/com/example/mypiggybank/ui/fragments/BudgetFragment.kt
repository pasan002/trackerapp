package com.example.mypiggybank.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.ui.AddBudgetActivity
import com.example.mypiggybank.ui.MainViewModel
import com.example.mypiggybank.ui.adapter.BudgetAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var chart: BarChart
    private lateinit var tvTotalBudgetStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up chart
        chart = view.findViewById(R.id.budgetComparisonChart)
        setupChart()

        // Set up budget status
        tvTotalBudgetStatus = view.findViewById(R.id.tvTotalBudgetStatus)

        // Set up RecyclerView
        budgetAdapter = BudgetAdapter { budget ->
            // Handle budget item click
            val intent = Intent(requireContext(), AddBudgetActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<RecyclerView>(R.id.rvBudgets).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }

        // Set up Add Budget button
        view.findViewById<MaterialButton>(R.id.btnAddBudget).setOnClickListener {
            val intent = Intent(requireContext(), AddBudgetActivity::class.java)
            startActivity(intent)
        }

        // Observe budget and expenses
        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            budget?.let { 
                budgetAdapter.updateBudgets(listOf(it))
                viewModel.totalExpense.value?.let { expense ->
                    updateChart(it.amount, expense)
                    updateBudgetStatus(it.amount, expense)
                }
            } ?: run {
                // No budget set
                updateChart(0.0, 0.0)
                updateBudgetStatus(0.0, 0.0)
                budgetAdapter.updateBudgets(emptyList())
            }
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            viewModel.currentBudget.value?.let { budget ->
                updateChart(budget.amount, expense)
                updateBudgetStatus(budget.amount, expense)
            }
        }
    }

    private fun updateBudgetStatus(budget: Double, spent: Double) {
        tvTotalBudgetStatus.text = String.format("₹%.2f / ₹%.2f", spent, budget)
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setNoDataText("No budget data available")
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("Budget", "Expenses"))
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            
            animateY(1000)
        }
    }

    private fun updateChart(budget: Double, expense: Double) {
        if (budget == 0.0 && expense == 0.0) {
            chart.clear()
            chart.invalidate()
            return
        }

        val entries = listOf(
            BarEntry(0f, budget.toFloat()),  // Budget
            BarEntry(1f, expense.toFloat())  // Expenses
        )

        val dataSet = BarDataSet(entries, "Budget vs Expenses").apply {
            colors = listOf(
                Color.rgb(76, 175, 80),  // Green for budget
                if (expense > budget) {
                    Color.rgb(244, 67, 54)  // Red for over-budget expenses
                } else {
                    Color.rgb(33, 150, 243)  // Blue for within-budget expenses
                }
            )
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        chart.data = barData
        chart.invalidate()
    }
} 