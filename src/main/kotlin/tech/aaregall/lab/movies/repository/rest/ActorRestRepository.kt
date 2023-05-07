package tech.aaregall.lab.movies.repository.rest

import com.querydsl.core.types.dsl.DatePath
import com.querydsl.core.types.dsl.StringExpression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer
import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Actor
import tech.aaregall.lab.movies.domain.QActor
import java.time.LocalDate
import java.util.Optional

@RepositoryRestResource(path = "actors", collectionResourceRel = "actors", itemResourceRel = "actor")
interface ActorRestRepository : JpaRepository<Actor, Long>, QuerydslPredicateExecutor<Actor>, QuerydslBinderCustomizer<QActor> {

    override fun customize(bindings: QuerydslBindings, root: QActor) {
        bindings.including(root.firstName, root.lastName, root.birthDate, root.deathDate, root.alive)
        bindings.bind(String::class.java).first(StringExpression::containsIgnoreCase)
        bindDateBetween(bindings, root.birthDate)
        bindDateBetween(bindings, root.deathDate)
    }

    fun bindDateBetween(bindings: QuerydslBindings, datePath: DatePath<LocalDate>) {
        bindings.bind(datePath).`as`("${datePath.metadata.name}_between")
            .all { path, values -> if (values.size == 2) Optional.of(path.between(values.first(), values.last())) else Optional.of(path.eq(values.first()))}
    }

}

