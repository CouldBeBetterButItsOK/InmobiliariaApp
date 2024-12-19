package com.teknos.m8uf2.fxsane.fragment

import PropertyAdapter
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.FragmentRealEstateBinding
import com.teknos.m8uf2.fxsane.fragment.EditObjectFragment
import com.teknos.m8uf2.fxsane.fragment.ItemDetailFragment
import com.teknos.m8uf2.fxsane.singleton.InmobiliariaSingleton

class RealEstateFragment : Fragment() {
    private var _binding: FragmentRealEstateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRealEstateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                menu.findItem(R.id.search_view).isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search_view -> {
                        // Handle search functionality here
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        // Initialize Singleton and Adapter
        val singleton = InmobiliariaSingleton.getInstance()
        singleton.getREProperties { properties, error ->
            if (error == null) {
                val propertyAdapter = PropertyAdapter(properties) { property ->
                    singleton.selectREProperty(property)
                    // Open detail fragment for the selected property
                    val itemDetailFragment = ItemDetailFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_container, itemDetailFragment)
                        .addToBackStack(null)
                        .commit()
                }
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = propertyAdapter
            } else {
                // Handle errors
                println("Error fetching properties: $error")
            }
        }

        // Floating Action Button to Add/Edit Property
        binding.floatingActionButton.setOnClickListener {
            val editFragment = EditObjectFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_container, editFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}