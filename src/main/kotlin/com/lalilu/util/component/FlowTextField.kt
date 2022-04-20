package com.lalilu.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlowTextField(
    title: String,
    numberValue: MutableState<Float> = remember { mutableStateOf(0f) }
) {
    val textValue = remember(numberValue.value) { mutableStateOf(numberValue.value.toString()) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Surface(
            color = Color.LightGray,
            shape = RoundedCornerShape(5.dp)
        ) {
            BasicTextField(
                modifier = Modifier.padding(5.dp),
                value = textValue.value,
                singleLine = true,
                maxLines = 1,
                onValueChange = {
                    it.toFloatOrNull()?.let { value ->
                        if (value !in 0f..50000f) {
                            numberValue.value = value.coerceIn(0f, 50000f)
                            return@BasicTextField
                        }
                        numberValue.value = value
                    }
                    textValue.value = it
                })
        }
    }
}