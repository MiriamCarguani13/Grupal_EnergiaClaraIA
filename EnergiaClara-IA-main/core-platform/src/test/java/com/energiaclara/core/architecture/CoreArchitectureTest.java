package com.energiaclara.core.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CoreArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.energiaclara.core");
    }

    @Test
    void domain_does_not_depend_on_spring() {
        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("El dominio core-platform debe ser framework-agnostic (Dependency Rule).")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_jpa() {
        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "org.hibernate.."
                )
                .because("El dominio core-platform no debe conocer JPA/Hibernate.")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_jackson() {
        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                .because("El dominio no debe acoplarse a serializadores específicos.")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_servlet_api() {
        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.servlet..",
                        "javax.servlet.."
                )
                .because("El dominio no debe acoplarse a la capa web.")
                .check(classes);
    }
}
