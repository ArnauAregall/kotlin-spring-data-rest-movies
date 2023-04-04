package tech.aaregall.lab.movies.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.springframework.data.rest.core.annotation.RestResource
import java.time.LocalDate

@Entity
@Table(name = "movie")
class Movie (

    @NotNull
    @Column(name = "title")
    var title: String,

    @NotNull
    @Column(name = "release_date")
    var releaseDate: LocalDate,

    @NotNull
    @ManyToOne
    @JoinColumn(name = "director_id")
    @RestResource(path = "director")
    var director: Director

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToMany
    @JoinTable(
        name = "movie_character",
        joinColumns = [JoinColumn(name = "movie_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")]
    )
    @RestResource(path = "characters")
    var characters: MutableSet<Character>? = mutableSetOf()
}
