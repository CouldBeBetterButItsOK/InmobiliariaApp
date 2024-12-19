package com.teknos.m8uf2.fxsane.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.FragmentEditObjectBinding
import com.teknos.m8uf2.fxsane.model.Propietat as RealEstateProperty
import com.teknos.m8uf2.fxsane.singleton.AuthManager
import com.teknos.m8uf2.fxsane.singleton.InmobiliariaSingleton
import java.util.*

class EditObjectFragment : Fragment() {

    private var _binding: FragmentEditObjectBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditObjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val singleton = InmobiliariaSingleton.getInstance()
        val currentProperty = singleton.getSelectedREProperty()

        // Pre-fill fields if a property is selected
        currentProperty?.let {
            binding.title.text = getString(R.string.edit_property)
            binding.name.setText(it.name)
            binding.street.setText(it.street)
            binding.city.setText(it.city)
            binding.type.setText(it.type)
            binding.m2.setText(it.m2.toString())
            binding.price.setText(it.price.toString())
            binding.description.setText(it.description)
        }

        // Cancel button with confirmation dialog
        binding.cancelButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.cancel))
                .setMessage(getString(R.string.cancel_question))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        // Save button
        binding.saveButton.setOnClickListener {
            val user = AuthManager.getInstance().getCurrentUser()

            val property = currentProperty ?: RealEstateProperty()

            // Update property details
            property.apply {
                name = binding.name.text.toString()
                street = binding.street.text.toString()
                city = binding.city.text.toString()
                type = binding.type.text.toString()
                m2 = binding.m2.text.toString().toIntOrNull() ?: 0
                price = binding.price.text.toString().toDoubleOrNull() ?: 0.0
                description = binding.description.text.toString()
                userId = user!!.uid // Assign current user's ID
                lastUpdate = Date() // Assign current date
            }

            singleton.saveREProperty(property) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Property saved successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error saving property: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}