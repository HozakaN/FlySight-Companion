package fr.hozakan.flysightcompanion.framework.extension

fun <T, R> List<T>.mapConsecutive(transform: (T, T) -> R): List<R> {
    return (0 until this.size - 1).map { i ->
        transform(this[i], this[i + 1])
    }
}

fun <T, R> List<T>.firstNotNullConsecutive(transform: (T, T) -> R?): R? {
    for (i in 0 until this.size - 1) {
        val result = transform(this[i], this[i + 1])
        if (result != null) {
            return result
        }
    }
    return null
}

fun <T, R> List<T>.firstNotNullIndexed(transform: (Int, T, T) -> R?): R? {
    var index = 0
    for (i in 0 until this.size - 1) {
        val result = transform(index, this[i], this[i + 1])
        if (result != null) {
            return result
        }
        index++
    }
    return null
}