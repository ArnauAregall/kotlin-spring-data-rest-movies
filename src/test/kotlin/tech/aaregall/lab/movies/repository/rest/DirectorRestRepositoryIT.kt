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
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.aaregall.lab.movies.AbstractIT
import tech.aaregall.lab.movies.domain.Director
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase
import tech.aaregall.lab.movies.test.matchers.streamToIsMatcher
import java.util.stream.IntStream

private const val BASE_PATH = "/api/directors"

@CleanDatabase
class DirectorRestRepositoryIT(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val directorRestRepository: DirectorRestRepository) : AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should Get Directors paginated` () {
            val directors = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> directorRestRepository.save(
                    Director( "FirstName $index", "LastName $index")
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .accept(APPLICATION_JSON)
                .param("page", "0")
                .param("size", directors.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.directors.length()").value(directors.size),
                    jsonPath("$._embedded.directors[*].id",
                        containsInAnyOrder(streamToIsMatcher(directors.stream().map{ it.id.toInt() }))
                    ),
                    jsonPath("$._embedded.directors[*].first_name",
                        containsInAnyOrder(streamToIsMatcher(directors.stream().map(Director::firstName)))
                    ),
                    jsonPath("$._embedded.directors[*].last_name",
                        containsInAnyOrder(streamToIsMatcher(directors.stream().map(Director::lastName)))
                    ),
                    jsonPath("$.page").isNotEmpty,
                    jsonPath("$.page.size").value(directors.size),
                    jsonPath("$.page.total_elements").value(directors.size),
                    jsonPath("$.page.total_pages").value(1),
                    jsonPath("$.page.number").value(0)
                )
        }

        @Test
        fun `Should Get a Director by ID` () {
            val director = directorRestRepository.save(Director("Terence", "Young"))

            mockMvc.perform(get("${BASE_PATH}/${director.id}")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(director.id.toInt()),
                    jsonPath("$.first_name").value(director.firstName),
                    jsonPath("$.last_name").value(director.lastName),
                    jsonPath("$._links").isNotEmpty,
                    jsonPath("$._links.self.href", containsString("${BASE_PATH}/${director.id}")),
                    jsonPath("$._links.director.href", containsString("${BASE_PATH}/${director.id}")),
                    jsonPath("$._links.movies.href", containsString("${BASE_PATH}/${director.id}/movies")),
                )
        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Director` () {
            val director = Director("Terence", "Young")

            val result = mockMvc.perform(post(BASE_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(notNullValue()),
                    jsonPath("$.first_name").value(director.firstName),
                    jsonPath("$.last_name").value(director.lastName)
                )
                .andReturn()

            val directorId = JsonPath.read<Int>(result.response.contentAsString, "$.id")

            assertThat(directorRestRepository.getReferenceById(directorId.toLong()))
                .isNotNull
                .extracting(Director::firstName, Director::lastName)
                .containsExactly(director.firstName, director.lastName)
        }

    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch a Director by ID` () {
            val director = directorRestRepository.save(Director("Martine", "Campbelly"))

            mockMvc.perform(patch("${BASE_PATH}/${director.id}")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content("""
                {"first_name": "Martin", "last_name": "Campbell"}
                """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(director.id.toInt()),
                    jsonPath("$.first_name").value("Martin"),
                    jsonPath("$.last_name").value("Campbell")
                )
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete a Director by ID` () {
            val director = directorRestRepository.save(Director("John", "Glen"))

            mockMvc.perform(delete("${BASE_PATH}/${director.id}"))
                .andExpect(status().isNoContent)

            assertThat(directorRestRepository.findById(director.id)).isEmpty
        }

    }

}

