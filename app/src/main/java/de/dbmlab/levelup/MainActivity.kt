package de.dbmlab.levelup


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.dbmlab.levelup.ui.theme.LevelUpTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LevelUpTheme {
                MathTrainerScreen()
            }
        }
    }
}
