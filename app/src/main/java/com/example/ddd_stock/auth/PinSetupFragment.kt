package com.example.ddd_stock.auth

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ddd_stock.R
import com.example.ddd_stock.databinding.FragmentPinSetupBinding

class PinSetupFragment : Fragment() {

    private var _binding: FragmentPinSetupBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    private val pin = StringBuilder()
    private var isVerifying = false
    private var firstPin = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupKeypad()
        updatePinDisplay()
        animateEntrance()
    }

    private fun animateEntrance() {
        binding.apply {
            tvTitle.alpha = 0f
            tvSubtitle.alpha = 0f
            tvPinDisplay.alpha = 0f
            layoutPinDots.alpha = 0f
            layoutKeypad.alpha = 0f

            listOf(tvTitle, tvSubtitle, tvPinDisplay, layoutPinDots, layoutKeypad)
                .forEachIndexed { index, view ->
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                        duration = 400
                        startDelay = (index * 120).toLong()
                        interpolator = OvershootInterpolator()
                        start()
                    }
                }
        }
    }

    private fun setupKeypad() {
        binding.btn1.setOnClickListener { appendDigit("1") }
        binding.btn2.setOnClickListener { appendDigit("2") }
        binding.btn3.setOnClickListener { appendDigit("3") }
        binding.btn4.setOnClickListener { appendDigit("4") }
        binding.btn5.setOnClickListener { appendDigit("5") }
        binding.btn6.setOnClickListener { appendDigit("6") }
        binding.btn7.setOnClickListener { appendDigit("7") }
        binding.btn8.setOnClickListener { appendDigit("8") }
        binding.btn9.setOnClickListener { appendDigit("9") }
        binding.btn0.setOnClickListener { appendDigit("0") }
        binding.btnClear.setOnClickListener { clearPin() }
        binding.btnBackspace.setOnClickListener { removeLastDigit() }
    }

    private fun appendDigit(digit: String) {
        if (pin.length >= 4) return
        pin.append(digit)
        updatePinDisplay()
        if (pin.length == 4) {
            onPinComplete()
        }
    }

    private fun removeLastDigit() {
        if (pin.isEmpty()) return
        pin.deleteCharAt(pin.length - 1)
        updatePinDisplay()
        binding.tvError.visibility = View.GONE
    }

    private fun clearPin() {
        pin.clear()
        updatePinDisplay()
        binding.tvError.visibility = View.GONE
    }

    private fun updatePinDisplay() {
        val display = pin.toString().padEnd(4, ' ').map {
            if (it == ' ') "•" else "●"
        }.joinToString("  ")
        binding.tvPinDisplay.text = display

        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            if (index < pin.length) {
                dot.setBackgroundResource(R.drawable.pin_dot_filled)
            } else {
                dot.setBackgroundResource(R.drawable.pin_dot_empty)
            }
        }
    }

    private fun onPinComplete() {
        val pinText = pin.toString()
        val error = com.example.ddd_stock.util.ValidationUtils.validatePin(pinText)
        if (error != null) {
            showError(error)
            clearPin()
            return
        }

        if (!isVerifying) {
            firstPin = pinText
            isVerifying = true
            pin.clear()
            binding.tvTitle.text = getString(R.string.auth_verify_pin)
            binding.tvSubtitle.text = "Re-enter your PIN to confirm"
            updatePinDisplay()
        } else {
            if (pinText == firstPin) {
                onPinConfirmed(pinText)
            } else {
                showError("PINs do not match")
                isVerifying = false
                pin.clear()
                firstPin = ""
                binding.tvTitle.text = getString(R.string.auth_setup_pin)
                binding.tvSubtitle.text = getString(R.string.auth_pin_subtitle)
                updatePinDisplay()
            }
        }
    }

    private fun onPinConfirmed(pinText: String) {
        val uid = viewModel.authState.value?.let {
            if (it is AuthViewModel.AuthState.Success) it.uid else null
        }
        if (uid == null) {
            findNavController().navigate(R.id.action_pin_setup_to_home)
            return
        }
        viewModel.verifyPin(pinText, uid)
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(binding.tvError, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
