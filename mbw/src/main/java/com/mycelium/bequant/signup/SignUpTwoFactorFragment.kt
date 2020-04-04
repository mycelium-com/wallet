package com.mycelium.bequant.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mycelium.wallet.R
import kotlinx.android.synthetic.main.fragment_bequant_sign_up_two_factor.*


class SignUpTwoFactorFragment : Fragment(R.layout.fragment_bequant_sign_up_two_factor) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next.setOnClickListener {
            findNavController().navigate(R.id.action_signUpTwoFactor_to_backupCode)
        }
    }
}