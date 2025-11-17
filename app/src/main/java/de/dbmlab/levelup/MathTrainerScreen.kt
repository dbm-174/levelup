package de.dbmlab.levelup


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.dbmlab.levelup.ui.theme.LevelUpTheme
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


@Preview(showBackground = true)
@Composable
fun MathTrainerScreen() {

    var task by remember { mutableStateOf(generateTask()) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    fun newTask() {
        task = generateTask()
        answer = ""
        feedback = null
    }

    LevelUpTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) answer = it },
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
                            feedback = if (userValue == task.result) correct else incorrect
                        } else {
                            newTask()
                        }
                    },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(if (feedback == null) stringResource(R.string.check) else stringResource(R.string.next), fontSize = 24.sp)
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
        }
    }
}
