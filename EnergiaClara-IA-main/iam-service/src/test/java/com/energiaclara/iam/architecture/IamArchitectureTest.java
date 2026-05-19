package com.energiaclara.iam.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class IamArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.energiaclara.iam");
    }

    @Test
    void iam_module_does_not_depend_on_spring() {
        noClasses()
                .that().resideInAPackage("com.energiaclara.iam..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("iam-service es bounded context Spring-free. Wiring se hace en infrastructure (IamWiringConfig).")
                .check(classes);
    }

    @Test
    void iam_module_does_not_depend_on_jpa() {
        noClasses()
                .that().resideInAPackage("com.energiaclara.iam..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "org.hibernate.."
                )
                .because("Las entidades JPA viven en infrastructure, no en iam-service.")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_application_layer() {
        noClasses()
                .that().resideInAPackage("com.energiaclara.iam.domain..")
                .should().dependOnClassesThat().resideInAPackage("com.energiaclara.iam.application..")
                .because("Dependency Rule: dominio no conoce capa application.")
                .check(classes);
    }
}
