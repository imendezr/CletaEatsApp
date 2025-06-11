package com.example.cletaeatsapp.utils

fun Double.format(digits: Int): String = "%.${digits}f".format(this)
