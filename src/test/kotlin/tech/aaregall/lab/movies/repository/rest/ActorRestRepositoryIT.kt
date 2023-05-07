package tech.aaregall.lab.movies.repository.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
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
import tech.aaregall.lab.movies.domain.Actor
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase
import tech.aaregall.lab.movies.test.matchers.streamToIsMatcher
import java.time.LocalDate
import java.util.stream.IntStream

private const val BASE_PATH = "/api/actors"

@CleanDatabase
class ActorRestRepositoryIT @Autowired constructor (
    val mockMvc: MockMvc,
    val actorRestRepository: ActorRestRepository): AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should find available Actors using pagination` () {
            val actors = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> actorRestRepository.save(
                    Actor( "FirstName $index", "LastName $index", LocalDate.now().minusYears(index.toLong()), null)
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .accept(HAL_JSON)
                .param("page", "0")
                .param("size", actors.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.actors.length()").value(actors.size),
                    jsonPath("$._embedded.actors[*].id",
                        containsInAnyOrder(streamToIsMatcher(actors.stream().map { it.id!!.toInt() }))
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
                .accept(HAL_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(actor.id!!.toInt()),
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

        @Nested
        @DisplayName("GET $BASE_PATH with QueryDSL filters")
        inner class Filter {

            @Test
            fun `Should filter all Actors by first and last name` () {
                val actor1 = actorRestRepository.save(Actor("Roger", "Moore",
                    LocalDate.of(1927, 10, 14), LocalDate.of(2017, 5, 23)))

                val actor2 = actorRestRepository.save(Actor("George", "Lazenby",
                    LocalDate.of(1939, 9, 5), null))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("firstName", "rOgEr"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor1.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].first_name").value(actor1.firstName),
                        jsonPath("$._embedded.actors[0].last_name").value(actor1.lastName),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("lastName", "laZenbY"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor2.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].first_name").value(actor2.firstName),
                        jsonPath("$._embedded.actors[0].last_name").value(actor2.lastName),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )
            }

            @Test
            fun `Should filter all Actors by aliveness` () {
                val actor1 = actorRestRepository.save(Actor("Roger", "Moore",
                    LocalDate.of(1927, 10, 14), LocalDate.of(2017, 5, 23)))

                val actor2 = actorRestRepository.save(Actor("George", "Lazenby",
                    LocalDate.of(1939, 9, 5), null))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .queryParam("alive", "false"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor1.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].is_alive").value(false),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("alive", "true"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor2.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].is_alive").value(true),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )
            }

            @Test
            fun `Should filter all Actors by birth date` () {
                val actor = actorRestRepository.save(Actor("Daniel", "Craig",
                    LocalDate.of(1968, 3, 2), null))

                actorRestRepository.save(Actor("Sean", "Connery",
                    LocalDate.of(1930, 8, 25), LocalDate.of(2020, 10, 31)))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .queryParam("birthDate", "1968-03-02"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].birth_date").value("1968-03-02"),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )
            }

            @Test
            fun `Should filter all Actors by death date` () {
                val actor = actorRestRepository.save(Actor("Sean", "Connery",
                    LocalDate.of(1930, 8, 25), LocalDate.of(2020, 10, 31)))

                actorRestRepository.save(Actor("Daniel", "Craig",
                    LocalDate.of(1968, 3, 2), null))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .queryParam("deathDate", "2020-10-31"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(1),
                        jsonPath("$._embedded.actors[0].id").value(actor.id!!.toInt()),
                        jsonPath("$._embedded.actors[0].death_date").value("2020-10-31"),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )
            }

            @Test
            fun `Should filter all Actors by birth date between` () {
                val actor1 = actorRestRepository.save(Actor("Roger", "Moore",
                    LocalDate.of(1927, 10, 14), LocalDate.of(2017, 5, 23)))

                val actor2 = actorRestRepository.save(Actor("Sean", "Connery",
                    LocalDate.of(1930, 8, 25), LocalDate.of(2020, 10, 31)))

                val actor3 = actorRestRepository.save(Actor("Pierce", "Brosnan",
                    LocalDate.of(1953, 5, 16), null))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .queryParam("birthDate_between", "1925-01-01", "1930-12-31"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(2),
                        jsonPath("$._embedded.actors[*].id",
                            containsInAnyOrder(`is`(actor1.id!!.toInt()), `is`(actor2.id!!.toInt()))
                        ),
                        jsonPath("$._embedded.actors[*].id", not(contains(actor3.id!!.toInt()))),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(2),
                    )
            }

            @Test
            fun `Should filter all Actors by death date between` () {
                val actor1 = actorRestRepository.save(Actor("Roger", "Moore",
                    LocalDate.of(1927, 10, 14), LocalDate.of(2017, 5, 23)))

                val actor2 = actorRestRepository.save(Actor("Sean", "Connery",
                    LocalDate.of(1930, 8, 25), LocalDate.of(2020, 10, 31)))

                val actor3 = actorRestRepository.save(Actor("Pierce", "Brosnan",
                    LocalDate.of(1953, 5, 16), null))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .queryParam("deathDate_between", "2015-01-01", "2020-12-31"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.actors.length()").value(2),
                        jsonPath("$._embedded.actors[*].id",
                            containsInAnyOrder(`is`(actor1.id!!.toInt()), `is`(actor2.id!!.toInt()))
                        ),
                        jsonPath("$._embedded.actors[*].id", not(contains(actor3.id!!.toInt()))),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(2),
                    )
            }
        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create an Actor` (@Autowired objectMapper: ObjectMapper) {
            val actor = Actor("Daniel", "Craig", LocalDate.of(1968, 3, 2), null)

            val result = mockMvc.perform(post(BASE_PATH)
                .accept(HAL_JSON)
                .contentType(HAL_JSON)
                .content(objectMapper.writeValueAsString(actor)))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(HAL_JSON))
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
                .accept(HAL_JSON)
                .content("""
                {"first_name": "Pierce", "last_name": "Brosnan", "birth_date": "1953-05-16", "death_date": null}
                """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(actor.id!!.toInt()),
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

            assertThat(actorRestRepository.findById(actor.id!!)).isEmpty
        }

    }

}