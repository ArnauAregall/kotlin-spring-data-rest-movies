package tech.aaregall.lab.movies

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`is`
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
        // given
        // Actors
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

        val seanBeanLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Sean", "last_name": "Bean", "birth_date": "1959-04-17"}
            """.trimIndent())

        // Characters
        val jamesBondLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "James Bond", "actors": ["$pierceBrosnanLink", "$danielCraigLink"]}
            """.trimIndent())

        val leChiffreLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "Le Chiffre", "actors": ["$madsMikkelsenLink"]}
            """.trimIndent())

        val alecTrevelyanLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "Alec Trevelyan", "actors": ["$seanBeanLink"]}
            """.trimIndent())

        // Director
        val directorLink = createAndReturnSelfHref("/api/directors",
            """
                {"first_name": "Martin", "last_name": "Campbell"}
            """.trimIndent())

        // Movies
        val goldeneyeLink = createAndReturnSelfHref("/api/movies",
            """
                {"title": "Goldeneye", 
                "release_date": "1995-12-20", 
                "director": "$directorLink", 
                "characters": ["$jamesBondLink", "$alecTrevelyanLink"]}
            """.trimIndent())

        val casinoRoyaleLink = createAndReturnSelfHref("/api/movies",
            """
                {"title": "Casino Royale", 
                "release_date": "2006-11-14", 
                "director": "$directorLink", 
                "characters": ["$jamesBondLink", "$leChiffreLink"]}
            """.trimIndent())

        // when and then
        performGet(goldeneyeLink)
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
                        jsonPath("$._embedded.characters.length()").value(2),
                        jsonPath("$._embedded.characters[*].name", containsInAnyOrder("James Bond", "Alec Trevelyan"))
                    )
                    .andDo { charactersResult ->

                        val charactersActorsLinks = JsonPath.read<JSONArray?>(charactersResult.response.contentAsString, "$._embedded.characters[*]._links.actors.href")
                            .filterIsInstance<String>()
                            .sorted()

                        performGet(charactersActorsLinks[0])
                            .andExpectAll(
                                jsonPath("$._embedded.actors.length()").value(2),
                                jsonPath("$._embedded.actors[*].first_name", containsInAnyOrder("Pierce", "Daniel")),
                                jsonPath("$._embedded.actors[*].last_name", containsInAnyOrder("Brosnan", "Craig"))
                            )

                        performGet(charactersActorsLinks[1])
                            .andExpectAll(
                                jsonPath("$._embedded.actors.length()").value(1),
                                jsonPath("$._embedded.actors[0].first_name").value("Sean"),
                                jsonPath("$._embedded.actors[0].last_name").value("Bean")
                            )

                    }
            }

        performGet(casinoRoyaleLink)
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
                    .andDo { charactersResult ->

                        val charactersActorsLinks = JsonPath.read<JSONArray?>(charactersResult.response.contentAsString, "$._embedded.characters[*]._links.actors.href")
                            .filterIsInstance<String>()
                            .sorted()

                        performGet(charactersActorsLinks[0])
                            .andExpectAll(
                                jsonPath("$._embedded.actors.length()").value(2),
                                jsonPath("$._embedded.actors[*].first_name", containsInAnyOrder("Pierce", "Daniel")),
                                jsonPath("$._embedded.actors[*].last_name", containsInAnyOrder("Brosnan", "Craig"))
                            )

                        performGet(charactersActorsLinks[1])
                            .andExpectAll(
                                jsonPath("$._embedded.actors.length()").value(1),
                                jsonPath("$._embedded.actors[0].first_name").value("Mads"),
                                jsonPath("$._embedded.actors[0].last_name").value("Mikkelsen")
                            )

                    }
            }

    }

    @Test
    fun `Should filter Actors by the name of the Director that directed the Movies where they played a Character` () {
        // Director
        val directorLink = createAndReturnSelfHref("/api/directors",
            """
                {"first_name": "Martin", "last_name": "Campbell"}
            """.trimIndent())

        // Actors
        val pierceBrosnanLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Pierce", "last_name": "Brosnan", "birth_date": "1953-05-16"}
            """.trimIndent())

        val danielCraigLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Daniel", "last_name": "Craig", "birth_date": "1968-03-02"}
            """.trimIndent())

        val evaGreenLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Eva", "last_name": "Green", "birth_date": "1980-07-06"}
            """.trimIndent())

        val judiDenchLink = createAndReturnSelfHref("/api/actors",
            """
                {"first_name": "Judi", "last_name": "Dench", "birth_date": "1934-12-09"}
            """.trimIndent())

        // Characters
        val jamesBondLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "James Bond", "actors": ["$pierceBrosnanLink", "$danielCraigLink"]}
            """.trimIndent())

        val vesperLyndLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "Vesper Lynd", "actors": ["$evaGreenLink"]}
            """.trimIndent())

        val mLink = createAndReturnSelfHref("/api/characters",
            """
                {"name": "M", "actors": ["$judiDenchLink"]}
            """.trimIndent())

        // Movies
        performPost("/api/movies",
            """
                {"title": "Goldeneye", 
                "release_date": "1995-12-20", 
                "director": "$directorLink", 
                "characters": ["$jamesBondLink", "$mLink"]}
            """.trimIndent())

        performPost("/api/movies",
            """
                {"title": "Casino Royale", 
                "release_date": "2006-11-14", 
                "director": "$directorLink", 
                "characters": ["$jamesBondLink", "$mLink", "$vesperLyndLink"]}
            """.trimIndent())

        mockMvc.perform(get("/api/actors")
            .queryParam("characters.movies.directors.firstName", "Martin")
            .queryParam("characters.movies.directors.lastName", "Campbell")
            .accept(HAL_JSON))
            .andExpect(status().isOk)
            .andExpectAll(
                jsonPath("$._embedded.actors.length()").value(4),
                jsonPath("$._embedded.actors[*]._links.self.href",
                    containsInAnyOrder(`is`(pierceBrosnanLink), `is`(danielCraigLink), `is`(evaGreenLink), `is`(judiDenchLink))
                ),
                jsonPath("$.page").isNotEmpty,
                jsonPath("$.page.total_elements").value(4),
            )
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