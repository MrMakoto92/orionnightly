package dev.orionlabs.orionmusic.extensions.exceptions

class RequiredExtensionsMissingException(val required: List<String>) :
    Exception("Missing required extensions: ${required.joinToString(", ")}")
