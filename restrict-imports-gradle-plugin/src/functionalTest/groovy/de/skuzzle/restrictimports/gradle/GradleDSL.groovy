package de.skuzzle.restrictimports.gradle

enum GradleDSL {
    GROOVY(".gradle"), KOTLIN(".gradle.kts");

    final String fileExtension

    GradleDSL(String fileExtension) {
        this.fileExtension = fileExtension
    }

}
