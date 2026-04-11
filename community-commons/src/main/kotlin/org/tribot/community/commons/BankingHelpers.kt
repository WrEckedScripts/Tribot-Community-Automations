package org.tribot.community.commons

import org.tribot.script.sdk.Bank

/**
 * Small collection of banking helpers that are commonly useful across many community
 * scripts. This is just a starting point — feel free to add more.
 *
 * Keep helpers in this module generic and composable. Script-specific logic (e.g.
 * "walk to the Varrock west bank for my fishing script") belongs inside the script
 * module itself, not here.
 */
object BankingHelpers {

    /**
     * Ensures the bank is open (opening it if necessary and waiting for the interface
     * to load). Returns true if the bank is open when the method returns.
     *
     * This is a thin wrapper around [Bank.ensureOpen] — kept in the shared library
     * mostly to give contributors a single place to discover banking helpers from.
     */
    fun ensureBankOpen(): Boolean = Bank.ensureOpen()

    /**
     * Deposits all items not in the given whitelist. Useful at the start of a loop
     * iteration to reset inventory state to a known-good baseline. Returns the number
     * of items deposited, or -1 if the bank could not be opened.
     */
    fun depositAllExcept(vararg itemNames: String): Int {
        if (!ensureBankOpen()) return -1
        return Bank.depositAllExcept(*itemNames)
    }
}
