package tech.aaregall.lab.movies.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "character")
data class Character (

    @NotNull
    @Column(name = "name")
    var name: String?,

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1

    @ManyToMany(mappedBy = "characters")
    lateinit var actors: Collection<Actor>

    @ManyToMany
    @JoinTable(
        name = "movie_character",
        joinColumns = [JoinColumn(name = "character_id")],
        inverseJoinColumns = [JoinColumn(name = "movie_id")]
    )
    lateinit var movies: Collection<Movie>

}
