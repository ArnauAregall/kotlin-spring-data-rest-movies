package tech.aaregall.lab.movies.repository.rest

import com.querydsl.core.types.dsl.StringExpression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer
import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Director
import tech.aaregall.lab.movies.domain.QDirector

@RepositoryRestResource(path = "directors", collectionResourceRel = "directors", itemResourceRel = "director")
interface DirectorRestRepository : JpaRepository<Director, Long>, QuerydslPredicateExecutor<Director>, QuerydslBinderCustomizer<QDirector> {

    override fun customize(bindings: QuerydslBindings, root: QDirector) {
        bindings.bind(String::class.java).first(StringExpression::containsIgnoreCase)
    }

}