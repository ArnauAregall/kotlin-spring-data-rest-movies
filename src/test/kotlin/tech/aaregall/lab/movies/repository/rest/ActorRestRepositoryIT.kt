package tech.aaregall.lab.movies.repository.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.aaregall.lab.movies.AbstractIT
import tech.aaregall.lab.movies.domain.Actor
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase
import tech.aaregall.lab.movies.test.matchers.streamToIsMatcher
import java.time.LocalDate
import java.util.stream.IntStream

private const val BASE_PATH = "/api/actors"

@CleanDatabase
class ActorRestRepositoryIT (
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val actorRestRepository: ActorRestRepository): AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should Get Actors paginated` () {
            val actors = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> actorRestRepository.save(
                    Actor( "FirstName $index", "LastName $index", LocalDate.now().minusYears(index.toLong()), null)
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .accept(APPLICATION_JSON)
                .param("page", "0")
                .param("size", actors.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.actors.length()").value(actors.size),
                    jsonPath("$._embedded.actors[*].id",
                        containsInAnyOrder(streamToIsMatcher(actors.stream().map { it.id.toInt() }))
                    ),
                    jsonPath("$._embedded.actors[*].first_name",
                        containsInAnyOrder(streamToIsMatcher(actors.stream().map(Actor::firstName)))
                    ),
                    jsonPath("$._embedded.actors[*].birth_date",
                        containsInAnyOrder(streamToIsMatcher(actors.stream().map(Actor::birthDate).map(LocalDate::toString)))
                    ),
                    jsonPath("$.page").isNotEmpty,
                    jsonPath("$.page.size").value(actors.size),
                    jsonPath("$.page.total_elements").value(actors.size),
                    jsonPath("$.page.total_pages").value(1),
                    jsonPath("$.page.number").value(0)
                )
        }

        @Test
        fun `Should Get an Actor by ID` () {
            val actor = actorRestRepository.save(Actor("Sean", "Connery",
                LocalDate.of(1930, 8, 25), LocalDate.of(2020, 10, 31)))

            mockMvc.perform(get("${BASE_PATH}/${actor.id}")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(actor.id.toInt()),
                    jsonPath("$.first_name").value(actor.firstName),
                    jsonPath("$.last_name").value(actor.lastName),
                    jsonPath("$.birth_date").value(actor.birthDate.toString()),
                    jsonPath("$.death_date").value(actor.deathDate?.toString()),
                    jsonPath("$.is_alive").value(false),
                    jsonPath("$._links").isNotEmpty,
                    jsonPath("$._links.self.href", containsString("${BASE_PATH}/${actor.id}")),
                    jsonPath("$._links.actor.href", containsString("${BASE_PATH}/${actor.id}")),
                    jsonPath("$._links.characters.href", containsString("${BASE_PATH}/${actor.id}/characters"))
                )
        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create an Actor` () {
            val actor = Actor("Daniel", "Craig", LocalDate.of(1968, 3, 2), null)

            val result = mockMvc.perform(post(BASE_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actor)))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(notNullValue()),
                    jsonPath("$.first_name").value(actor.firstName),
                    jsonPath("$.last_name").value(actor.lastName),
                    jsonPath("$.birth_date").value(actor.birthDate.toString()),
                    jsonPath("$.death_date").value(nullValue()),
                    jsonPath("$.is_alive").value(true)
                ).andReturn()

            val actorId = JsonPath.read<Int>(result.response.contentAsString, "$.id")

            assertThat(actorRestRepository.getReferenceById(actorId.toLong()))
                .isNotNull
                .extracting(Actor::firstName, Actor::lastName, Actor::birthDate, Actor::deathDate)
                .containsExactly(actor.firstName, actor.lastName, actor.birthDate, null)
        }

    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch an Actor by ID` () {
            val actor = actorRestRepository.save(Actor("Pierse", "Bronan", LocalDate.of(1953, 5, 18), LocalDate.now()))

            mockMvc.perform(patch("${BASE_PATH}/${actor.id}")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content("""
                {"first_name": "Pierce", "last_name": "Brosnan", "birth_date": "1953-05-16", "death_date": null}
                """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(actor.id.toInt()),
                    jsonPath("$.first_name").value("Pierce"),
                    jsonPath("$.last_name").value("Brosnan"),
                    jsonPath("$.birth_date").value("1953-05-16"),
                    jsonPath("$.death_date").value(nullValue()),
                    jsonPath("$.is_alive").value(true)
                )
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete an Actor by ID` () {
            val actor = actorRestRepository.save(Actor("Judi", "Dench", LocalDate.of(1934, 12, 9), null))

            mockMvc.perform(delete("${BASE_PATH}/${actor.id}"))
                .andExpect(status().isNoContent)

            assertThat(actorRestRepository.findById(actor.id)).isEmpty
        }

    }

}