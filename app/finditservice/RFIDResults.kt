package com.ndzl.finditservice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RFIDResults(val rfidEPC: String): Parcelable