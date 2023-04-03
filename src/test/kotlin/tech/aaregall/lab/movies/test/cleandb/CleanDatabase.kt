package tech.aaregall.lab.movies.test.cleandb

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.sql.DataSource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(CleanDatabaseCallback::class)
annotation class CleanDatabase

private class CleanDatabaseCallback : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val dataSource = SpringExtension.getApplicationContext(context).getBean(DataSource::class.java)
        ResourceDatabasePopulator(ClassPathResource("db/scripts/clean_db.sql")).execute(dataSource)
    }
}