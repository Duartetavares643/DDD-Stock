package com.example.ddd_stock.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.auth.AuthViewModel
import com.example.ddd_stock.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupObservers()
        setupListeners()

        homeViewModel.loadUserProfile()
    }

    private fun setupObservers() {
        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.textHome.text = getString(R.string.home_welcome, user.username)
            binding.progressBar.visibility = View.GONE
            binding.textHome.visibility = View.VISIBLE
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.textHome.visibility = View.GONE
            }
        }

        homeViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                binding.progressBar.visibility = View.GONE
                binding.textHome.text = errorMsg
                binding.textHome.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_global_logout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
