/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solana.mobilewalletadapter.fakedapp.usecase

import com.dgsd.ksol.core.LocalTransactions
import com.dgsd.ksol.core.model.LAMPORTS_IN_SOL
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.ksol.core.programs.memo.MemoProgram
import com.dgsd.ksol.core.programs.system.SystemProgram

enum class MemoTransactionVersion {
    Legacy, V0
}

// NOTE: this is just a minimal implementation of this Solana transaction, for testing purposes. It
// is NOT suitable for production use.
object MemoTransactionUseCase {
    private val LAMPORTS_TO_SEND = LAMPORTS_IN_SOL.toLong()
    private val RECIPIENT_PUBLIC_KEY = PublicKey.fromByteArray(ByteArray(32))

    fun create(publicKeyBytes: ByteArray, latestBlockhash: String): ByteArray {
        val sender = PublicKey.fromByteArray(publicKeyBytes)
        val transaction = LocalTransactions.createUnsignedTransferTransaction(
            sender = sender,
            recipient = RECIPIENT_PUBLIC_KEY,
            lamports = LAMPORTS_TO_SEND,
            memo = createMemoFor(sender),
            recentBlockhash = PublicKey.fromBase58(latestBlockhash)
        )

        return LocalTransactions.serialize(transaction)
    }

    private fun createMemoFor(publicKey: PublicKey): String {
        return "Sending txn to ${publicKey.toBase58String()}"
    }

    fun verify(publicKey: ByteArray, signedTransaction: ByteArray) {
        val sender = PublicKey.fromByteArray(publicKey)
        val transaction = LocalTransactions.deserializeTransaction(signedTransaction)
        require(transaction.message.accountKeys.any { it.publicKey == sender }) { "Sender is not part of account keys" }
        require(transaction.message.accountKeys.any { it.publicKey == RECIPIENT_PUBLIC_KEY }) { "Recipient is not part of account keys" }
        require(transaction.message.instructions.size == 2) { "Invalid instruction added to transaction" }

        val txnInstruction = transaction.message.instructions.first { it.programAccount == SystemProgram.PROGRAM_ID }
        require(SystemProgram.decodeInstruction(txnInstruction.inputData).lamports == LAMPORTS_TO_SEND) { "Signed transaction does not have same amount" }

        val memoInstruction = transaction.message.instructions.first { it.programAccount == MemoProgram.PROGRAM_ID }
        require(MemoProgram.decodeInstruction(memoInstruction.inputData) == createMemoFor(sender)) { "Signed memo transaction does not match the one sent" }

        val signer = transaction.message.accountKeys.singleOrNull { it.isSigner }
        requireNotNull(signer) { "No signer added to transaction" }
        require(signer.publicKey == sender) { "Signer account key not added to transaction" }
    }
}
