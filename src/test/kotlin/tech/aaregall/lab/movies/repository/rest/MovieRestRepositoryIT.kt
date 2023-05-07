package tech.aaregall.lab.movies.repository.rest

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
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
class MovieRestRepositoryIT @Autowired constructor (
    val mockMvc: MockMvc,
    val movieRestRepository: MovieRestRepository,
    val directorRestRepository: DirectorRestRepository) : AbstractIT() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {

        @Test
        fun `Should find available Movies using pagination` () {
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

        @Nested
        @DisplayName("GET $BASE_PATH with QueryDSL filters")
        inner class Filter {

            @Test
            fun `Should filter all Movies by title`() {
                val director = directorRestRepository.save(Director("Martin", "Campbell"))

                val movie1 = movieRestRepository.save(Movie("GoldenEye", LocalDate.of(1997, 8, 23), director))
                val movie2 = movieRestRepository.save(Movie("Casino Royale", LocalDate.of(2006, 11, 16), director))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("title", "eye"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie1.id!!.toInt()),
                        jsonPath("$._embedded.movies[0].title").value(movie1.title),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("title", "RoYaL"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie2.id!!.toInt()),
                        jsonPath("$._embedded.movies[0].title").value(movie2.title),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

            }

            @Test
            fun `Should filter all Movies by release date`() {
                val director = directorRestRepository.save(Director("Martin", "Campbell"))

                val movie1 = movieRestRepository.save(Movie("GoldenEye", LocalDate.of(1997, 8, 23), director))
                val movie2 = movieRestRepository.save(Movie("Casino Royale", LocalDate.of(2006, 11, 16), director))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("releaseDate", "1997-08-23"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie1.id!!.toInt()),
                        jsonPath("$._embedded.movies[0].release_date").value(movie1.releaseDate.toString()),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("releaseDate", "2006-11-16"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie2.id!!.toInt()),
                        jsonPath("$._embedded.movies[0].release_date").value(movie2.releaseDate.toString()),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

            }

            @Test
            fun `Should filter all Movies by release date between`() {
                val director = directorRestRepository.save(Director("Martin", "Campbell"))

                val movie1 = movieRestRepository.save(Movie("GoldenEye", LocalDate.of(1997, 8, 23), director))
                val movie2 = movieRestRepository.save(Movie("Casino Royale", LocalDate.of(2006, 11, 16), director))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("releaseDate", "1995-01-01", "2000-12-01"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie1.id!!.toInt()),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("releaseDate", "2005-01-01", "2010-12-31"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie2.id!!.toInt()),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )

            }

            @Test
            fun `Should filter all Movies by Director first name and last name`() {
                val director1 = directorRestRepository.save(Director("Martin", "Campbell"))
                val director2 = directorRestRepository.save(Director("Sam", "Mendes"))

                val movie1 = movieRestRepository.save(Movie("GoldenEye", LocalDate.of(1997, 8, 23), director1))
                val movie2 = movieRestRepository.save(Movie("Casino Royale", LocalDate.of(2006, 11, 16), director1))
                val movie3 = movieRestRepository.save(Movie("Skyfall", LocalDate.of(2012, 10, 31), director2))

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("director.firstName", "marti"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(2),
                        jsonPath("$._embedded.movies[*].id",
                            containsInAnyOrder(`is`(movie1.id!!.toInt()), `is`(movie2.id!!.toInt()))
                        ),
                        jsonPath("$._embedded.movies[*].id", not(contains(movie3.id!!.toInt()))),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(2),
                    )

                mockMvc.perform(get(BASE_PATH)
                    .accept(HAL_JSON)
                    .param("director.lastName", "Mende"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(HAL_JSON))
                    .andExpectAll(
                        jsonPath("$._embedded.movies.length()").value(1),
                        jsonPath("$._embedded.movies[0].id").value(movie3.id!!.toInt()),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.total_elements").value(1),
                    )
            }

        }

    }

    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Movie` () {
            val director = directorRestRepository.save(Director("Martin", "Cambpell"))

            val movie = Movie("Casino Royale", LocalDate.of(2006, 11, 16), director)

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