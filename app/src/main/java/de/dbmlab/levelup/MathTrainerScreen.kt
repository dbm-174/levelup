package de.dbmlab.levelup

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.dbmlab.levelup.ui.theme.LevelUpTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

// -------------------------------------------------------------
// Multiplikations-Pool (1x1 bis 10x10) mit Gewichten
// -------------------------------------------------------------

data class MultiTask(val a: Int, val b: Int, var weight: Int = 5)

fun createInitialPool(): MutableList<MultiTask> {
    return (1..10).flatMap { a ->
        (1..10).map { b ->
            MultiTask(a, b, 5)
        }
    }.toMutableList()
}

// Speichern
fun saveWeights(context: Context, pool: List<MultiTask>) {
    val prefs = context.getSharedPreferences("weights", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    pool.forEach { task ->
        editor.putInt("${task.a}x${task.b}", task.weight)
    }
    editor.apply()
}

// Laden
fun loadWeights(context: Context, pool: List<MultiTask>) {
    val prefs = context.getSharedPreferences("weights", Context.MODE_PRIVATE)
    pool.forEach { task ->
        val key = "${task.a}x${task.b}"
        val stored = prefs.getInt(key, task.weight)
        task.weight = stored
    }
}

// Gewichtete Auswahl
fun pickWeighted(pool: List<MultiTask>): MultiTask {
    val total = pool.sumOf { it.weight }
    var rnd = (1..total).random()
    for (t in pool) {
        rnd -= t.weight
        if (rnd <= 0) return t
    }
    return pool.last()
}

// -------------------------------------------------------------
// Aufgabe generieren (inkl. Add + Sub)
// -------------------------------------------------------------

enum class TaskType { MULTIPLY, ADD, SUB }

data class Task(val question: String, val result: Int, val multiRef: MultiTask? = null)

fun generateTask(pool: List<MultiTask>): Task {
    return when (TaskType.values().random()) {
        TaskType.MULTIPLY -> {
            val t = pickWeighted(pool)
            Task("${t.a} × ${t.b}", t.a * t.b, t)
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

// Gewicht anpassen
fun updateWeight(ref: MultiTask?, correct: Boolean) {
    if (ref == null) return
    ref.weight = if (correct) {
        (ref.weight - 1).coerceAtLeast(1)
    } else {
        (ref.weight + 2).coerceAtMost(20)
    }
}

// -------------------------------------------------------------
// Erfolg-Animation
// -------------------------------------------------------------

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

// -------------------------------------------------------------
// HAUPTSPIEL – 20 FRAGEN + Neustart
// -------------------------------------------------------------

@Composable
fun MathTrainerScreen() {

    val context = LocalContext.current

    var pool by remember { mutableStateOf(createInitialPool()) }

    LaunchedEffect(Unit) {
        loadWeights(context, pool)
    }

    var task by remember { mutableStateOf(generateTask(pool)) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    var score by remember { mutableStateOf(0) }
    var questionIndex by remember { mutableStateOf(1) }
    val totalQuestions = 20

    fun newTask() {
        task = generateTask(pool)
        answer = ""
        feedback = null
        showSuccessAnimation = false
    }

    fun restart() {
        score = 0
        questionIndex = 1
        newTask()
    }

    LevelUpTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (questionIndex > totalQuestions) {
                        Text(
                            "Spiel beendet!\nPunkte: $score / $totalQuestions",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { restart() },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Neustart", fontSize = 24.sp)
                        }
                        return@Column
                    }

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
                        textStyle = TextStyle(fontSize = 28.sp),
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

                                updateWeight(task.multiRef, correct)
                                saveWeights(context, pool)

                                if (correct) {
                                    showSuccessAnimation = true
                                    feedback = correctText
                                    score++
                                } else {
                                    feedback = incorrectText
                                }
                            } else {
                                questionIndex++
                                if (questionIndex <= totalQuestions) {
                                    newTask()
                                }
                            }
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(if (feedback == null) "Prüfen" else "Weiter", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    feedback?.let {
                        Text(
                            text = it,
                            fontSize = 28.sp,
                            color = if (it == correctText) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
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
