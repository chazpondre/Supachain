/*
░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░       ░░░        ░░░      ░░░        ░░░      ░░░       ░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓       ▓▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ████  █████  █████  ███  ███  ████████  ████  █████  █████  ████  ██  ███  ██████████████████████
█████████████████████       ███        ██  ████  ██        ███      ██████  ██████      ███  ████  █████████████████████
*/

package dev.supachain.robot.director

import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.*

internal interface DirectorCore {
    val defaultProvider: Provider<*>
    val directives: MutableMap<String, Directive>
    val toolMap: ToolMap
    val messenger: Messenger
    var defaultSeed: Int?
}
