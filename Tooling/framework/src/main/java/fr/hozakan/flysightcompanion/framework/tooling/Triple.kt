package fr.hozakan.flysightcompanion.framework.tooling

infix fun <A, B, C> Pair<A, B>.triple(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)

fun <A, B, C> A.triple(with: B, and: C): Triple<A, B, C> = Triple(this, with, and)