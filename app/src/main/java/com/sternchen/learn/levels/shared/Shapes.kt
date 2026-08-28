package com.sternchen.learn.levels.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.sternchen.learn.R

/**
 * The distinct visual objects used across the naming / matching / color lessons.
 *
 * Each object renders as a clear, high-contrast silhouette. Objects are large
 * and simple (CVI-friendly: single figure, high salience, no clutter). The same
 * [ShapeKind] + [color] tuple defines "sameness" for the matching task.
 */
enum class ShapeKind {
    BALL, STAR, SQUARE, HEART, TRIANGLE, DIAMOND;

    /** Localized spoken/display name of this object. */
    @get:StringRes
    val nameRes: Int
        get() = when (this) {
            BALL -> R.string.obj_ball
            STAR -> R.string.obj_star
            SQUARE -> R.string.obj_square
            HEART -> R.string.obj_heart
            TRIANGLE -> R.string.obj_triangle
            DIAMOND -> R.string.obj_diamond
        }
}

/** A concrete tappable object: a shape rendered in a colour. */
data class ObjectItem(
    val shape: ShapeKind,
    val color: Color,
)

/**
 * Draws a [ObjectItem]'s shape filled with its colour, centred in the given
 * [Modifier] box. Shapes are drawn to normalise to the same bounding box so
 * that size differences come from [Modifier] sizing, not the shape itself.
 */
@Composable
fun ShapeView(item: ObjectItem, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val side = size.minDimension
        val path = when (item.shape) {
            ShapeKind.BALL -> circlePath(side)
            ShapeKind.SQUARE -> squarePath(side)
            ShapeKind.TRIANGLE -> trianglePath(side)
            ShapeKind.HEART -> heartPath(side)
            ShapeKind.STAR -> starPath(side)
            ShapeKind.DIAMOND -> diamondPath(side)
        }
        drawPath(path, color = item.color)
    }
}

private fun DrawScope.circlePath(side: Float): Path {
    val inset = side * 0.02f
    return Path().apply {
        addOval(Rect(inset, inset, side - inset, side - inset))
    }
}

private fun DrawScope.squarePath(side: Float): Path {
    val inset = side * 0.10f
    return Path().apply {
        this.moveTo(inset, inset)
        this.lineTo(side - inset, inset)
        this.lineTo(side - inset, side - inset)
        this.lineTo(inset, side - inset)
        this.close()
    }
}

private fun DrawScope.trianglePath(side: Float): Path {
    val inset = side * 0.06f
    return Path().apply {
        this.moveTo(side / 2f, inset)
        this.lineTo(side - inset, side - inset)
        this.lineTo(inset, side - inset)
        this.close()
    }
}

private fun DrawScope.heartPath(side: Float): Path {
    val s = side
    return Path().apply {
        // Two lobes + bottom point, normalised to the bounding box.
        moveTo(s / 2f, s * 0.82f)
        cubicTo(s * 0.06f, s * 0.42f, s * 0.22f, s * 0.06f, s / 2f, s * 0.30f)
        cubicTo(s * 0.78f, s * 0.06f, s * 0.94f, s * 0.42f, s / 2f, s * 0.82f)
        close()
    }
}

private fun DrawScope.starPath(side: Float): Path {
    val cx = side / 2f
    val cy = side / 2f
    val outer = side * 0.42f
    val inner = side * 0.18f
    return Path().apply {
        var px = 0f
        var py = 0f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val angle = (Math.PI / 5) * i
            val x = cx + r * kotlin.math.sin(angle).toFloat()
            val y = cy - r * kotlin.math.cos(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
            px = x; py = y
        }
        close()
    }
}

private fun DrawScope.diamondPath(side: Float): Path {
    val inset = side * 0.08f
    return Path().apply {
        this.moveTo(side / 2f, inset)
        this.lineTo(side - inset, side / 2f)
        this.lineTo(side / 2f, side - inset)
        this.lineTo(inset, side / 2f)
        this.close()
    }
}
