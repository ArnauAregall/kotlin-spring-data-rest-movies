package tech.aaregall.lab.movies.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "director")
open class Director (

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
    var movies: MutableSet<Movie>? = mutableSetOf()
}