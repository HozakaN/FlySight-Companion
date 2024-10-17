package fr.hozakan.flysightble.configfilesmodule.business.parser

abstract class MultilineConfigItemParser : ConfigItemParser() {
    private var satisfyCallCounter = 0
    override fun isMultilineParser(): Boolean = true
    fun isSatisfied(): Boolean {
        satisfyCallCounter++
        return configItemFilled() || satisfyCallCounter >= maxLoop()
    }

    protected abstract fun configItemFilled(): Boolean

    fun reset() {
        satisfyCallCounter = 0
        doReset()
    }

    protected abstract fun doReset()

    protected open fun maxLoop() = maxLoop
}

private const val maxLoop = 10