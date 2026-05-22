package com.example.ddd_stock.auth

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.databinding.FragmentRegisterBinding
import com.example.ddd_stock.util.ValidationUtils

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
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
            val inputs = listOf(
                tilUsername, tilFirstName, tilSurname, tilEmail,
                tilContact, tilPassword, tilPin, btnRegister
            )
            inputs.forEachIndexed { index, view ->
                view.alpha = 0f
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                    duration = 400
                    startDelay = (index * 80 + 100).toLong()
                    start()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val firstName = binding.etFirstName.text.toString().trim()
            val surname = binding.etSurname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val contact = binding.etContact.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val pin = binding.etPin.text.toString()

            viewModel.register(username, firstName, surname, email, contact, password, pin)
        }

        binding.tvLoginAction.setOnClickListener {
            viewModel.resetState()
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                if (text.length >= 3) {
                    viewModel.checkUsernameDebounced(text)
                } else {
                    binding.tvUsernameFeedback.visibility = View.GONE
                }
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                if (ValidationUtils.validateEmail(text) == null && text.isNotEmpty()) {
                    viewModel.checkEmailDebounced(text)
                } else {
                    binding.tvEmailFeedback.visibility = View.GONE
                }
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                viewModel.evaluatePassword(text)
                if (text.isNotEmpty()) {
                    binding.passwordStrength.visibility = View.VISIBLE
                    binding.tvPasswordStrengthLabel.visibility = View.VISIBLE
                } else {
                    binding.passwordStrength.visibility = View.GONE
                    binding.tvPasswordStrengthLabel.visibility = View.GONE
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.btnRegister.showLoading()
                    hideError()
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.btnRegister.hideLoading()
                    findNavController().navigate(R.id.action_register_to_home)
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.btnRegister.hideLoading()
                    showError(state.message)
                }
                is AuthViewModel.AuthState.Idle -> {
                    binding.btnRegister.hideLoading()
                }
            }
        }

        viewModel.usernameExists.observe(viewLifecycleOwner) { exists ->
            when (exists) {
                true -> {
                    binding.tvUsernameFeedback.text = "Username already taken"
                    binding.tvUsernameFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                    binding.tvUsernameFeedback.visibility = View.VISIBLE
                }
                false -> {
                    binding.tvUsernameFeedback.text = "Username available"
                    binding.tvUsernameFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green))
                    binding.tvUsernameFeedback.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvUsernameFeedback.visibility = View.GONE
                }
            }
        }

        viewModel.emailExists.observe(viewLifecycleOwner) { exists ->
            when (exists) {
                true -> {
                    binding.tvEmailFeedback.text = "Email already registered"
                    binding.tvEmailFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                    binding.tvEmailFeedback.visibility = View.VISIBLE
                }
                false -> {
                    binding.tvEmailFeedback.text = "Email available"
                    binding.tvEmailFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green))
                    binding.tvEmailFeedback.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvEmailFeedback.visibility = View.GONE
                }
            }
        }

        viewModel.passwordStrength.observe(viewLifecycleOwner) { strength ->
            binding.passwordStrength.setStrength(strength)
            val label = when (strength) {
                ValidationUtils.PasswordStrength.WEAK -> "Weak"
                ValidationUtils.PasswordStrength.MEDIUM -> "Medium"
                ValidationUtils.PasswordStrength.STRONG -> "Strong"
                ValidationUtils.PasswordStrength.VERY_STRONG -> "Very Strong"
            }
            binding.tvPasswordStrengthLabel.text = label
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
