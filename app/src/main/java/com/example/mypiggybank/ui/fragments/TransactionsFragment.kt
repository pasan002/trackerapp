package com.example.mypiggybank.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.ui.AddTransactionActivity
import com.example.mypiggybank.ui.MainViewModel
import com.example.mypiggybank.ui.adapter.TransactionsListAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var rvTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionsListAdapter
    private lateinit var fabAddTransaction: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTransactions = view.findViewById(R.id.transactionsRecyclerView)
        fabAddTransaction = view.findViewById(R.id.addTransactionFab)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionsListAdapter(
            transactionList = emptyList(),
            onEditClick = { transaction -> editTransaction(transaction) },
            onDeleteClick = { transaction -> showDeleteDialog(transaction) }
        )
        rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.updateTransactions(transactions)
        }
    }

    private fun setupClickListeners() {
        fabAddTransaction.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_TRANSACTION)
        }
    }

    private fun editTransaction(transaction: Transaction) {
        val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
            putExtra("transaction", transaction)
            putExtra("isEdit", true)
        }
        startActivityForResult(intent, REQUEST_EDIT_TRANSACTION)
    }

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        viewModel.deleteTransaction(transaction)
        showUndoSnackbar(transaction)
    }

    private fun showUndoSnackbar(deletedTransaction: Transaction) {
        Snackbar.make(
            requireView(),
            "Transaction deleted",
            Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            viewModel.addTransaction(deletedTransaction)
        }.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            val transaction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra("transaction", Transaction::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra("transaction")
            }
            
            transaction?.let {
                when (requestCode) {
                    REQUEST_ADD_TRANSACTION -> viewModel.addTransaction(it)
                    REQUEST_EDIT_TRANSACTION -> viewModel.updateTransaction(it)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_ADD_TRANSACTION = 100
        private const val REQUEST_EDIT_TRANSACTION = 101
    }
} 