package com.mycelium.wallet.activity.fio.requests.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mycelium.wallet.activity.fio.requests.SentFioRequestStatusActivity
import com.mycelium.wallet.activity.receive.ReceiveCoinsActivity
import com.mycelium.wallet.activity.send.ManualAddressEntry
import com.mycelium.wallet.activity.send.SendCoinsActivity
import com.mycelium.wallet.activity.send.model.SendBtcModel
import com.mycelium.wallet.activity.send.model.SendCoinsViewModel
import com.mycelium.wallet.activity.send.model.SendEthModel
import com.mycelium.wallet.activity.send.model.SendFioModel
import com.mycelium.wallet.activity.util.BtcFeeFormatter
import com.mycelium.wallet.activity.util.FeeFormatter
import com.mycelium.wapi.wallet.Address
import com.mycelium.wapi.wallet.Util
import com.mycelium.wapi.wallet.WalletAccount
import com.mycelium.wapi.wallet.btc.AbstractBtcAccount
import com.mycelium.wapi.wallet.coins.Value
import com.mycelium.wapi.wallet.eth.EthAccount
import com.mycelium.wapi.wallet.fio.FioAccount
import com.mycelium.wapi.wallet.fio.FioModule
import com.mycelium.wapi.wallet.fio.RegisteredFIOName
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.regex.Pattern

class FioRequestCreateViewModel(val app: Application) : SendCoinsViewModel(app) {
    override val uriPattern = Pattern.compile("[a-zA-Z0-9]+")!!
    override fun sendTransaction(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun getFeeFormatter(): FeeFormatter = BtcFeeFormatter()

    private lateinit var fioModule: FioModule

    override fun init(account: WalletAccount<*>, intent: Intent) {
        super.init(account, intent)
        val walletManager = mbwManager.getWalletManager(false)
        fioModule = walletManager.getModuleById(FioModule.ID) as FioModule
        val accountId = checkNotNull(intent.getSerializableExtra(SendCoinsActivity.ACCOUNT) as UUID)
        val account = mbwManager.getWalletManager(false).getAccount(accountId)
                ?: throw IllegalStateException("Wrong account")

        model = when (account) {
            is FioAccount -> SendFioModel(app, account, intent)
            is AbstractBtcAccount -> SendBtcModel(app, account, intent)
            is EthAccount -> SendEthModel(app, account, intent)
            else -> TODO("Not implemented for this type")
        }

        val fioNames = fioModule.getFIONames(account)
        if (fioNames.isEmpty()) {
            TODO("Handle case when account to registered")
        }

        payeeAccount.value = account
        payeeFioAddreses.value = fioNames
        payeeFioName.value = fioNames[0].name
        payeeTokenPublicAddress.value = account.receiveAddress.toString()
    }

    val payerTokenPublicAddress = MutableLiveData<String>()
    val payeeTokenPublicAddress = MutableLiveData<String>()
    val payeeFioAddreses = MutableLiveData<List<RegisteredFIOName>>()
    val payeeAccount = MutableLiveData<WalletAccount<*>>()

    fun sendRequest(activity: Activity, doOnSuccess: (Unit) -> Unit, doOnError: (Exception) -> Unit) {
        viewModelScope.launch(IO) {
            val fioModule = mbwManager.getWalletManager(false).getModuleById(FioModule.ID) as FioModule
            val uuid = fioModule.getFioAccountByFioName(payeeFioName.value!!)!!
            val fioAccount = mbwManager.getWalletManager(false).getAccount(uuid) as? FioAccount
            if (fioAccount != null) {
                val transferTokensFee = fioAccount.getTransferTokensFee()
                val selectedAccount = mbwManager.selectedAccount
                try {
                    val requestFunds = fioAccount.requestFunds(
                            payerFioName.value!!,
                            payeeFioName.value!!,
                            payeeTokenPublicAddress.value!!,
                            Util.valueToDouble(getAmount().value!!),
                            fioMemo.value ?: "",
                            selectedAccount.basedOnCoinType.symbol,
                            selectedAccount.coinType.symbol,
                            transferTokensFee)

                    withContext(Main) {
                        SentFioRequestStatusActivity.start(
                                activity,
                                getAmount().value ?: Value.zeroValue(selectedAccount.coinType),
                                payeeFioName.value!!,
                                payerFioName.value!!,
                                fioMemo.value ?: "",
                                requestFunds.transactionId)
                        activity.finish()
                    }
                } catch (ex: Exception) {
                    withContext(Main) {
                        doOnError.invoke(ex)
                    }
                }
            }
        }
    }


    fun getPayeeFioAddreses(): List<RegisteredFIOName>? {
        return payeeFioAddreses.value
    }

    override fun processReceivedResults(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
        super.processReceivedResults(requestCode, resultCode, data, activity)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ReceiveCoinsActivity.MANUAL_ENTRY_RESULT_CODE && !data?.getStringExtra(ManualAddressEntry.ADDRESS_RESULT_FIO).isNullOrBlank()) {
                val fioAddress = data?.getStringExtra(ManualAddressEntry.ADDRESS_RESULT_FIO)!!
                val addressResult = data.getSerializableExtra(ManualAddressEntry.ADDRESS_RESULT_NAME) as Address
                payerFioName.value = fioAddress
                payerTokenPublicAddress.value = addressResult.toString()
            }
        }
    }
}