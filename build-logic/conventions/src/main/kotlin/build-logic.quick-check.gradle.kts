import de.skuzzle.restrictimports.verifypublication.VerifyPublicationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.named("quickCheck").configure {
    dependsOn(
        tasks.withType<VerifyPublicationTask>(),
        tasks.named("spotlessCheck"),
        tasks.withType<JavaCompile>(),
        tasks.withType<Javadoc>(),
        tasks.withType<KotlinCompile>(),
        tasks.withType<GroovyCompile>(),
        tasks.withType<Test>()
    )
}
