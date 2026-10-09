package dev.orionlabs.orionmusic.extensions.exceptions

class ExtensionNotFoundException(val id: String?) : Exception("Extension not found: $id")
