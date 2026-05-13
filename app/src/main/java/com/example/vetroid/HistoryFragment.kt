package com.example.vetroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val adapter = LogAdapter()

        binding.rvLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLogs.adapter = adapter

        // 30 günden eski kayıtları temizle
        viewLifecycleOwner.lifecycleScope.launch {
            val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            database.executionLogDao().deleteOlderThan(cutoff)
        }

        // Log akışını dinle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.executionLogDao().getAllLogs().collect { logs ->
                    adapter.submitList(logs)
                    binding.tvEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvLogs.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Geçmişi Temizle")
                .setMessage("Tüm geçmiş silinecek. Emin misiniz?")
                .setPositiveButton("Sil") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        database.executionLogDao().deleteAll()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
