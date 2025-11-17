package de.dbmlab.levelup


import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.dbmlab.levelup.ui.theme.LevelUpTheme
import kotlinx.coroutines.delay
import kotlin.random.Random


enum class TaskType { MULTIPLY, ADD, SUB }

data class Task(val question: String, val result: Int)

fun generateTask(): Task {
    return when (TaskType.values().random()) {
        TaskType.MULTIPLY -> {
            val a = Random.nextInt(1, 11)
            val b = Random.nextInt(1, 11)
            Task("$a × $b", a * b)
        }
        TaskType.ADD -> {
            val a = Random.nextInt(0, 101)
            val b = Random.nextInt(0, 101 - a)
            Task("$a + $b", a + b)
        }
        TaskType.SUB -> {
            val a = Random.nextInt(0, 101)
            val b = Random.nextInt(0, a)
            Task("$a − $b", a - b)
        }
    }
}


@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier
) {
    // Sichtbarkeit steuern
    var visible by remember { mutableStateOf(false) }

    // Startet beim ersten Composable-Aufruf
    LaunchedEffect(Unit) {
        visible = true
        delay(900)
        visible = false
    }

    if (!visible) return

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        // Einfliegender Haken
        val offsetY by animateDpAsState(
            targetValue = if (visible) 0.dp else (-40).dp,
            animationSpec = spring(dampingRatio = 0.5f),
            label = ""
        )

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier
                .size(72.dp)
                .offset(y = offsetY)
        )

        // ⭐ Sterne
        StarsOverlay()
    }
}


@Composable
fun StarsOverlay() {
    val stars = listOf(
        Pair(-40.dp, -20.dp),
        Pair(0.dp, -35.dp),
        Pair(40.dp, -10.dp)
    )

    stars.forEach { (x, y) ->
        var scale by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            scale = 1f
            delay(300)
            scale = 0f
        }

        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = tween(300),
            label = ""
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD600),
            modifier = Modifier
                .size(28.dp)
                .offset(x = x, y = y)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    alpha = animatedScale
                )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MathTrainerScreen() {

    var task by remember { mutableStateOf(generateTask()) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) } // State to control animation visibility

    fun newTask() {
        task = generateTask()
        answer = ""
        feedback = null
        showSuccessAnimation = false // Reset animation state
    }

    LevelUpTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(contentAlignment = Alignment.Center) { // Use a Box to overlay the animation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Aufgabe
                    Text(
                        text = task.question,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Eingabefeld
                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) answer = it
                        },
                        label = { Text(stringResource(R.string.answer)) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 28.sp),
                        modifier = Modifier.width(200.dp),
                        enabled = feedback == null
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val correct = stringResource(R.string.correct)
                    val incorrect = stringResource(R.string.incorrect, task.result)

                    // Button
                    Button(
                        onClick = {
                            if (feedback == null) {
                                val userValue = answer.toIntOrNull()
                                if (userValue == task.result) {
                                    showSuccessAnimation =
                                        true // Set state to true to show animation
                                    feedback = correct
                                } else {
                                    feedback = incorrect
                                }
                            } else {
                                newTask()
                            }
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            if (feedback == null) stringResource(R.string.check) else stringResource(
                                R.string.next
                            ), fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rückmeldung
                    feedback?.let {
                        Text(
                            text = it,
                            fontSize = 28.sp,
                            color = if (it == correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Call the animation here, outside the onClick
                if (showSuccessAnimation) {
                    SuccessAnimation(modifier = Modifier.size(120.dp))
                }
            }
        }
    }
}
