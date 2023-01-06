/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solana.mobilewalletadapter.fakedapp.usecase

import android.net.Uri
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.model.Cluster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// NOTE: this is just a minimal implementation of this Solana RPC call, for testing purposes. It is
// NOT suitable for production use.
object GetLatestBlockhashUseCase {
    @Suppress("BlockingMethodInNonBlockingContext") // running in Dispatchers.IO
    suspend operator fun invoke(rpcUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val api = SolanaApi(Cluster.Custom(rpcUri.toString(), rpcUri.toString()))

            runCatching {
                api.getRecentBlockhash().blockhash
            }.getOrElse { failure ->
                throw GetLatestBlockhashFailedException("Error getting latest blockhash", failure)
            }
        }
    }

    class GetLatestBlockhashFailedException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}