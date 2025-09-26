package arun.pkg.sample_notes_spring_boot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SampleNotesSpringBootApplication

fun main(args: Array<String>) {
	runApplication<SampleNotesSpringBootApplication>(*args)
}
