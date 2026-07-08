package org.craft.packetfactory.packet.dialog

sealed interface DialogInputControl {

    class Text(
        val width: Int = 200,
        val label: String = "",
        val labelVisible: Boolean = true,
        val initial: String = "",
        val maxLength: Int = 32,
        val multiline: Multiline? = null
    ) : DialogInputControl {
        data class Multiline(val maxLines: Int? = null, val height: Int? = null)
    }

     class Toggle(
        val label: String,
        val initial: Boolean = false,
        val onTrue: String = "true",
        val onFalse: String = "false"
    ) : DialogInputControl

    class NumberRange(
        val width: Int = 200,
        val label: String = "",
        val labelFormat: String = "options.generic_value",
        val start: Float,
        val end: Float,
        val initial: Float? = null,
        val step: Float? = null
    ) : DialogInputControl

    class SingleOption(
        val width: Int = 200,
        val label: String = "",
        val labelVisible: Boolean = true,
        val entries: List<OptionEntry>
    ) : DialogInputControl {
        data class OptionEntry(val id: String, val display: String? = null, val initial: Boolean = false)
    }

}

class DialogInput(val key: String, val control: DialogInputControl)
