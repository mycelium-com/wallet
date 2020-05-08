package com.mycelium.bequant.receive.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ShowQRViewModel : ViewModel() {
    val addressLabel = MutableLiveData<String>()
    val tagLabel = MutableLiveData<String>()
}