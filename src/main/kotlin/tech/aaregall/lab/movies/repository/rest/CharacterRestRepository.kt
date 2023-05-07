package tech.aaregall.lab.movies.repository.rest

import org.springframework.data.rest.core.annotation.RepositoryRestResource
import tech.aaregall.lab.movies.domain.Character
import tech.aaregall.lab.movies.domain.QCharacter
import tech.aaregall.lab.movies.repository.querydsl.QuerydslRepository

@RepositoryRestResource(path = "characters", collectionResourceRel = "characters", itemResourceRel = "character")
interface CharacterRestRepository : QuerydslRepository<Character, Long, QCharacter>