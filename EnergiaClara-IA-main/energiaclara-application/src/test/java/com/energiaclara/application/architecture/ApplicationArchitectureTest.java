package com.energiaclara.application.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ApplicationArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.energiaclara.application");
    }

    @Test
    void application_does_not_depend_on_spring() {
        noClasses()
                .that().resideInAPackage("com.energiaclara.application..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("La capa application orquesta casos de uso sin Spring. Wiring en infrastructure.")
                .check(classes);
    }

    @Test
    void application_does_not_depend_on_jpa() {
        noClasses()
                .that().resideInAPackage("com.energiaclara.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate.."
                )
                .because("Application define puertos; JPA es detalle de infrastructure.")
                .check(classes);
    }
}
