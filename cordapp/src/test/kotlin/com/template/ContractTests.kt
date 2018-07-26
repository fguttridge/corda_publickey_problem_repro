package com.template

import net.corda.core.contracts.TransactionState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.transactions.WireTransaction
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.SerializationEnvironmentRule
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import org.junit.Rule
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import kotlin.test.assertEquals
import kotlin.test.assertTrue

data class User(val keyPair: KeyPair, val name: String)

class ContractTests {

    @Rule
    @JvmField
    val testSerialization = SerializationEnvironmentRule()

    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val ledgerServices = MockServices(megaCorp)
    private val dummyNotary = TestIdentity(DUMMY_NOTARY_NAME)

    private fun buildUser1(): User {
        val gen = KeyPairGenerator.getInstance("EC")
        val keySize = 256
        gen.initialize(keySize)
        val keyPair = gen.genKeyPair()
        return User(keyPair, "user1")
    }

    @Test
    fun `dummy test`() {
        val user = buildUser1()
        val tx  = TransactionBuilder(notary = null)
        tx.addOutputState(TransactionState(TemplateState("template_state", user.keyPair.public), TEMPLATE_CONTRACT_ID, dummyNotary.party))
        tx.addCommand(TemplateContract.Commands.Action(), megaCorp.publicKey)
        val wtx = tx.toWireTransaction(ledgerServices)
        val s = wtx.outputsOfType<TemplateState>().single()
        // Data is correct
        assertEquals("template_state", s.data)
        // Key spec changed from Sun EC key to Bouncy Castle EC key
        assertEquals(user.keyPair.public, s.publicKey)
    }
}