package com.example.mypiggybank.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mypiggybank.R
import com.example.mypiggybank.ui.MainViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetComparisonFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var chart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget_comparison, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chart = view.findViewById(R.id.budgetComparisonChart)
        setupChart()

        // Observe budget and expenses
        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            viewModel.totalExpense.value?.let { expense ->
                updateChart(budget?.amount ?: 0.0, expense)
            }
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            viewModel.currentBudget.value?.let { budget ->
                updateChart(budget.amount, expense)
            }
        }
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            
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