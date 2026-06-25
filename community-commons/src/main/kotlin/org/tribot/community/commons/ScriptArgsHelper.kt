package org.tribot.community.commons

import org.tribot.community.commons.ScriptArgsHelper.RAW_KEY
import org.tribot.community.commons.ScriptArgsHelper.load
import org.tribot.community.commons.ScriptArgsHelper.set
import org.tribot.community.commons.ScriptArgsHelper.setAll


/**
 * Stores and exposes the arguments supplied to a script.
 *
 * Structured arguments use `key:value` pairs separated by `|`, for example:
 * `profile:main|world:352|stop:true`. Parsed keys and values are converted to
 * trimmed and converted to lowercase. Entries that do not contain exactly one `:` separator are ignored,
 * and duplicate keys retain their last parsed value.
 *
 * An argument string without a colon is treated as unstructured input and stored
 * under the reserved [RAW_KEY] key after trimming.
 *
 * This object holds process-wide mutable state. Calling [load] replaces all
 * previously stored arguments, while [set] and [setAll] update the current state.
 */
object ScriptArgsHelper {
    private const val RAW_KEY = "raw"
    private val args = mutableMapOf<String, String>()

    /**
     * Replaces the currently stored arguments with values parsed from [argString].
     *
     * Any string containing a colon is treated as structured input. Otherwise,
     * its trimmed value is stored under [RAW_KEY].
     *
     * @param argString the complete argument string supplied to the script
     */
    fun load(argString: String) {
        args.clear()

        // Check if it's structured (has at least one colon)
        if (argString.contains(":")) {
            args.putAll(parseArgs(argString))
        } else {
            // Store as raw input
            args[RAW_KEY] = argString.trim()
        }
    }

    /**
     * Returns a map containing all arguments.
     */
    fun getAll(): Map<String, String> {
        return args
    }

    /**
     * Returns the value associated with [key], or `null` when the key is absent.
     *
     * Keys parsed by [load] are lowercase. Keys added through [set] or [setAll]
     * are stored exactly as provided.
     */
    fun get(key: String): String? = args[key]

    /**
     * Returns the value associated with [key], or [default] when the key is absent.
     */
    fun getOrDefault(key: String, default: String): String = args.getOrDefault(key, default)

    /**
     * Returns the value associated with [key] as an [Int].
     *
     * @return the parsed integer, or `null` when the key is absent or its value is
     * not a valid integer
     */
    fun getInt(key: String): Int? = args[key]?.toIntOrNull()

    /**
     * Returns whether an argument is stored for [key].
     */
    fun has(key: String): Boolean = key in args

    /**
     * Adds or replaces a single argument.
     *
     * Unlike values parsed by [load], [key] and [value] are stored without
     * lowercasing or trimming.
     */
    fun set(key: String, value: String) {
        args[key] = value
    }

    /**
     * Adds or replaces all entries in [values] without clearing other arguments.
     *
     * Keys and values are stored without lowercasing or trimming.
     */
    fun setAll(values: Map<String, String>) {
        args.putAll(values)
    }

    /**
     * Parses pipe-separated `key:value` entries.
     *
     * Malformed entries are skipped. Keys and values are trimmed and lowercased.
     */
    private fun parseArgs(argString: String): Map<String, String> {
        return argString.split("|").mapNotNull { pair ->
            val parts = pair.split(":", limit = 2)
            if (parts.size == 2) {
                parts[0].trim().lowercase() to parts[1].trim().lowercase()
            } else {
                null
            }
        }.toMap()
    }
}
