package com.teknos.m8uf2.fxsane.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.activity.BodyActivity
import com.teknos.m8uf2.fxsane.databinding.FragmentLogInBinding
import com.teknos.m8uf2.fxsane.model.UserApp
import com.teknos.m8uf2.fxsane.singleton.AuthManager
import com.teknos.m8uf2.fxsane.singleton.InmobiliariaSingleton

class LogInFragment : Fragment() {
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager
    private var userApp: UserApp? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        val view = binding.root
        authManager = AuthManager.getInstance()
        if( authManager.getCurrentUser() != null) {
            authManager.getUserById { u, e ->
                if (u != null) {
                    userApp = u
                    binding.mail.setText(userApp!!.email ?: "")
                    binding.password.setText(userApp!!.password ?: "")
                }
                else{
                    Toast.makeText(requireContext(), "Error finding current user", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.loginBT.setOnClickListener {
            if (validationFormulary()) {
                val email = binding.mail.text.toString()
                val password = binding.password.text.toString()
                authManager.signIn(email, password){ success, message ->
                    if (success){
                        Toast.makeText(requireContext(), "@string/login_successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), BodyActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(requireContext(), "Everything is fine, world is beautiful", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(), "@string/login_failed $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.createUserBT.setOnClickListener {
            val fragment = EditUserFragment()
            authManager.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.log_in_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        return view
    }
    private fun validationFormulary(): Boolean {
        val email = binding.mail.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        var isValid = true
        if (!email.matches(emailPattern.toRegex())) {
            isValid = false
            Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_SHORT).show()
        }
        if (password.length < 8) {
            isValid = false
            Toast.makeText(
                requireContext(),
                "Password must be at least 8 characters long",
                Toast.LENGTH_SHORT
            ).show()
        }
        return isValid

    }
}