package com.lalilu.util.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun DragAndScrollableCanvas(
    bitmap: ImageBitmap,
    rectSize: Size,
    rectX: MutableState<Float>,
    rectY: MutableState<Float>,
    scaleValue: MutableState<Float>,
    scope: CoroutineScope = rememberCoroutineScope(),
    bitmapDraw: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val transformMatrix = remember { mutableStateOf(Matrix()) }

    LaunchedEffect(bitmap) {
        bitmapDraw.value = false
    }

    fun reDrawMatrix(newMatrix: Matrix) {
        val tempMatrix = Matrix(transformMatrix.value.values)
        transformMatrix.value = Matrix()
        tempMatrix.timesAssign(newMatrix)
        transformMatrix.value = tempMatrix
    }

    /**
     * fitCenter 图片
     */
    fun photoTiling(containerSize: Size, bitmap: ImageBitmap) {
        val matrix = Matrix()
        var w = bitmap.width.toFloat()
        var h = bitmap.height.toFloat()
        val x = containerSize.width
        val y = containerSize.height
        val temp = y / x - h / w        // 容器和源图的宽高比进行比较

        val scaleTemp: Float = if (temp > 0) x / w else y / h
        w *= scaleTemp
        h *= scaleTemp

        if (temp > 0) {
            matrix.translate(y = (y - h) / 2f)
        } else {
            matrix.translate(x = (x - w) / 2f)
        }
        matrix.scale(x = scaleTemp, y = scaleTemp)
        transformMatrix.value = matrix
    }

    Box(modifier = Modifier.onPointerEvent(PointerEventType.Scroll) {
        scope.launch {
            val position = it.changes.first().position
            val deltaY = it.changes.first().scrollDelta.y
            val currScale = 1f + if (deltaY < 0f) 0.1f else -0.1f
            val zoom = max(if (currScale > 0) currScale else 1f, 0.1f)

            val x = position.x * (1f - zoom)
            val y = position.y * (1f - zoom)

            val newMatrix = Matrix(
                floatArrayOf(
                    zoom, 0f, 0f, 0f,
                    0f, zoom, 0f, 0f,
                    0f, 0f, 1f, 0f,
                    x, y, 0f, 1f
                )
            )
            reDrawMatrix(newMatrix)
        }
    }) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .onPointerEvent(PointerEventType.Move) {
                if (!it.changes.first().pressed) return@onPointerEvent
                val lastPosition = it.changes.first().previousPosition
                val position = it.changes.first().position
                val movementX = position.x - lastPosition.x
                val movementY = position.y - lastPosition.y

                val newMatrix = Matrix(
                    floatArrayOf(
                        1f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f,
                        0f, 0f, 1f, 0f,
                        movementX, movementY, 0f, 1f
                    )
                )
                reDrawMatrix(newMatrix)
            }
        ) {
            if (!bitmapDraw.value) {
                photoTiling(size, bitmap)
                bitmapDraw.value = true
            }
            val zeroX = (size.width - rectSize.width) / 2f
            val zeroY = (size.height - rectSize.height) / 2f

            withTransform(transformBlock = {
                // 计算最小缩放Value，使图片缩放不小于预设框
                val minScaleValue = max(rectSize.width / bitmap.width, rectSize.height / bitmap.height)
                transformMatrix.value.values[Matrix.ScaleX] =
                    transformMatrix.value.values[Matrix.ScaleX]
                        .coerceAtLeast(minScaleValue)

                transformMatrix.value.values[Matrix.ScaleY] =
                    transformMatrix.value.values[Matrix.ScaleY]
                        .coerceAtLeast(minScaleValue)

                val scaleTemp = transformMatrix.value.values[Matrix.ScaleX]
                val scaleWidth = (zeroX - bitmap.width * scaleTemp + rectSize.width)
                    .coerceAtMost(zeroX)
                val scaleHeight = (zeroY - bitmap.height * scaleTemp + rectSize.height)
                    .coerceAtMost(zeroY)

                transformMatrix.value.values[Matrix.TranslateX] =
                    transformMatrix.value.values[Matrix.TranslateX]
                        .coerceIn(scaleWidth, zeroX)

                transformMatrix.value.values[Matrix.TranslateY] =
                    transformMatrix.value.values[Matrix.TranslateY]
                        .coerceIn(scaleHeight, zeroY)

                transform(transformMatrix.value)
                val translateX = transformMatrix.value.values[Matrix.TranslateX]
                val translateY = transformMatrix.value.values[Matrix.TranslateY]
                rectX.value = zeroX - translateX
                rectY.value = zeroY - translateY
                scaleValue.value = scaleTemp
            }) { drawImage(bitmap) }

            withTransform(transformBlock = {
                translate(left = zeroX, top = zeroY)
            }) {
                drawRect(
                    color = Color.Black,
                    size = rectSize,
                    alpha = 0.5f,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
