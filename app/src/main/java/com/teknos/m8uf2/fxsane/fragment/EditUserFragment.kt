package com.teknos.m8uf2.fxsane.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.FragmentEditUserBinding
import com.teknos.m8uf2.fxsane.model.UserApp
import com.teknos.m8uf2.fxsane.singleton.AuthManager



class EditUserFragment : Fragment() {
    private var _binding: FragmentEditUserBinding? = null
    private val binding get() = _binding!!
    private var userApp: UserApp? = null
    private lateinit var authManager: AuthManager


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("EditUserFragment", "EditUserFragment cargado")
        Toast.makeText(requireContext(), "EditUserFragment cargado", Toast.LENGTH_SHORT).show()
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        val view = binding.root
        authManager = AuthManager.getInstance()
        if( authManager.getCurrentUser() != null) {
            authManager.getUserById { u, e ->
                if (u != null) {
                    userApp = u
                    binding.title.text = getString(R.string.edit_user)
                    binding.mail.setText(userApp!!.email ?: "")
                    binding.nickname.setText(userApp!!.nickName ?: "")
                    binding.password.setText(userApp!!.password ?: "")
                    binding.city.setText(userApp!!.city ?: "")
                    binding.street.setText(userApp!!.street ?: "")
                    binding.phone.setText(userApp!!.phone.toString() ?: "")
                }
                else{
                    Toast.makeText(requireContext(), "Error finding current user", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.saveBT.setOnClickListener {
            if(validationFormulary()) {
                binding.saveBT.isEnabled = false
                authManager.saveUser(
                    binding.mail.text.toString(),
                    binding.password.text.toString(),
                    binding.nickname.text.toString(),
                    binding.phone.text.toString().toInt(),
                    binding.street.text.toString(),
                    binding.city.text.toString()
                ) { success, message ->
                    if (success) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.cancelBT.setOnClickListener {
            AlertDialog.Builder(binding.root.context)
                .setTitle(getString(R.string.cancel))
                .setMessage(getString(R.string.cancel_question))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    parentFragmentManager.popBackStack()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun validationFormulary(): Boolean {
        val mail = binding.mail.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val nickname = binding.nickname.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val street = binding.street.text.toString().trim()
        val phone = binding.phone.text.toString().trim()

        var isValid = true

        if (mail.isEmpty() || password.isEmpty() || nickname.isEmpty() || city.isEmpty() || street.isEmpty() || phone.isEmpty()) {
            isValid = false
            Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
        }

        val emailPattern = Patterns.EMAIL_ADDRESS
        if (!emailPattern.matcher(mail).matches()) {
            isValid = false
            Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_SHORT).show()
        }

        if (password.length < 8) {
            isValid = false
            Toast.makeText(requireContext(), "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
        }

        if (nickname.length < 4) {
            isValid = false
            Toast.makeText(requireContext(), "Nickname must be at least 4 characters long", Toast.LENGTH_SHORT).show()
        }
        if (phone.length != 9 || phone.toIntOrNull() == null) {
            isValid = false
            Toast.makeText(requireContext(), "Phone is invalid ", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }
}