package org.craft.packetfactory.packet.dialog

sealed interface DialogClickAction {

    class RunCommand(val command: String) : DialogClickAction

    class OpenUrl(val url: String) : DialogClickAction

    class CopyToClipboard(val text: String) : DialogClickAction

    class SuggestCommand(val command: String) : DialogClickAction

    class CommandTemplate(val template: String) : DialogClickAction

    class Custom(val id: String) : DialogClickAction
}
