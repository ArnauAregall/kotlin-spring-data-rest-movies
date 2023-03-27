package tech.aaregall.lab.movies.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "director")
data class Director (

    @NotNull
    @Column(name = "first_name")
    var firstName: String,

    @NotNull
    @Column(name = "last_name")
    var lastName: String,

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1

    @OneToMany(mappedBy = "director")
    var movies: Collection<Movie>? = emptyList()
}