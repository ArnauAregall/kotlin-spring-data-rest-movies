package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Director
import tech.aaregall.lab.movies.domain.QDirector
import tech.aaregall.lab.movies.repository.querydsl.QuerydslRepository

@RepositoryRestResource(path = "directors", collectionResourceRel = "directors", itemResourceRel = "director")
interface DirectorRestRepository : QuerydslRepository<Director, Long, QDirector>