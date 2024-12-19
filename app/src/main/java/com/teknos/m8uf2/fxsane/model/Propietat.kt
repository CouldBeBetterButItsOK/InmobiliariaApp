package com.teknos.m8uf2.fxsane.model

import java.util.Date

data class Propietat(
    var id: String? = null,
    var name: String = "",
    var street: String = "",
    var city: String = "",
    var number: String = "",
    var type: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var m2: Int = 0,
    var userId: String = "",
    var img: String = "",
    var sold: Boolean = false,
    var lastUpdate: Date = Date()
    ) {
        constructor() : this(null, "", "", "", "", "", 0.0, "", 0, "", "",false, Date())

    }
