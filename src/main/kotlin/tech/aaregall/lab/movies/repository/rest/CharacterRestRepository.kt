package tech.aaregall.lab.movies.repository.rest

import com.querydsl.core.types.dsl.StringExpression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer
import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Character
import tech.aaregall.lab.movies.domain.QCharacter

@RepositoryRestResource(path = "characters", collectionResourceRel = "characters", itemResourceRel = "character")
interface CharacterRestRepository : JpaRepository<Character, Long>, QuerydslPredicateExecutor<Character>, QuerydslBinderCustomizer<QCharacter> {

    override fun customize(bindings: QuerydslBindings, root: QCharacter) {
        bindings.bind(String::class.java).first(StringExpression::containsIgnoreCase)
    }

}