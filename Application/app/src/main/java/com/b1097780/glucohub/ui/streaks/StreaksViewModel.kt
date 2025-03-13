package com.b1097780.glucohub.ui.streaks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper

class StreaksViewModel : ViewModel() {

    private val _currentStreak = MutableLiveData<String>()
    val currentStreak: LiveData<String> = _currentStreak

    private val _highestStreak = MutableLiveData<String>()
    val highestStreak: LiveData<String> = _highestStreak

    private val _multiplierText = MutableLiveData<String>()
    val multiplierText: LiveData<String> = _multiplierText

    private val _streakTip = MutableLiveData<String>()
    val streakTip: LiveData<String> = _streakTip

    fun loadData(context: Context) {
        val current = PreferencesHelper.getUserStreak(context)
        val highest = PreferencesHelper.getHighestStreak(context)
        val multiplier = PreferencesHelper.getCoinMultiplier(context)

        _currentStreak.value = "Your Current Streak: $current"
        _highestStreak.value = "Your Highest Streak: $highest"
        _multiplierText.value = "Your Current Multiplier Progress: x$multiplier"
        _streakTip.value = getStreakTip()
    }

    // Get Random Streak Tip
    private fun getStreakTip(): String {
        val tips = listOf(
            "Stay consistent! Small habits lead to big changes.",
            "Your future self will thank you for today's effort!",
            "Missing one day won't ruin progress, but don’t make it two!",
            "Build your habits, one day at a time!",
            "Consistency beats perfection—keep going!",
            "Every streak starts with a single day. Keep pushing!",
            "The best way to predict your future is to create it.",
            "Turn discipline into habit—make streaks effortless!",
            "Winning is done in the small daily victories.",
            "Streaks aren’t about being perfect, they’re about showing up.",
            "Small progress is still progress. Keep going!",
            "Your habits define your future. Keep stacking good ones!",
            "It’s not about motivation, it’s about commitment.",
            "Do something today that your future self will thank you for.",
            "You don’t need to be extreme, just consistent.",
            "A streak is proof of your dedication—don’t break it!",
            "Push past the excuses—show up for yourself today.",
            "You’ve already come this far, don’t stop now!",
            "The longer your streak, the harder it is to quit!",
            "Streaks aren’t magic, they’re a reflection of your effort!",
            "Momentum is your best friend—keep it going!",
            "Your success is built on what you do daily.",
            "A streak is just a chain of small wins. Keep winning!",
            "Streak today so you don’t have to restart tomorrow!",
            "You are one streak away from your next breakthrough!",
            "Greatness isn’t what you do once, it’s what you do daily.",
            "The key to progress? Never missing two days in a row!",
            "A day missed is a day closer to breaking your streak—stay on track!",
            "Streaks are about trust—prove to yourself that you can do this.",
            "The hardest part is starting. You’re already past that!",
            "Your habit today is your strength tomorrow!",
            "A streak isn’t about how fast you go—it’s about never stopping.",
            "Turn your streak into a game—how long can you keep it alive?",
            "Every day counts. Every effort matters.",
            "Good habits take time. Keep watering your streak!",
            "Every streak has a day one. Make today count!",
            "Nothing changes until you do—keep your streak alive!",
            "The best way to not quit is to never stop!",
            "Your streak isn’t just numbers, it’s proof of your commitment!",
            "When you want to quit, remember why you started.",
            "Habit-building is like planting a tree—nurture it daily!",
            "You don’t have to go fast, you just have to keep going.",
            "The easiest way to keep your streak? Just do it for today!",
            "Every streak day is a win—keep collecting victories!",
            "Think long-term: Where do you want your streak to be in 6 months?",
            "Streaks create discipline, and discipline creates success.",
            "Break the cycle of quitting—build the cycle of consistency!",
            "The pain of breaking a streak is worse than the effort to continue.",
            "If you stop now, you’ll have to start over. Keep moving forward!"
        )
        return tips.random()
    }

}
