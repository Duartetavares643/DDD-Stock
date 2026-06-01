package com.example.ddd_stock.ui.home
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.databinding.FragmentHomeBinding
import com.example.ddd_stock.ui.auth.LoginViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        binding.btnLogout.setOnClickListener { authViewModel.logout(); findNavController().navigate(R.id.action_global_logout) }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
