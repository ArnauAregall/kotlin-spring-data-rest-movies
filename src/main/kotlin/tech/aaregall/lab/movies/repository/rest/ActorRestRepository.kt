package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Actor
import tech.aaregall.lab.movies.domain.QActor
import tech.aaregall.lab.movies.repository.querydsl.QuerydslRepository

@RepositoryRestResource(path = "actors", collectionResourceRel = "actors", itemResourceRel = "actor")
interface ActorRestRepository : QuerydslRepository<Actor, Long, QActor> {

    override fun customizeBindings(bindings: QuerydslBindings, root: QActor) {
        bindings.including(root.firstName, root.lastName, root.birthDate, root.deathDate, root.alive)
        bindDateBetween(bindings, root.birthDate)
        bindDateBetween(bindings, root.deathDate)
    }

}

