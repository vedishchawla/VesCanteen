package com.example.vescanteen.model

/**
 * Represents a registered user, stored in Firestore "users" collection.
 */
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = ""
)
