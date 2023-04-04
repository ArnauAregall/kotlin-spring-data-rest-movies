package tech.aaregall.lab.movies.domain

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.Objects.isNull

@Entity
@Table(name = "actor")
open class Actor (

    @NotNull
    @Column(name = "first_name")
    var firstName: String,

    @NotNull
    @Column(name = "last_name")
    var lastName: String,

    @NotNull
    @Column(name = "birth_date")
    var birthDate: LocalDate,

    @Column(name = "death_date")
    var deathDate: LocalDate?,

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToMany
    @JoinTable(
        name = "actor_character",
        joinColumns = [JoinColumn(name = "actor_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")]
    )
    open var characters: MutableSet<Character>? = mutableSetOf()

    @JsonProperty("is_alive")
    private fun isAlive(): Boolean = isNull(deathDate)

}
