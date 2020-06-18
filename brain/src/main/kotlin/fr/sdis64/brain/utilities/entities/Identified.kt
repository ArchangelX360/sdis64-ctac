package fr.sdis64.brain.utilities.entities

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
open class Identified {

    @Id
    @GeneratedValue
    var id: Long? = null
}

fun <T : Identified> T.withIdOf(b: T): T = apply {
    this.id = b.id
}

fun <T : Identified> T.withId(id: Long?): T = apply {
    this.id = id
}
