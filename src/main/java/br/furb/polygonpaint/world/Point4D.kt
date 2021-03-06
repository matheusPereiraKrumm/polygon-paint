package br.furb.polygonpaint.world

data class Point4D(var x: Double,
                   var y: Double,
                   var z: Double = 0.0,
                   var w: Double = 1.0) {

    fun getPair() = x to y

    fun getTriple() = x to y to z

    companion object {
        fun invertPoint(pto: Point4D) {
            pto.x = pto.x * -1
            pto.y = pto.y * -1
            pto.z = pto.z * -1
            pto.w = pto.w * -1
        }
    }

    /**
     * Retorna uma copia invertida do ponto atual
     */
    fun inverted(): Point4D {
        val point = this.copy()
        Point4D.invertPoint(point)
        return point
    }

    /**
     * Move o ponto atual para a posição do ponto de parametro
     */
    fun moveTo(point: Point4D) {
        x = point.x
        y = point.y
        z = point.z
        w = point.w
    }

    /**
     * Translada o ponto atual com o ponto de parametro
     */
    fun sumTo(point: Point4D) {
        x += point.x
        y += point.y
        z += point.z
        w += point.w
    }

    /**
     * Pega a diferença de eixos entre dois pontos
     */
    fun diffPoints(pto2: Point4D): Point4D {
        return Point4D(
                x - pto2.x,
                y - pto2.y,
                z - pto2.z,
                w - pto2.w)
    }
}
