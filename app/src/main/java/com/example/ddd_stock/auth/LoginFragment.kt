package com.example.ddd_stock.auth
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        setupListeners(); setupObservers(); animateEntrance()
    }

    private fun animateEntrance() = binding.apply {
        listOf(tvTitle, tvSubtitle, tilEmail, tilPassword, btnLogin, tvRegisterLink, tvRegisterAction).forEachIndexed { i, v ->
            v.alpha = 0f; ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).apply { duration = 400; startDelay = (i * 100).toLong(); start() }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { viewModel.login(binding.etEmail.text.toString().trim(), binding.etPassword.text.toString()) }
        binding.tvRegisterAction.setOnClickListener { viewModel.resetState(); findNavController().navigate(R.id.action_login_to_register) }
        binding.etEmail.doAfterTextChanged { hideError() }
        binding.etPassword.doAfterTextChanged { hideError() }
    }

    private fun setupObservers() = viewModel.authState.observe(viewLifecycleOwner) { state ->
        when (state) {
            is AuthViewModel.AuthState.Loading -> { binding.btnLogin.showLoading(); hideError() }
            is AuthViewModel.AuthState.Success -> { binding.btnLogin.hideLoading(); findNavController().navigate(R.id.action_login_to_home) }
            is AuthViewModel.AuthState.Error -> { binding.btnLogin.hideLoading(); showError(state.message) }
            is AuthViewModel.AuthState.Idle -> binding.btnLogin.hideLoading()
        }
    }

    private fun showError(message: String) { binding.tvError.text = message; binding.tvError.visibility = View.VISIBLE; ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply { duration = 300; start() } }
    private fun hideError() { binding.tvError.visibility = View.GONE }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
