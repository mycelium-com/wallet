package com.mycelium.wapi.api

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import com.mrd.bitlib.util.HexUtils
import com.mrd.bitlib.util.Sha256Hash
import com.mycelium.WapiLogger
import com.mycelium.net.ServerEndpoints
import com.mycelium.wapi.api.jsonrpc.JsonRpcTcpClient
import com.mycelium.wapi.api.jsonrpc.RPC
import com.mycelium.wapi.api.jsonrpc.RpcParams
import com.mycelium.wapi.api.jsonrpc.RpcRequestOut
import com.mycelium.wapi.api.request.BroadcastTransactionRequest
import com.mycelium.wapi.api.request.CheckTransactionsRequest
import com.mycelium.wapi.api.request.GetTransactionsRequest
import com.mycelium.wapi.api.request.QueryTransactionInventoryRequest
import com.mycelium.wapi.api.request.QueryUnspentOutputsRequest
import com.mycelium.wapi.api.response.*

/**
 * This is a Wapi Client that avoids calls that require BQS by talking to ElectrumX for related calls
 */
class WapiClientElectrumX(serverEndpoints: ServerEndpoints, logger: WapiLogger, versionCode: String) : WapiClient(serverEndpoints, logger, versionCode) {

    private lateinit var jsonRpcTcpClient: JsonRpcTcpClient

    fun start() {
        Thread {
            jsonRpcTcpClient = JsonRpcTcpClient("electrumx-1.mycelium.com", 50012)
            jsonRpcTcpClient.start()
        }.start()
    }
    override fun queryUnspentOutputs(request: QueryUnspentOutputsRequest): WapiResponse<QueryUnspentOutputsResponse> {
//        public final Collection<Address> addresses;

//        public final int height;
//        public final Collection<TransactionOutputEx> unspent;
        return WapiResponse<QueryUnspentOutputsResponse>();
    }

    override fun queryTransactionInventory(request: QueryTransactionInventoryRequest): WapiResponse<QueryTransactionInventoryResponse> {
//        public final List<Address> addresses;
//        public final int limit;

//        public final int height;
//        public final List<Sha256Hash> txIds;

        return WapiResponse<QueryTransactionInventoryResponse>();
    }

    override fun getTransactions(request: GetTransactionsRequest): WapiResponse<GetTransactionsResponse> {
//        public final Collection<Sha256Hash> txIds;
//        public final Collection<TransactionExApi> transactions;

        return WapiResponse<GetTransactionsResponse>();
    }

    override fun broadcastTransaction(request: BroadcastTransactionRequest): WapiResponse<BroadcastTransactionResponse> {
        val txHex = HexUtils.toHex(request.rawTransaction)
        val response = jsonRpcTcpClient.write("blockchain.transaction.broadcast", RpcParams.listParams(txHex), 50000)
        var txId = response.getResult(String::class.java)!!

        return WapiResponse(BroadcastTransactionResponse(true, Sha256Hash.fromString(txId)))
    }

    override fun checkTransactions(request: CheckTransactionsRequest): WapiResponse<CheckTransactionsResponse> {
//        public final List<Sha256Hash> txIds;
//        public final Collection<TransactionStatus> transactions;

        return WapiResponse<CheckTransactionsResponse>();
    }

    fun serverFeatures(): ServerFeatures {
        val response = jsonRpcTcpClient.write("server.features", RpcParams.listParams(), 50000)
        return response.getResult(ServerFeatures::class.java)!!
    }

    fun estimateFee(blocks : Array<Int>): Map<Int, Double> {
        var requestsList = ArrayList<RpcRequestOut>()
        blocks.forEach { nBlocks ->
            requestsList.add(RpcRequestOut("blockchain.estimatefee", RpcParams.listParams(nBlocks)))
        }

        val response = jsonRpcTcpClient.write(requestsList, 5000)
        val estimatesArray = response.responses

        var results = HashMap<Int, Double>()

        estimatesArray.forEachIndexed { index, response ->
            val estimate = response.getResult(Double::class.java)!!
            results.put(blocks[index], estimate)
        }

        return results
    }
}


data class ServerFeatures (
    @SerializedName("server_version") val serverVersion: String
)