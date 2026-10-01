package com.example.buildingfexfrontend.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.buildingfexfrontend.core.i18n.string

private val IncomeColor = Color(0xFF34C759)
private val ExpenseColor = Color(0xFFFF3B30)
private val LegendColors = listOf(
    Color(0xFF34C759),
    Color(0xFFFFCC00),
    Color(0xFFFF3B30),
    Color(0xFF007AFF),
)

/** Grouped bar chart (income vs expenses) drawn on a Canvas. */
@Composable
fun BarChart(
    labels: List<String>,
    income: List<Double>,
    expenses: List<Double>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            val maxValue = (income + expenses).maxOrNull()?.takeIf { it > 0 }?.toFloat() ?: return@Canvas
            if (labels.isEmpty()) return@Canvas
            val groupWidth = size.width / labels.size
            val barWidth = groupWidth * 0.30f
            val chartHeight = size.height

            labels.forEachIndexed { index, _ ->
                val left = index * groupWidth
                val incomeValue = (income.getOrNull(index) ?: 0.0).toFloat()
                val expenseValue = (expenses.getOrNull(index) ?: 0.0).toFloat()
                val incomeHeight = (incomeValue / maxValue) * chartHeight
                val expenseHeight = (expenseValue / maxValue) * chartHeight

                drawBar(left + groupWidth * 0.18f, chartHeight - incomeHeight, barWidth, incomeHeight, IncomeColor)
                drawBar(left + groupWidth * 0.52f, chartHeight - expenseHeight, barWidth, expenseHeight, ExpenseColor)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegendEntry(color = IncomeColor, label = string("dash.legendIncome"))
            LegendEntry(color = ExpenseColor, label = string("dash.legendExpenses"))
        }
    }
}

private fun DrawScope.drawBar(x: Float, top: Float, width: Float, height: Float, color: Color) {
    if (height <= 0f || width <= 0f) return
    drawRect(color = color, topLeft = Offset(x, top), size = Size(width, height))
}

/** Donut chart (paid / pending / overdue) with a small legend. */
@Composable
fun DonutChart(
    slices: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
) {
    val total = slices.sumOf { it.second }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = 18.dp.toPx())
            val radius = this.size.minDimension / 2 - stroke.width / 2
            if (total <= 0.0) {
                drawCircle(
                    color = Color(0xFFE5E5EA),
                    radius = radius,
                    style = stroke,
                )
                return@Canvas
            }
            var startAngle = -90f
            slices.forEachIndexed { index, (_, value) ->
                if (value <= 0.0) return@forEachIndexed
                val sweep = (value / total) * 360.0
                drawArc(
                    color = LegendColors[index % LegendColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep.toFloat(),
                    useCenter = false,
                    topLeft = Offset(
                        (this.size.width - radius * 2) / 2f,
                        (this.size.height - radius * 2) / 2f,
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = stroke,
                )
                startAngle += sweep.toFloat()
            }
        }
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            slices.forEachIndexed { index, (label, _) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(10.dp)) {
                        drawCircle(color = LegendColors[index % LegendColors.size])
                    }
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape),
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
