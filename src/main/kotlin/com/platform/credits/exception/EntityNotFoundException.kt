package com.platform.credits.exception

class EntityNotFoundException(entityType: String, field: String, value: Any) :
    RuntimeException("$entityType not found with $field: $value")
