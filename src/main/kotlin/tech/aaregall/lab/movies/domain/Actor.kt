package tech.aaregall.lab.movies.domain

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.Objects

@Entity
@Table(name = "actor")
data class Actor (

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
    var id: Long = -1

    @ManyToMany
    @JoinTable(
        name = "actor_character",
        joinColumns = [JoinColumn(name = "actor_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")]
    )
    lateinit var characters: Collection<Character>

    @JsonProperty("is_alive")
    private fun isAlive(): Boolean = Objects.isNull(deathDate)

}
