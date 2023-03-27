package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Character

@RepositoryRestResource(path = "characters", collectionResourceRel = "characters", itemResourceRel = "character")
interface CharacterRestRepository : JpaRepository<Character, Long>