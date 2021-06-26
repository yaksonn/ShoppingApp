package com.yaksonn.shoppingapp.utils

object NameValidation {
    fun validate(charSequence: CharSequence?): Boolean {
        val name = charSequence?.trim()

        return !(name.isNullOrEmpty() || !name.contains(Regex("[A-Za-z\\p{L}]")) || name.contains(Regex("[^A-Za-z0-9\\s\\p{L}]")))
    }
}