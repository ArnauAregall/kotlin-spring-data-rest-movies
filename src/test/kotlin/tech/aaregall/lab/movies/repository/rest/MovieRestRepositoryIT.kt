package tech.aaregall.lab.movies.repository.rest

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
import tech.aaregall.lab.movies.domain.Director
import tech.aaregall.lab.movies.domain.Movie
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase
import tech.aaregall.lab.movies.test.matchers.streamToIsMatcher
import java.time.LocalDate
import java.util.stream.IntStream

private const val BASE_PATH = "/api/movies"

@CleanDatabase
class MovieRestRepositoryIT(
    @Autowired val mockMvc: MockMvc,
    @Autowired val movieRestRepository: MovieRestRepository,
    @Autowired val directorRestRepository: DirectorRestRepository) : AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should Get Movies paginated` () {
            val director = directorRestRepository.save(Director("Director First Name", "Director Last Name"))
            val movies = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> movieRestRepository.save(
                    Movie("Movie $index", LocalDate.now().minusYears(index.toLong()), director)
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .accept(HAL_JSON)
                .param("page", "0")
                .param("size", movies.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.movies.length()").value(movies.size),
                    jsonPath("$._embedded.movies[*].id",
                        containsInAnyOrder(streamToIsMatcher(movies.stream().map { it.id!!.toInt() }))
                    ),
                    jsonPath("$._embedded.movies[*].title",
                        containsInAnyOrder(streamToIsMatcher(movies.stream().map(Movie::title)))
                    ),
                    jsonPath("$._embedded.movies[*].release_date",
                        containsInAnyOrder(streamToIsMatcher(movies.stream().map(Movie::releaseDate).map(LocalDate::toString)))
                    ),
                    jsonPath("$.page").isNotEmpty,
                    jsonPath("$.page.size").value(movies.size),
                    jsonPath("$.page.total_elements").value(movies.size),
                    jsonPath("$.page.total_pages").value(1),
                    jsonPath("$.page.number").value(0)
                )
        }

        @Test
        fun `Should Get a Movie by ID` () {
            val director = directorRestRepository.save(Director("Terence", "Young"))
            val movie = movieRestRepository.save(Movie("From Russia With Love", LocalDate.of(1964, 9, 12), director))

            mockMvc.perform(get("${BASE_PATH}/${movie.id}")
                .accept(HAL_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(movie.id!!.toInt()),
                    jsonPath("$.title").value(movie.title),
                    jsonPath("$.release_date").value(movie.releaseDate.toString()),
                    jsonPath("$._links").isNotEmpty,
                    jsonPath("$._links.self.href", containsString("${BASE_PATH}/${movie.id}")),
                    jsonPath("$._links.movie.href", containsString("${BASE_PATH}/${movie.id}")),
                    jsonPath("$._links.characters.href", containsString("${BASE_PATH}/${movie.id}/characters")),
                    jsonPath("$._links.director.href", containsString("${BASE_PATH}/${movie.id}/director"))
                )
        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Movie` () {
            val director = directorRestRepository.save(Director("Martin", "Cambpell"))

            val movie = Movie("Casino Royale", LocalDate.of(2066, 11, 16), director)

            val directorLink = JsonPath.read<String>(
                mockMvc.perform(get("/api/directors/${director.id}")).andReturn().response.contentAsString, "$._links.self.href")

            val result = mockMvc.perform(post(BASE_PATH)
                .accept(HAL_JSON)
                .content("""
                {"title": "${movie.title}", "release_date":  "${movie.releaseDate}", "director": "$directorLink"}
                """.trimIndent()))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(notNullValue()),
                    jsonPath("$.title").value(movie.title),
                    jsonPath("$.release_date").value(movie.releaseDate.toString())
                )
                .andReturn()

            val movieId = JsonPath.read<Int>(result.response.contentAsString, "$.id")

            val createdMovie = movieRestRepository.getReferenceById(movieId.toLong())

            assertThat(createdMovie)
                .isNotNull
                .extracting(Movie::title, Movie::releaseDate)
                .containsExactly(movie.title, movie.releaseDate)

            assertThat(createdMovie.director.id).isEqualTo(director.id)
        }

    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch a Movie by ID` () {
            val director = directorRestRepository.save(Director("Cary Joji", "Fukunaga"))
            val movie = movieRestRepository.save(Movie("No time to live", LocalDate.of(2021, 10, 1), director))

            mockMvc.perform(patch("${BASE_PATH}/${movie.id}")
                .accept(HAL_JSON)
                .contentType(HAL_JSON)
                .content("""
                {"title": "No Time To Die"}
                """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(movie.id!!.toInt()),
                    jsonPath("$.title").value("No Time To Die")
                )
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete a Movie by ID` () {
            val director = directorRestRepository.save(Director("John", "Glen"))
            val movie = movieRestRepository.save(Movie("Octopussy", LocalDate.of(1983, 6, 6), director))

            mockMvc.perform(delete("${BASE_PATH}/${movie.id}"))
                .andExpect(status().isNoContent)

            assertThat(movieRestRepository.findById(movie.id!!)).isEmpty
        }

    }

}