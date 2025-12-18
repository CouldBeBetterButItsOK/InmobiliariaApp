package com.teknos.m8uf2.fxsane.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.teknos.m8uf2.fxsane.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.teknos.m8uf2.fxsane.model.Propietat
import com.teknos.m8uf2.fxsane.singleton.InmobiliariaSingleton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScanQRFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScanQRFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_scan_q_r, container, false)

        val btnScan = view.findViewById<Button>(R.id.scanQr)
        btnScan.setOnClickListener {
            startQRScanner()
        }

        return view
    }
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleQrResult(result.contents)
        }
    }
    private fun startQRScanner() {
        try {
            // 1️⃣ Cargar la imagen del drawable
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.qrdeprova)

            // 2️⃣ Convertir a formato ZXing
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source: LuminanceSource = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            // 3️⃣ Leer el QR
            val result = MultiFormatReader().decode(binaryBitmap)
            val qrText = result.text

            // 4️⃣ Convertir a Propietat
            val property = Gson().fromJson(qrText, Propietat::class.java)
            InmobiliariaSingleton.getInstance().selectREProperty(property)

            // 5️⃣ Abrir el fragment de edición
            openEditFragment()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "No se pudo leer el QR", Toast.LENGTH_SHORT).show()
        }
    }
    /*private fun startQRScanner() {
        val options = ScanOptions()
        options.setPrompt("Escanea un código QR")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setBarcodeImageEnabled(true)

        qrLauncher.launch(options)
    }*/
    private fun handleQrResult(qrText: String) {
        try {
            val gson = Gson()
            val property = gson.fromJson(qrText, Propietat::class.java)

            InmobiliariaSingleton.getInstance().selectREProperty(property)

            openEditFragment()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "QR inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEditFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_container, EditObjectFragment())
            .addToBackStack(null)
            .commit()
    }

}