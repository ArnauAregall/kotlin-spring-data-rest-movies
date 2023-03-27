package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Actor

@RepositoryRestResource(path = "actors", collectionResourceRel = "actors", itemResourceRel = "actor")
interface ActorRestRepository : JpaRepository<Actor, Long>