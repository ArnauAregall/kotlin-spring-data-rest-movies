package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Director

@RepositoryRestResource(path = "directors", collectionResourceRel = "directors", itemResourceRel = "director")
interface DirectorRestRepository : JpaRepository<Director, Long>