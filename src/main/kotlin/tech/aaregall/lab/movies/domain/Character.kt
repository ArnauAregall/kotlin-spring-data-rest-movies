package tech.aaregall.lab.movies.domain

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

@Entity
@Table(name = "character")
open class Character (

    @NotNull
    @Column(name = "name")
    var name: String?,

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1

    @ManyToMany(mappedBy = "characters")
    var actors: MutableSet<Actor>? = mutableSetOf()

    @ManyToMany
    @JoinTable(
        name = "movie_character",
        joinColumns = [JoinColumn(name = "character_id")],
        inverseJoinColumns = [JoinColumn(name = "movie_id")]
    )
    var movies: MutableSet<Movie>? = mutableSetOf()

}
