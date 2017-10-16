package br.furb.polygonpaint.world

import br.furb.polygonpaint.gl
import br.furb.polygonpaint.glColor
import br.furb.polygonpaint.glPoint
import br.furb.polygonpaint.polygon
import br.furb.polygonpaint.world.attributes.Color
import br.furb.polygonpaint.world.attributes.Color.WHITE
import br.furb.polygonpaint.world.attributes.GraphicalPrimitive
import com.sun.xml.internal.ws.addressing.EndpointReferenceUtil.transform
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.util.*

class GraphicalObject {
    var color: Color = WHITE
    var graphicalPrimitive: GraphicalPrimitive = GraphicalPrimitive.LINE_LOOP
    private var points: MutableList<Point4D> = ArrayList()
    private lateinit var boundingBox: BoundingBox
    private var transformation: Transformacao4D = Transformacao4D()
    private var selectedPoint: Point4D? = null
    private var children: MutableList<GraphicalObject> = ArrayList()

    fun selectPolygon() {
        throw NotImplementedException()
    }

    fun removePolygon() {
        throw NotImplementedException()
    }

    fun drawBoundingBox() {
        if (hasBoundingBox())
            useTransformation { boundingBox.desenharOpenGLBBox() }
    }

    private fun hasBoundingBox(): Boolean = !points.isEmpty()

    fun draw() {
        gl {
            glColor(color)
            glPointSize(3f)
            glLineWidth(3f)

            useTransformation {
                polygon(graphicalPrimitive, points.size) {
                    points.forEach { glPoint(it) }
                }

                children.forEach { it.draw() }
            }
        }
    }

    private fun useTransformation(block: () -> Unit) {
        gl {
            glPushMatrix()
            glMultMatrixd(transformation.GetDate(), 0)
        }
        block()
        gl {
            glPopMatrix()
        }

    }

    fun selectedPoint(): Point4D? {
        return when {
            selectedPoint != null -> selectedPoint
            hasBoundingBox() -> points.last()
            else -> null
        }
    }

    fun addPoint(point4D: Point4D) {
        if (!hasBoundingBox())
            boundingBox = BoundingBox(point4D)
        else
            boundingBox.atualizarBBox(point4D)

        points.add(point4D)
    }

    private val isLineLoop = { !(points.size == 2 && points[0] == points[1]) }
    fun removeSelectedPoint() {
        points.remove(selectedPoint())
    }

    fun searchNextPoint(point: Point4D) {
        selectedPoint = points.minBy { distancePoints(it, point) }
    }

    private fun distancePoints(pt1: Point4D, pt2: Point4D): Double {
        return Math.pow(pt1.x - pt2.x, 2.0) + Math.pow(pt1.y - pt2.y, 2.0)
    }

    fun atualizaBBox() {
        if (!points.isEmpty()) {
            boundingBox = BoundingBox(points.first())
            points.forEach { boundingBox.atualizarBBox(it) }
        }
    }

    fun isInternal(point: Point4D): Boolean {
        if (boundingBox.isInternal(point)) {
            var intersections = 0
            for (i in 1 until points.size) {
                val pointOne = points[i - 1]
                val pointTwo = points[i]

                if (pointIntersect(point, pointOne, pointTwo))
                    intersections++
            }
            val pointOne = points[0]
            val pointTwo = points[points.size - 1]

            if (pointIntersect(point, pointOne, pointTwo))
                intersections++

            if (intersections % 2 != 0)
                return true
        }

        return false
    }

    private fun pointIntersect(point: Point4D, pointOne: Point4D, pointTwo: Point4D): Boolean {
        val yOneMenor = pointOne.y < pointTwo.y
        val menorY = if (yOneMenor) pointOne.y else pointTwo.y
        val maiorY = if (!yOneMenor) pointOne.y else pointTwo.y
        if (point.y in menorY..maiorY) {
            if (point.x <= pointOne.x || point.x <= pointTwo.x) {
                if (point.x <= pointOne.x && point.x <= pointTwo.x) {
                    return true
                }

                // Parece que só precisa dos cálculos pesados se o ponto clicado estiver
                //   dentro da boundBox Criado pelos dois pontos
                //ScanLine
                val ti = (point.y - pointOne.y) / (pointTwo.y - pointOne.y)
                val xi = pointOne.x + (pointTwo.x - pointOne.x) * ti

                return point.x <= xi
            }
        }
        return false

    }

    fun translate(tx: Double, ty: Double, tz: Double) {
        val matrizTranslate = Transformacao4D()
        matrizTranslate.atribuirTranslacao(tx, ty, tz)
        transformation = matrizTranslate.transformMatrix(transformation)
    }

    fun scale(proportion : Double){
        transformWithCenterBBox {
            val matrizTmpEscala = Transformacao4D()
            matrizTmpEscala.atribuirEscala(proportion, proportion, 1.0)
            return@transformWithCenterBBox matrizTmpEscala
        }
    }

    fun rotation(radians : Double){
        transformWithCenterBBox {
            val matrizTmpEscala = Transformacao4D()
            matrizTmpEscala.atribuirRotacaoZ(radians)
            return@transformWithCenterBBox matrizTmpEscala
        }
    }

    private fun transformWithCenterBBox(block: () -> Transformacao4D) {
        var matrizTmp = Transformacao4D()

        boundingBox.processarCentroBBox()
        var pontoApoio = transformation.transformPoint(boundingBox.centro.inverted())

        val matrizTmpTranslacao = Transformacao4D()
        matrizTmpTranslacao.atribuirTranslacao(pontoApoio.x, pontoApoio.y, pontoApoio.z)
        matrizTmp = matrizTmpTranslacao.transformMatrix(matrizTmp)

        matrizTmp = block().transformMatrix(matrizTmp)

        pontoApoio = pontoApoio.inverted()
        val matrizTmpTranslacaoInversa = Transformacao4D()
        matrizTmpTranslacaoInversa.atribuirTranslacao(pontoApoio.x, pontoApoio.y, pontoApoio.z)
        matrizTmp = matrizTmpTranslacaoInversa.transformMatrix(matrizTmp)

        transformation = matrizTmp.transformMatrix(transformation)
    }

    fun addChild(child: GraphicalObject) {
        children.add(child)
    }

    fun removeChild(child: GraphicalObject) {
        if(!children.remove(child)){
            children.forEach { it.removeChild(child) }
        }
    }
}


