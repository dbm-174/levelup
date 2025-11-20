

package de.dbmlab.levelup

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.dbmlab.levelup.ui.theme.LevelUpTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class TaskType { MULTIPLY, ADD, SUB }

data class Task(val question: String, val result: Int)

/* -------------------------
   SPACED REPETITION:
   Multiplication tasks stored in a pool with weights.
   Correct answers decrease weight, making tasks rarer.
---------------------------- */

val multiplyPool = mutableStateListOf(
    Triple(2, 2, 5),
    Triple(3, 4, 5),
    Triple(5, 6, 5),
    Triple(7, 8, 5),
    Triple(9, 3, 5),
    Triple(4, 9, 5),
)

fun pickFromWeighted(pool: List<Triple<Int, Int, Int>>): Triple<Int, Int, Int> {
    val expanded = pool.flatMap { triple -> List(triple.third) { triple } }
    return expanded.random()
}

fun generateTask(): Task {
    return when (TaskType.values().random()) {
        TaskType.MULTIPLY -> {
            val (a, b, _) = pickFromWeighted(multiplyPool)
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

fun updateWeights(task: Task, correct: Boolean) {
    multiplyPool.replaceAll {
        if ("×" in task.question) {
            val parts = task.question.split(" × ")
            val a = parts[0].toInt()
            val b = parts[1].toInt()
            if (it.first == a && it.second == b) {
                val newWeight = if (correct) (it.third - 1).coerceAtLeast(1) else (it.third + 2)
                Triple(it.first, it.second, newWeight)
            } else it
        } else it
    }
}

/* -------------------------
   SUCCESS ANIMATION
---------------------------- */

@Composable
fun SuccessAnimation(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(900)
        visible = false
    }
    if (!visible) return

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val offsetY by animateDpAsState(
            targetValue = if (visible) 0.dp else (-40).dp,
            animationSpec = spring(dampingRatio = 0.5f), label = ""
        )

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(72.dp).offset(y = offsetY)
        )
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
            targetValue = scale, animationSpec = tween(300), label = ""
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD600),
            modifier = Modifier.size(28.dp).offset(x = x, y = y).graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                alpha = animatedScale
            )
        )
    }
}

/* -------------------------
   MAIN GAME: 20 QUESTIONS
---------------------------- */

@Composable
fun MathTrainerScreen() {
    var task by remember { mutableStateOf(generateTask()) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    var score by remember { mutableStateOf(0) }
    var questionIndex by remember { mutableStateOf(1) }
    val totalQuestions = 3

    fun newTask() {
        if (questionIndex > totalQuestions) return
        task = generateTask()
        answer = ""
        feedback = null
        showSuccessAnimation = false
    }

    LevelUpTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text("Frage $questionIndex / $totalQuestions", fontSize = 22.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = task.question,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) answer = it
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 28.sp),
                        modifier = Modifier.width(200.dp),
                        enabled = feedback == null
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val correctText = "Richtig!"
                    val incorrectText = "Falsch — Lösung: ${task.result}"

                    Button(
                        onClick = {
                            if (feedback == null) {
                                val userValue = answer.toIntOrNull()
                                val correct = userValue == task.result

                                updateWeights(task, correct)

                                if (correct) {
                                    showSuccessAnimation = true
                                    feedback = correctText
                                    score++
                                } else {
                                    feedback = incorrectText
                                }
                            } else {
                                questionIndex++
                                newTask()
                            }
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            if (feedback == null) "Prüfen" else "Weiter",
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    feedback?.let {
                        Text(
                            text = it,
                            fontSize = 28.sp,
                            color = if (it == correctText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (questionIndex > totalQuestions) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            "Spiel beendet!\nPunkte: $score / $totalQuestions",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showSuccessAnimation) {
                    SuccessAnimation(modifier = Modifier.size(120.dp))
                }
            }
        }
    }
}