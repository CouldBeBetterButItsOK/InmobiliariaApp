package com.teknos.m8uf2.fxsane.singleton

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.teknos.m8uf2.fxsane.model.UserApp
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager private constructor(){
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var user: UserApp? = null

    companion object{
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(): AuthManager{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: AuthManager().also { INSTANCE = it }
            }
        }
    }

    fun getCurrentUser(): FirebaseUser?{
        return firebaseAuth.currentUser
    }
    fun getUserById(callback: (UserApp?, String?) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            callback(null, "Current user is null")
            return
        }

        firebaseFirestore.collection("UserApp")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userById = document.toObject(UserApp::class.java)
                    if (userById != null) {
                        callback(userById, null)
                    } else {
                        callback(null, "User data is null")
                    }
                } else {
                    callback(null, "User not found")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.message)
            }
    }
    fun setActualUser(callback: (Boolean) -> Unit) {
        getUserById { u, e ->
            if (u != null) {
                user = u
                callback(true)
            } else {
                callback(false)
            }
        }
    }
    fun saveUser(
        email: String,
        password: String,
        nickname: String,
        phone: Int,
        street: String,
        city: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // Si el usuario ya existe, actualizamos su información
            setActualUser {
                if (it) {
                    val userId = currentUser.uid
                    val credential = EmailAuthProvider.getCredential(user!!.email, user!!.password)
                    currentUser.reauthenticate(credential).addOnCompleteListener { task
                        ->
                        if (task.isSuccessful) {
                            // Actualizar el correo
                            currentUser.updateEmail(email)
                                .addOnCompleteListener { emailTask ->
                                    if (emailTask.isSuccessful) {
                                        // Actualizar la contraseña
                                        currentUser.updatePassword(password)
                                            .addOnCompleteListener { passwordTask ->
                                                if (passwordTask.isSuccessful) {
                                                    // Actualizar el perfil (nickname)
                                                    val profileUpdates =
                                                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                            .setDisplayName(nickname)
                                                            .build()
                                                    currentUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener { profileTask ->
                                                            if (profileTask.isSuccessful) {
                                                                // Actualizar Firestore con los nuevos datos
                                                                val updatedUser = UserApp(
                                                                    userId,
                                                                    email,
                                                                    password,
                                                                    nickname,
                                                                    phone,
                                                                    street,
                                                                    city
                                                                )
                                                                firebaseFirestore.collection("UserApp")
                                                                    .document(userId)
                                                                    .set(updatedUser)
                                                                    .addOnSuccessListener {
                                                                        user = updatedUser
                                                                        callback(
                                                                            true,
                                                                            "Usuario actualizado correctamente"
                                                                        )
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        callback(
                                                                            false,
                                                                            "Error al actualizar en Firestore: ${e.message}"
                                                                        )
                                                                    }
                                                            } else {
                                                                callback(
                                                                    false,
                                                                    "Error al actualizar el perfil: ${profileTask.exception?.message}"
                                                                )
                                                            }
                                                        }
                                                } else {
                                                    callback(
                                                        false,
                                                        "Error al actualizar la contraseña: ${passwordTask.exception?.message}"
                                                    )
                                                }
                                            }
                                    } else {
                                        callback(
                                            false,
                                            "Error al actualizar el correo: ${emailTask.exception?.message}"
                                        )
                                    }
                                }

                        } else {
                            callback(
                                false,
                                "Error al autenticar el usuario: ${task.exception?.message}"
                            )
                        }
                    }
                } else {
                    callback(false, "Error al obtener el usuario actual")
                }
            }
        }
        else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = task.result?.user?.uid

                        if (userId != null) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(nickname)
                                .build()

                            firebaseAuth.currentUser?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        // Guardar en Firestore
                                        val newUser = UserApp(userId, email, password, nickname, phone, street, city)
                                        firebaseFirestore.collection("UserApp")
                                            .document(userId)
                                            .set(newUser)
                                            .addOnSuccessListener {
                                                callback(true, "Usuario creado correctamente")
                                            }
                                            .addOnFailureListener { e ->
                                                callback(false, "Error al guardar en Firestore: ${e.message}")
                                            }
                                    } else {
                                        callback(false, "Error al configurar el perfil: ${profileTask.exception?.message}")
                                    }
                                }
                        } else {
                            callback(false, "Error al obtener UID del usuario")
                        }
                    } else {
                        callback(false, "Error al registrar usuario: ${task.exception?.message}")
                    }
                }
            signOut()
        }
    }
    fun deleteUser(callback: (Boolean, String?) -> Unit) {
        val credential = EmailAuthProvider.getCredential(user!!.email, user!!.password)
        val currentUser = firebaseAuth.currentUser
        val userid = user!!.id

        currentUser!!.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseFirestore.collection("UserApp")
                    .document(userid!!)
                    .delete()
                    .addOnSuccessListener {
                        currentUser.delete().addOnCompleteListener { deleteTask ->
                            if (task.isSuccessful) {
                                callback(true, "Usuario eliminado correctamente")
                                user = null
                            } else {
                                callback(
                                    false,
                                    "Error al eliminar el usuario: ${deleteTask.exception?.message}"
                                )
                            }
                        }
                    }
            }else{
                callback(false, "Error al autenticar el usuario: ${task.exception?.message}")
            }
        }
    }
    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit){
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    setActualUser{
                        if(it){
                            InmobiliariaSingleton.getInstance().signIn(user!!)
                            callback(true, null)
                        }
                    }
                    callback(true, null)
                }else{
                    callback(false, task.exception?.message)
                }
            }
        }
    fun signOut() {
        user = null
        InmobiliariaSingleton.getInstance().signOut()
        firebaseAuth.signOut()
    }

}