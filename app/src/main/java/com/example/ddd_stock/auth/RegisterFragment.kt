package com.example.ddd_stock.auth
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        setupListeners(); setupObservers(); animateEntrance()
    }

    private fun animateEntrance() = binding.apply {
        listOf(tvTitle, tvSubtitle, tilUsername, tilFirstName, tilSurname, tilEmail, tilContact, tilPassword, tilPin, btnRegister).forEachIndexed { i, v ->
            v.alpha = 0f; ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).apply { duration = 400; startDelay = (i * 80 + 100).toLong(); start() }
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            viewModel.register(binding.etUsername.text.toString().trim(), binding.etFirstName.text.toString().trim(), binding.etSurname.text.toString().trim(),
                binding.etEmail.text.toString().trim(), binding.etContact.text.toString().trim(), binding.etPassword.text.toString(), binding.etPin.text.toString())
        }
        binding.tvLoginAction.setOnClickListener { viewModel.resetState(); findNavController().navigate(R.id.action_register_to_login) }
        binding.etUsername.doAfterTextChanged { val t = it?.toString()?.trim() ?: ""; if (t.length >= 3) viewModel.checkUsernameDebounced(t) else binding.tvUsernameFeedback.visibility = View.GONE }
        binding.etEmail.doAfterTextChanged { val t = it?.toString()?.trim() ?: ""; if (ValidationUtils.validateEmail(t) == null && t.isNotEmpty()) viewModel.checkEmailDebounced(t) else binding.tvEmailFeedback.visibility = View.GONE }
        binding.etPassword.doAfterTextChanged {
            val t = it?.toString() ?: ""; viewModel.evaluatePassword(t)
            binding.passwordStrength.visibility = if (t.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvPasswordStrengthLabel.visibility = if (t.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> { binding.btnRegister.showLoading(); hideError() }
                is AuthViewModel.AuthState.Success -> { binding.btnRegister.hideLoading(); findNavController().navigate(R.id.action_register_to_home) }
                is AuthViewModel.AuthState.Error -> { binding.btnRegister.hideLoading(); showError(state.message) }
                is AuthViewModel.AuthState.Idle -> binding.btnRegister.hideLoading()
            }
        }

        viewModel.usernameExists.observe(viewLifecycleOwner) { exists ->
            binding.tvUsernameFeedback.apply {
                when (exists) { true -> { text = "Username already taken"; setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red)); visibility = View.VISIBLE }
                    false -> { text = "Username available"; setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green)); visibility = View.VISIBLE }
                    else -> visibility = View.GONE }
            }
        }

        viewModel.emailExists.observe(viewLifecycleOwner) { exists ->
            binding.tvEmailFeedback.apply {
                when (exists) { true -> { text = "Email already registered"; setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red)); visibility = View.VISIBLE }
                    false -> { text = "Email available"; setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green)); visibility = View.VISIBLE }
                    else -> visibility = View.GONE }
            }
        }

        viewModel.passwordStrength.observe(viewLifecycleOwner) { strength ->
            binding.passwordStrength.setStrength(strength)
            binding.tvPasswordStrengthLabel.text = when (strength) { ValidationUtils.PasswordStrength.WEAK -> "Weak"; ValidationUtils.PasswordStrength.MEDIUM -> "Medium"; ValidationUtils.PasswordStrength.STRONG -> "Strong"; ValidationUtils.PasswordStrength.VERY_STRONG -> "Very Strong" }
        }
    }

    private fun showError(message: String) { binding.tvError.text = message; binding.tvError.visibility = View.VISIBLE; ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply { duration = 300; start() } }
    private fun hideError() { binding.tvError.visibility = View.GONE }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
