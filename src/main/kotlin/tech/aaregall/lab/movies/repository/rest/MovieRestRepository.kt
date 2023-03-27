package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Movie

@RepositoryRestResource(path = "movies", collectionResourceRel = "movies", itemResourceRel = "movie")
interface MovieRestRepository : JpaRepository<Movie, Long>