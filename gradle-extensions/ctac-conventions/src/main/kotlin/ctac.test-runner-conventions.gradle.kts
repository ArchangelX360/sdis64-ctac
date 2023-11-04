import org.gradle.api.tasks.testing.logging.TestLogEvent

tasks.withType<AbstractTestTask> {
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR)
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
    }
}
