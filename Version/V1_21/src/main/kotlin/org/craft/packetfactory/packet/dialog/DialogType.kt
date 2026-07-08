package org.craft.packetfactory.packet.dialog

sealed interface DialogType {

    val title: String
    val externalTitle: String?
    val canCloseWithEscape: Boolean
    val pause: Boolean
    val afterAction: DialogAfterAction
    val body: List<Body>
    val inputs: List<DialogInput>

    data class Notice(
        override val title: String,
        override val body: List<Body> = emptyList(),
        override val inputs: List<DialogInput> = emptyList(),
        val action: DialogButton = DialogButton("OK"),
        override val externalTitle: String? = null,
        override val canCloseWithEscape: Boolean = true,
        override val pause: Boolean = false,
        override val afterAction: DialogAfterAction = DialogAfterAction.CLOSE
    ) : DialogType

    data class Confirmation(
        override val title: String,
        val yesButton: DialogButton,
        val noButton: DialogButton,
        override val body: List<Body> = emptyList(),
        override val inputs: List<DialogInput> = emptyList(),
        override val externalTitle: String? = null,
        override val canCloseWithEscape: Boolean = true,
        override val pause: Boolean = false,
        override val afterAction: DialogAfterAction = DialogAfterAction.CLOSE
    ) : DialogType

    data class MultiAction(
        override val title: String,
        val actions: List<DialogButton>,
        val exitAction: DialogButton? = null,
        val columns: Int = 2,
        override val body: List<Body> = emptyList(),
        override val inputs: List<DialogInput> = emptyList(),
        override val externalTitle: String? = null,
        override val canCloseWithEscape: Boolean = true,
        override val pause: Boolean = false,
        override val afterAction: DialogAfterAction = DialogAfterAction.CLOSE
    ) : DialogType

    data class ServerLinks(
        override val title: String,
        val exitAction: DialogButton? = null,
        val columns: Int = 2,
        val buttonWidth: Int = 150,
        override val body: List<Body> = emptyList(),
        override val inputs: List<DialogInput> = emptyList(),
        override val externalTitle: String? = null,
        override val canCloseWithEscape: Boolean = true,
        override val pause: Boolean = false,
        override val afterAction: DialogAfterAction = DialogAfterAction.CLOSE
    ) : DialogType
}
