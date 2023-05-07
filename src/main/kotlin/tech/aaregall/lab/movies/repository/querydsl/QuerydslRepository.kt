package tech.aaregall.lab.movies.repository.querydsl

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.dsl.DatePath
import com.querydsl.core.types.dsl.StringExpression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer
import org.springframework.data.querydsl.binding.QuerydslBindings
import org.springframework.data.repository.NoRepositoryBean
import java.util.Optional

/**
 * Interface that any repository willing to support QueryDSL predicates should extend.
 * @param T The entity type class.
 * @param ID The entity ID type class.
 * @param Q The QueryDSL entity root class generated from the entity type class.
 */
@NoRepositoryBean
interface QuerydslRepository<T, ID, Q : EntityPath<T>> : JpaRepository<T, ID>, QuerydslPredicateExecutor<T>, QuerydslBinderCustomizer<Q> {

    /**
     * Adds common binding customizations. To provide special binding customizations use customizeBindings.
     * @see customizeBindings
     */
    override fun customize(bindings: QuerydslBindings, root: Q) {
        bindings.bind(String::class.java).first(StringExpression::containsIgnoreCase)
        customizeBindings(bindings, root)
    }

    /**
     * Customize the bindings for the given root.
     * @param bindings the bindings to customize.
     * @param root the entity root.
     */
    fun customizeBindings(bindings: QuerydslBindings, root: Q) {
        // Default implementation is empty
    }

    /**
     * Binds the provided datePath as "path_between" to filter between two dates.
     * @param bindings the bindings.
     * @param datePath the date path to bind to between.
     */
    fun <C : Comparable<C>> bindDateBetween(bindings: QuerydslBindings, datePath: DatePath<C>) {
        bindings.bind(datePath).`as`("${datePath.metadata.name}_between")
            .all { path, values -> if (values.size == 2) Optional.of(path.between(values.first(), values.last())) else Optional.of(path.eq(values.first()))}
    }

}