package fr.hozakan.flysightcompanion.framework.tooling

class Triple<A, B, C>(
    val first: A,
    val second: B,
    val third: C
)

public infix fun <A, B, C> Pair<A, B>.triple(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)