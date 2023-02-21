package compaas.core

import compaas.core.engine.LanguageInfo

case class Component(
    val name: String,
    val language: LanguageInfo,
    val source: String
)