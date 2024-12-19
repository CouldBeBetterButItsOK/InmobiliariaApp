package com.teknos.m8uf2.fxsane.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.FragmentItemDetailBinding
import com.teknos.m8uf2.fxsane.fragment.EditObjectFragment
import com.teknos.m8uf2.fxsane.model.Propietat as RealEstateProperty
import com.teknos.m8uf2.fxsane.singleton.AuthManager
import com.teknos.m8uf2.fxsane.singleton.InmobiliariaSingleton
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.sin

class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val singleton = InmobiliariaSingleton.getInstance()
        val property = singleton.getSelectedREProperty() ?: return
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Set property details
        binding.objName.text = property.name
        binding.category.text = property.type
        binding.date.text = "${R.string.updated} ${
            property.lastUpdate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
        }"
        binding.direction.text = "${property.city}, ${property.street}"
        binding.m2.text = "${getString(R.string.m2)}${property.m2} m²"
        binding.price.text = "${getString(R.string.price)} ${property.price}€"
        binding.description.text = property.description
        binding.objImg.setImageResource(R.drawable.property_pc)

        // Gesture detector for swipe to go back
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null && e2.x > e1.x) {
                    singleton.cleanSelectedProperty()
                    parentFragmentManager.popBackStack() // Go back
                    return true
                }
                return false
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Show or hide edit/delete buttons based on userId
        val currentUser = AuthManager.getInstance().getCurrentUser()?.uid
        if (currentUser != property.userId) {
            binding.editbt.visibility = View.GONE
            binding.deletebt.visibility = View.GONE
        } else {
            binding.editbt.visibility = View.VISIBLE
            binding.deletebt.visibility = View.VISIBLE

            // Edit button
            binding.editbt.setOnClickListener {
                val editFragment = EditObjectFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit()
            }

            // Delete button with confirmation dialog
            binding.deletebt.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.delete_user_question))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        singleton.removeAndArchiveProperty(property) { success, message ->
                            if (success) {
                                parentFragmentManager.popBackStack()
                            } else {
                                println("Error deleting property: $message")
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
