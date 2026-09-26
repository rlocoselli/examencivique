package fr.audeladedonnees.examen.civique

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.*

private data class Vertex(val x: Double, val y: Double, val z: Double) {
    operator fun plus(b: Vertex) = Vertex(x+b.x, y+b.y, z+b.z)
    operator fun times(t: Double) = Vertex(x*t, y*t, z*t)
}
private data class Face(val normal: Vertex, val u: Vertex, val v: Vertex, val number: Int)
private fun dots(number: Int): List<Pair<Int, Int>> = when (number) {
    1 -> listOf(0 to 0)
    2 -> listOf(-1 to -1, 1 to 1)
    3 -> listOf(-1 to -1, 0 to 0, 1 to 1)
    4 -> listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)
    5 -> dots(4) + (0 to 0)
    else -> listOf(-1 to -1, 1 to -1, -1 to 0, 1 to 0, -1 to 1, 1 to 1)
}

/** Perspective projection: all six faces and their pips rotate together in 3D. */
@Composable fun PivotingDice(value: Int, progress: Float, enabled: Boolean, roll: () -> Unit) {
    Canvas(Modifier.size(100.dp).clickable(enabled = enabled, role = Role.Button, onClickLabel = "Lancer le dé", onClick = roll)
        .semantics { contentDescription = "Dé : $value. Touchez pour lancer." }) {
        val t = progress.toDouble()
        val lift = abs(sin(t * PI * 3)) * (1 - t) * 22.dp.toPx()
        val squash = 1 - .10 * abs(sin(t * PI * 6)) * (1 - t)
        val rx = -.28 + t * 4 * PI
        val ry = .40 + t * 6 * PI
        val rz = -.08 + t * 2 * PI + sin(t * PI * 8) * .08 * (1-t)
        fun rotate(p: Vertex): Vertex {
            val x1 = p.x; val y1 = p.y*cos(rx)-p.z*sin(rx); val z1 = p.y*sin(rx)+p.z*cos(rx)
            val x2 = x1*cos(ry)+z1*sin(ry); val z2 = -x1*sin(ry)+z1*cos(ry)
            return Vertex(x2*cos(rz)-y1*sin(rz), x2*sin(rz)+y1*cos(rz), z2)
        }
        fun project(p: Vertex): Offset {
            val camera = 5.0 / (5.0-p.z)
            val radius = size.width * .235
            return Offset((size.width/2 + p.x*radius*camera + sin(t*PI*2)*(1-t)*8.dp.toPx()).toFloat(),
                (size.height*.48 + p.y*radius*camera*squash-lift).toFloat())
        }
        drawOval(Color.Black.copy(alpha = (.23-lift/size.height*.3).toFloat().coerceIn(.08f,.23f)),
            Offset(size.width*.17f, size.height*.83f), Size(size.width*.66f, size.height*.10f))
        val remaining = (1..6).filter { it != value && it != 7-value }
        val side = remaining.first()
        val top = remaining.first { it != side && it != 7-side }
        val faces = listOf(
            Face(Vertex(0.0,0.0,1.0),Vertex(1.0,0.0,0.0),Vertex(0.0,1.0,0.0),value),
            Face(Vertex(0.0,0.0,-1.0),Vertex(-1.0,0.0,0.0),Vertex(0.0,1.0,0.0),7-value),
            Face(Vertex(1.0,0.0,0.0),Vertex(0.0,0.0,-1.0),Vertex(0.0,1.0,0.0),side),
            Face(Vertex(-1.0,0.0,0.0),Vertex(0.0,0.0,1.0),Vertex(0.0,1.0,0.0),7-side),
            Face(Vertex(0.0,-1.0,0.0),Vertex(1.0,0.0,0.0),Vertex(0.0,0.0,1.0),top),
            Face(Vertex(0.0,1.0,0.0),Vertex(1.0,0.0,0.0),Vertex(0.0,0.0,-1.0),7-top))
        // Camera at (0,0,5): keep only faces facing it, and paint furthest first.
        faces.filter { rotate(it.normal).z > .2 }.sortedBy { rotate(it.normal).z }.forEach { face ->
            fun facePoint(u: Double, v: Double) = project(rotate(face.normal + face.u*u + face.v*v))
            val corners = listOf(-1.0 to -1.0, 1.0 to -1.0, 1.0 to 1.0, -1.0 to 1.0)
            val path = Path().apply { corners.forEachIndexed { i, (u,v) -> val p = facePoint(u,v); if (i==0) moveTo(p.x,p.y) else lineTo(p.x,p.y) }; close() }
            val light = (.68 + .30 * rotate(face.normal).z).toFloat()
            drawPath(path, Brush.linearGradient(listOf(Color(light,light,(light+.02f).coerceAtMost(1f)), Color(light*.85f,light*.9f,light)), facePoint(-1.0,-1.0), facePoint(1.0,1.0)))
            drawPath(path, Color(0xFFB1C3DF), style = Stroke(1.dp.toPx()))
            dots(face.number).forEach { (x,y) ->
                val pip = Path().apply {
                    repeat(20) { i ->
                        val a = i*PI/10
                        val p = facePoint(x*.53 + cos(a)*.14, y*.53 + sin(a)*.14)
                        if (i==0) moveTo(p.x,p.y) else lineTo(p.x,p.y)
                    }; close()
                }
                drawPath(pip, if (face.number==1) Color(0xFFD95064) else Color(0xFF182C4B))
            }
        }
    }
}
