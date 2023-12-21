import com.diffplug.gradle.spotless.SpotlessCheck
import de.skuzzle.restrictimports.verifypublication.VerifyPublicationTask
import gradle.kotlin.dsl.accessors._516e34ac18c4e121e5f0f9bca9fd64a8.compileJava
import gradle.kotlin.dsl.accessors._516e34ac18c4e121e5f0f9bca9fd64a8.compileTestJava
import gradle.kotlin.dsl.accessors._516e34ac18c4e121e5f0f9bca9fd64a8.javadoc

tasks.named("quickCheck").configure {
    dependsOn(
        tasks.withType<VerifyPublicationTask>(),
        tasks.withType<SpotlessCheck>(),
        tasks.compileJava,
        tasks.compileTestJava,
        tasks.javadoc,
        tasks.named("test")
    )
}
