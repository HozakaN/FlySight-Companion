package fr.hozakan.flysightble.model.config

enum class Volume(
    val value: Int,
    val text: String
) {
    Volume0(0, "0%"),
    Volume1(1, "12.5%"),
    Volume2(2, "25%"),
    Volume3(3, "37.5%"),
    Volume4(4, "50%"),
    Volume5(5, "62.5%"),
    Volume6(6, "75%"),
    Volume7(7, "87.5%"),
    Volume8(8, "100%");

    companion object {
        fun fromText(text: String): Volume? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): Volume? {
            return entries.firstOrNull { it.value == value }
        }
    }

}