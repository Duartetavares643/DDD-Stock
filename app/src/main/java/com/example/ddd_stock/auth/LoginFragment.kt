package com.example.ddd_stock.auth

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupListeners()
        setupObservers()
        animateEntrance()
    }

    private fun animateEntrance() {
        binding.apply {
            tvTitle.alpha = 0f
            tvSubtitle.alpha = 0f
            tilEmail.alpha = 0f
            tilPassword.alpha = 0f
            btnLogin.alpha = 0f
            tvRegisterLink.alpha = 0f
            tvRegisterAction.alpha = 0f

            val views = listOf(tvTitle, tvSubtitle, tilEmail, tilPassword, btnLogin, tvRegisterLink, tvRegisterAction)
            views.forEachIndexed { index, view ->
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                    duration = 400
                    startDelay = (index * 100).toLong()
                    start()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvRegisterAction.setOnClickListener {
            viewModel.resetState()
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hideError()
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hideError()
            }
        })
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.btnLogin.showLoading()
                    hideError()
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.btnLogin.hideLoading()
                    showSuccess()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.btnLogin.hideLoading()
                    showError(state.message)
                }
                is AuthViewModel.AuthState.Idle -> {
                    binding.btnLogin.hideLoading()
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.alpha = 0f
        ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun showSuccess() {
        findNavController().navigate(R.id.action_login_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
