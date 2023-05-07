package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Movie
import tech.aaregall.lab.movies.domain.QMovie
import tech.aaregall.lab.movies.repository.querydsl.QuerydslRepository

@RepositoryRestResource(path = "movies", collectionResourceRel = "movies", itemResourceRel = "movie")
interface MovieRestRepository : QuerydslRepository<Movie, Long, QMovie> {

    override fun customizeBindings(bindings: QuerydslBindings, root: QMovie) {
        bindDateBetween(bindings, root.releaseDate)
    }

}