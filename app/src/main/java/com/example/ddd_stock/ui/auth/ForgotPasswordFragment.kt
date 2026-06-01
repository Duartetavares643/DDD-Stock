package com.example.ddd_stock.ui.auth
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
import com.example.ddd_stock.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        setupListeners(); setupObservers(); animateEntrance()
    }

    private fun animateEntrance() = binding.apply {
        listOf(tvTitle, tvSubtitle, tilEmail, btnSend, tvBackToLogin).forEachIndexed { i, v ->
            v.alpha = 0f; ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).apply { duration = 400; startDelay = (i * 100).toLong(); start() }
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener { viewModel.sendPasswordReset(binding.etEmail.text.toString().trim()) }
        binding.tvBackToLogin.setOnClickListener { findNavController().navigateUp() }
        binding.etEmail.doAfterTextChanged { viewModel.resetState(); hideSuccess(); hideError() }
    }

    private fun setupObservers() = viewModel.resetState.observe(viewLifecycleOwner) { state ->
        when (state) {
            is ForgotPasswordViewModel.ResetState.Loading -> { binding.btnSend.showLoading(); hideError(); hideSuccess() }
            is ForgotPasswordViewModel.ResetState.Success -> { binding.btnSend.hideLoading(); binding.tvSuccess.visibility = View.VISIBLE; ObjectAnimator.ofFloat(binding.tvSuccess, "alpha", 0f, 1f).apply { duration = 300; start() } }
            is ForgotPasswordViewModel.ResetState.Error -> { binding.btnSend.hideLoading(); showError(state.message) }
            is ForgotPasswordViewModel.ResetState.Idle -> binding.btnSend.hideLoading()
        }
    }

    private fun showError(message: String) { binding.tvError.text = message; binding.tvError.visibility = View.VISIBLE; ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply { duration = 300; start() } }
    private fun hideError() { binding.tvError.visibility = View.GONE }
    private fun hideSuccess() { binding.tvSuccess.visibility = View.GONE }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
