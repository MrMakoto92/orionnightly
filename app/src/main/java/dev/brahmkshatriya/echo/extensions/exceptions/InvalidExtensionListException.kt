package dev.orionlabs.orionmusic.extensions.exceptions

class InvalidExtensionListException(
    val link: String, override val cause: Throwable
) : Exception()
