package com.yaksonn.shoppingapp.utils

object AmountValidation {
    fun validate(charSequence: CharSequence?): Boolean {
        val amount = charSequence?.trim()
        return !(amount.isNullOrEmpty() || !amount.contains(Regex("[1-9]")) || amount.contains(Regex("[^0-9]")))
    }
}