package com.teknos.m8uf2.fxsane.singleton

import com.google.firebase.firestore.FirebaseFirestore
import com.teknos.m8uf2.fxsane.model.Propietat
import com.teknos.m8uf2.fxsane.model.UserApp

class InmobiliariaSingleton private constructor() {
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val propietats = mutableListOf<Propietat>()
    private var selectedObject: Propietat? = null
    private var currentUser: UserApp? = null
    private var authManager: AuthManager = AuthManager.getInstance()

    companion object {
        @Volatile
        private var instance: InmobiliariaSingleton? = null

        fun getInstance(): InmobiliariaSingleton {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = InmobiliariaSingleton().apply {
                            authManager.getUserById { u, e ->
                                if (u != null) {
                                    currentUser = u
                                }
                            }
                        }
                    }
                }
            }
            return instance!!
        }
    }

    fun getREProperties(callback: (List<Propietat>, String?) -> Unit) {
        firebaseFirestore.collection("RealEstateProperties")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val properties = querySnapshot.documents.mapNotNull { it.toObject(Propietat::class.java) }
                propietats.clear()
                propietats.addAll(properties)
                callback(properties, null)
            }
            .addOnFailureListener { e ->
                callback(emptyList(), "Error al obtener las propiedades: ${e.message}")
            }
    }
    fun saveREProperty(property: Propietat, callback: (Boolean, String?) -> Unit){
        val userId = currentUser?.id
        if (userId == null) {
            callback(false, "Usuario no autenticado")
            return
        }
        property.userId = userId
        if (property.id.isNullOrEmpty()) {
            property.id = firebaseFirestore.collection("RealEstateProperties")
                .document()
                .id
        }
        firebaseFirestore.collection("RealEstateProperties")
            .document(property.id!!)
            .set(property)
            .addOnSuccessListener {
                if (propietats.find { it.id == property.id } == null) {
                    propietats.add(property)
                } else {
                    val index = propietats.indexOfFirst { it.id == property.id }
                    if (index != -1) {
                        propietats[index] = property
                    }}
                    callback(true, "Propiedad guardada correctamente")

            }
    }

    fun selectREProperty(property: Propietat) {
        selectedObject = property
    }
    fun cleanSelectedProperty() {
        selectedObject = null
    }

    fun getSelectedREProperty(): Propietat? {
        return selectedObject
    }
    fun removeREProperty(property: Propietat, callback: (Boolean, String?) -> Unit) {
        val propertyId = property.id ?: return
        firebaseFirestore.collection("RealEstateProperties").document(propertyId)
            .delete()
            .addOnSuccessListener {
                propietats.remove(property)
                firebaseFirestore.collection("DeletedProperties").document(propertyId)
                    .set(property)
                    .addOnSuccessListener {
                        callback(true, "Propiedad eliminada correctamente")
                        cleanSelectedProperty()
                    }.addOnFailureListener { e ->
                        callback(false, "Error al archivar la propiedad: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Error al eliminar la propiedad: ${e.message}")
            }
    }
    fun signOut() {
        currentUser = null
    }
    fun signIn(userApp: UserApp){
        currentUser = userApp
    }



}
