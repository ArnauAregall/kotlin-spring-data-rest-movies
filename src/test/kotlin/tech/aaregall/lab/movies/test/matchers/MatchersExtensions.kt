package tech.aaregall.lab.movies.test.matchers

import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.stream.Stream

fun <T> streamToIsMatcher(stream: Stream<T>): Collection<Matcher<in T?>>? {
    return stream.map(Matchers::`is`).toList()
}