package tech.aaregall.lab.movies

import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.aaregall.lab.movies.test.cleandb.CleanDatabase

@CleanDatabase
class JamesBondUseCaseIT(@Autowired val mockMvc: MockMvc) : AbstractIT() {

    @Test
    fun `Should create Characters interpreted by Actors with HAL association links` () {
        val pierceBrosnanLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Pierce", "last_name": "Brosnan", "birth_date": "1953-05-16"}
            """.trimIndent())

        val danielCraigLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Daniel", "last_name": "Craig", "birth_date": "1968-03-02"}
            """.trimIndent())

        val madsMikkelsenLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Mads", "last_name": "Mikkelsen", "birth_date": "1965-11-22"}
            """.trimIndent())

        val jamesBondLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "James Bond", "actors": ["$pierceBrosnanLink", "$danielCraigLink"]}
            """.trimIndent())

        val leChiffreLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "Le Chiffre", "actors": ["$madsMikkelsenLink"]}
            """.trimIndent())

        performGet("$jamesBondLink/actors")
            .andExpectAll(
                jsonPath("$._embedded.actors.length()").value(2),
                jsonPath("$._embedded.actors[*].first_name", containsInAnyOrder("Pierce", "Daniel")),
                jsonPath("$._embedded.actors[*].last_name", containsInAnyOrder("Brosnan", "Craig"))
            )

        listOf(pierceBrosnanLink, danielCraigLink).forEach {
            performGet("$it/characters")
                .andExpectAll(
                    jsonPath("$._embedded.characters.length()").value(1),
                    jsonPath("$._embedded.characters[0].name").value("James Bond")
                )
        }

        performGet("$leChiffreLink/actors")
            .andExpectAll(
                jsonPath("$._embedded.actors.length()").value(1),
                jsonPath("$._embedded.actors[0].first_name").value("Mads"),
                jsonPath("$._embedded.actors[0].last_name").value("Mikkelsen")
            )

        performGet("$madsMikkelsenLink/characters")
            .andExpectAll(
                jsonPath("$._embedded.characters.length()").value(1),
                jsonPath("$._embedded.characters[0].name").value("Le Chiffre")
            )
    }


    @Test
    fun `Should create Movies directed by Directors, with Characters interpreted by Actors with HAL association links` () {
        val actor1Link = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Pierce", "last_name": "Brosnan", "birth_date": "1953-05-16"}
            """.trimIndent())

        val actor2Link = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Daniel", "last_name": "Craig", "birth_date": "1968-03-02"}
            """.trimIndent())

        val actor3Link = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Mads", "last_name": "Mikkelsen", "birth_date": "1965-11-22"}
            """.trimIndent())

        val character1Link = createAndReturnSelfHref("/api/characters",
            """
                {"name": "James Bond", "actors": ["$actor1Link", "$actor2Link"]}
            """.trimIndent())

        val character2Link = createAndReturnSelfHref("/api/characters",
            """
                {"name": "Le Chiffre", "actors": ["$actor3Link"]}
            """.trimIndent())

        val directorLink = createAndReturnSelfHref("/api/directors",
            """
                {"first_name": "Martin", "last_name": "Campbell"}
            """.trimIndent())

        val movie1Link = createAndReturnSelfHref("/api/movies",
            """
                {"title": "Goldeneye", 
                "release_date": "1995-12-20", 
                "director": "$directorLink", 
                "characters": ["$character1Link"]}
            """.trimIndent())

        val movie2Link = createAndReturnSelfHref("/api/movies",
            """
                {"title": "Casino Royale", 
                "release_date": "2006-11-14", 
                "director": "$directorLink", 
                "characters": ["$character1Link", "$character2Link"]}
            """.trimIndent())

        performGet(movie1Link)
            .andExpectAll(
                jsonPath("$.id").isNotEmpty,
                jsonPath("$.title").value("Goldeneye"),
                jsonPath("$.release_date").value("1995-12-20")
            )
            .andDo {
                val movieResponse = it.response.contentAsString

                performGet(JsonPath.read(movieResponse, "$._links.director.href"))
                    .andExpectAll(
                        jsonPath("$.id").isNotEmpty,
                        jsonPath("$.first_name").value("Martin"),
                        jsonPath("$.last_name").value("Campbell")
                    )

                performGet(JsonPath.read(movieResponse, "$._links.characters.href"))
                    .andExpectAll(
                        jsonPath("$._embedded.characters.length()").value(1 ),
                        jsonPath("$._embedded.characters[0].name").value("James Bond")
                    )
                    .andDo {charactersResult ->
                        performGet(JsonPath.read(charactersResult.response.contentAsString, "$._embedded.characters[0]._links.actors.href"))
                            .andExpectAll(
                                jsonPath("$._embedded.actors.length()").value(2),
                                jsonPath("$._embedded.actors[*].first_name", containsInAnyOrder("Pierce", "Daniel")),
                                jsonPath("$._embedded.actors[*].last_name", containsInAnyOrder("Brosnan", "Craig"))
                            )
                    }
            }

        performGet(movie2Link)
            .andExpectAll(
                jsonPath("$.id").isNotEmpty,
                jsonPath("$.title").value("Casino Royale"),
                jsonPath("$.release_date").value("2006-11-14")
            )
            .andDo {
                val movieResponse = it.response.contentAsString

                performGet(JsonPath.read(movieResponse, "$._links.director.href"))
                    .andExpectAll(
                        jsonPath("$.id").isNotEmpty,
                        jsonPath("$.first_name").value("Martin"),
                        jsonPath("$.last_name").value("Campbell")
                    )

                performGet(JsonPath.read(movieResponse, "$._links.characters.href"))
                    .andExpectAll(
                        jsonPath("$._embedded.characters.length()").value(2),
                        jsonPath("$._embedded.characters[*].name", containsInAnyOrder("James Bond", "Le Chiffre")),
                    )
            }

    }

    fun performGet(path: String): ResultActions {
        return mockMvc.perform(get(path)
            .accept(HAL_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(HAL_JSON))
    }

    fun performPost(path: String, body: String) : ResultActions {
        return mockMvc.perform(post(path)
            .accept(HAL_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(content().contentType(HAL_JSON))
    }

    fun createAndReturnSelfHref(path: String, body: String): String {
        return JsonPath.read(
            performPost(path, body)
                .andReturn().response.contentAsString, "_links.self.href")
    }

}