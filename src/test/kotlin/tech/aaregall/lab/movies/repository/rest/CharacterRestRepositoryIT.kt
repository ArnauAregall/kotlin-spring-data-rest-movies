package tech.aaregall.lab.movies.repository.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.aaregall.lab.movies.AbstractIT
import tech.aaregall.lab.movies.domain.Character
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase
import tech.aaregall.lab.movies.test.matchers.streamToIsMatcher
import java.util.stream.IntStream

private const val BASE_PATH = "/api/characters"

@CleanDatabase
class CharacterRestRepositoryIT(
    @Autowired val mockMvc: MockMvc,
    @Autowired val characterRestRepository: CharacterRestRepository) : AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should Get Characters paginated` () {
            val characters = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> characterRestRepository.save(
                    Character( "Character $index")
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .accept(HAL_JSON)
                .param("page", "0")
                .param("size", characters.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.characters.length()").value(characters.size),
                    jsonPath("$._embedded.characters[*].id",
                        containsInAnyOrder(streamToIsMatcher(characters.stream().map { it.id!!.toInt() }))
                    ),
                    jsonPath("$._embedded.characters[*].name",
                        containsInAnyOrder(streamToIsMatcher(characters.stream().map(Character::name)))
                    ),
                    jsonPath("$.page").isNotEmpty,
                    jsonPath("$.page.size").value(characters.size),
                    jsonPath("$.page.total_elements").value(characters.size),
                    jsonPath("$.page.total_pages").value(1),
                    jsonPath("$.page.number").value(0)
                )
        }

        @Test
        fun `Should Get a Character by ID` () {
            val character = characterRestRepository.save(Character("James Bond"))

            mockMvc.perform(get("${BASE_PATH}/${character.id}")
                .accept(HAL_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(character.id!!.toInt()),
                    jsonPath("$.name").value(character.name),
                    jsonPath("$._links").isNotEmpty,
                    jsonPath("$._links.self.href", containsString("${BASE_PATH}/${character.id}")),
                    jsonPath("$._links.character.href", containsString("${BASE_PATH}/${character.id}")),
                    jsonPath("$._links.actors.href", containsString("${BASE_PATH}/${character.id}/actors")),
                    jsonPath("$._links.movies.href", containsString("${BASE_PATH}/${character.id}/movies"))
                )
        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Character` (@Autowired objectMapper: ObjectMapper) {
            val character = Character("Jaws")

            val result = mockMvc.perform(post(BASE_PATH)
                .accept(HAL_JSON)
                .content(objectMapper.writeValueAsString(character)))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(notNullValue()),
                    jsonPath("$.name").value(character.name)
                ).andReturn()

            val characterId = JsonPath.read<Int>(result.response.contentAsString, "$.id")

            assertThat(characterRestRepository.getReferenceById(characterId.toLong()))
                .isNotNull
                .extracting(Character::name)
                .isEqualTo(character.name)
        }

    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch a Character by ID` () {
            val character = characterRestRepository.save(Character("Aurik Silverfinger"))

            mockMvc.perform(patch("${BASE_PATH}/${character.id}")
                .accept(HAL_JSON)
                .content("""
                {"name": "Auric Goldfinger"}
                """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(character.id!!.toInt()),
                    jsonPath("$.name").value("Auric Goldfinger")
                )
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete a Character by ID` () {
            val character = characterRestRepository.save(Character("Le Chiffre"))

            mockMvc.perform(delete("${BASE_PATH}/${character.id}"))
                .andExpect(status().isNoContent)

            assertThat(characterRestRepository.findById(character.id!!)).isEmpty
        }

    }



}