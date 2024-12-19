package com.teknos.m8uf2.fxsane.model

data class UserApp(
    val id: String? = null,
    var email: String = "",
    var password: String = "",
    var nickName: String = "",
    var phone: Int = 0,
    var street: String = "",
    var city: String = ""
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(null, "", "", "", 0, "", "")

}