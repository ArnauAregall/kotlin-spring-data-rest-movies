package tech.aaregall.lab.movies.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import tech.aaregall.lab.movies.domain.Actor
import tech.aaregall.lab.movies.domain.Character
import tech.aaregall.lab.movies.domain.Director
import tech.aaregall.lab.movies.domain.Movie

@Configuration
class RepositoryRestConfig {

    @Bean
    fun repositoryRestConfigurer(): RepositoryRestConfigurer {
        return object : RepositoryRestConfigurer {

            override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration?, cors: CorsRegistry?) {
                config!!.exposeIdsFor(Actor::class.java, Character::class.java, Director::class.java, Movie::class.java)
            }

        }
    }

}