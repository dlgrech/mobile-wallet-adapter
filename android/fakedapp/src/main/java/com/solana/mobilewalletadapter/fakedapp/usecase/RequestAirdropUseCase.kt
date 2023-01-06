/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solana.mobilewalletadapter.fakedapp.usecase

import android.net.Uri
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.model.Cluster
import com.dgsd.ksol.core.model.LAMPORTS_IN_SOL
import com.dgsd.ksol.core.model.PublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// NOTE: this is just a minimal implementation of this Solana RPC call, for testing purposes. It is
// NOT suitable for production use.
object RequestAirdropUseCase {

    @Suppress("BlockingMethodInNonBlockingContext") // running in Dispatchers.IO
    suspend operator fun invoke(rpcUri: Uri, publicKey: ByteArray) {
        withContext(Dispatchers.IO) {
            val api = SolanaApi(Cluster.Custom(rpcUri.toString(), rpcUri.toString()))
            api.requestAirdrop(
                PublicKey.fromByteArray(publicKey),
                LAMPORTS_IN_SOL.longValueExact()
            )
        }
    }

    class AirdropFailedException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}