package com.example.util

import android.content.Context
import com.example.ui.screens.QuoteItem
import kotlin.random.Random

/**
 * Dedicated provider for inspirational educational quotes displayed on the
 * Premium Exam Portal splash screen. Ensures a different quote is selected
 * across consecutive app sessions without immediate repetition.
 */
object SplashQuoteProvider {

    private const val PREFS_NAME = "premium_splash_quote_prefs"
    private const val KEY_LAST_QUOTE_INDEX = "last_quote_index"
    private const val KEY_SESSION_COUNT = "session_count"

    private val curatedQuotes: List<QuoteItem> = listOf(
        QuoteItem(
            text = "Education is the most powerful weapon which you can use to change the world.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "Dream, dream, dream. Dreams transform into thoughts and thoughts result in action.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "Arise, awake, and stop not till the goal is reached.",
            author = "Swami Vivekananda"
        ),
        QuoteItem(
            text = "Excellence is never an accident. It is always the result of high intention and sincere effort.",
            author = "Aristotle"
        ),
        QuoteItem(
            text = "Discipline is the refining fire by which talent becomes ability and preparation becomes triumph.",
            author = "Jim Rohn"
        ),
        QuoteItem(
            text = "Success is the sum of small efforts, repeated day in and day out with unwavering focus.",
            author = "Robert Collier"
        ),
        QuoteItem(
            text = "The future belongs to those who believe in the beauty of their dreams and prepare for them today.",
            author = "Eleanor Roosevelt"
        ),
        QuoteItem(
            text = "You cannot cross the sea merely by standing and staring at the water.",
            author = "Rabindranath Tagore"
        ),
        QuoteItem(
            text = "An investment in knowledge always pays the finest interest.",
            author = "Benjamin Franklin"
        ),
        QuoteItem(
            text = "Perseverance is not a long race; it is many short races one after the other.",
            author = "Walter Elliot"
        )
    )

    @Volatile
    private var inMemoryLastIndex: Int = -1

    fun getAllQuotes(): List<QuoteItem> = curatedQuotes

    /**
     * Selects a quote for the current splash session, guaranteeing that consecutive
     * sessions never repeat the exact same quote. Persists across app restarts via SharedPreferences.
     */
    fun getQuoteForNextSession(context: Context?): Pair<Int, QuoteItem> {
        val total = curatedQuotes.size
        if (total == 0) {
            return 0 to QuoteItem(
                text = "Knowledge empowers a brighter tomorrow.",
                author = "Premium Exam Portal"
            )
        }

        if (context == null) {
            val nextIdx = (inMemoryLastIndex + 1).mod(total)
            inMemoryLastIndex = nextIdx
            return nextIdx to curatedQuotes[nextIdx]
        }

        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastIndex = prefs.getInt(KEY_LAST_QUOTE_INDEX, -1)
            val sessionCount = prefs.getInt(KEY_SESSION_COUNT, 0)

            val nextIndex = if (lastIndex !in 0 until total) {
                0
            } else {
                // Step forward deterministically with occasional offset so all quotes are experienced
                val step = if (sessionCount % total == total - 1 && total > 2) 2 else 1
                (lastIndex + step) % total
            }

            val finalIndex = if (nextIndex == lastIndex && total > 1) {
                (lastIndex + 1) % total
            } else {
                nextIndex
            }

            prefs.edit()
                .putInt(KEY_LAST_QUOTE_INDEX, finalIndex)
                .putInt(KEY_SESSION_COUNT, sessionCount + 1)
                .apply()

            inMemoryLastIndex = finalIndex
            finalIndex to curatedQuotes[finalIndex]
        } catch (_: Exception) {
            val fallbackIndex = if (inMemoryLastIndex in 0 until total) {
                (inMemoryLastIndex + 1) % total
            } else {
                Random.nextInt(total)
            }
            inMemoryLastIndex = fallbackIndex
            fallbackIndex to curatedQuotes[fallbackIndex]
        }
    }
}
