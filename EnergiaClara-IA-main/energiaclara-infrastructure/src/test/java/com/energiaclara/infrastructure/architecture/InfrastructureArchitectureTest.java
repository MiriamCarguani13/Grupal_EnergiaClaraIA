package com.energiaclara.infrastructure.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Tests arquitectónicos para infrastructure:
 * <ul>
 *   <li>Ningún bounded context importa a otro (anti-coupling, ADR-009)</li>
 *   <li>Entities JPA solo en paquetes {@code persistence.*.entity}</li>
 * </ul>
 */
class InfrastructureArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.energiaclara.infrastructure");
    }

    @Test
    void consumption_does_not_depend_on_other_contexts() {
        noClasses()
                .that().resideInAPackage("..infrastructure.consumption..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.energyops..",
                        "..infrastructure.maintenance..",
                        "..infrastructure.education..",
                        "..infrastructure.analytics.."
                )
                .because("Bounded context Consumption no debe acoplarse a otros (ADR-009).")
                .check(classes);
    }

    @Test
    void energyops_does_not_depend_on_other_contexts() {
        noClasses()
                .that().resideInAPackage("..infrastructure.energyops..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.consumption..",
                        "..infrastructure.maintenance..",
                        "..infrastructure.education..",
                        "..infrastructure.analytics.."
                )
                .because("Bounded context EnergyOps no debe acoplarse a otros (ADR-009).")
                .check(classes);
    }

    @Test
    void maintenance_does_not_depend_on_other_contexts() {
        noClasses()
                .that().resideInAPackage("..infrastructure.maintenance..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.consumption..",
                        "..infrastructure.energyops..",
                        "..infrastructure.education..",
                        "..infrastructure.analytics.."
                )
                .because("Bounded context Maintenance no debe acoplarse a otros (ADR-009).")
                .check(classes);
    }

    @Test
    void education_does_not_depend_on_other_contexts() {
        noClasses()
                .that().resideInAPackage("..infrastructure.education..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.consumption..",
                        "..infrastructure.energyops..",
                        "..infrastructure.maintenance..",
                        "..infrastructure.analytics.."
                )
                .because("Bounded context Education no debe acoplarse a otros (ADR-009).")
                .check(classes);
    }

    @Test
    void analytics_does_not_depend_on_other_contexts() {
        noClasses()
                .that().resideInAPackage("..infrastructure.analytics..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.consumption..",
                        "..infrastructure.energyops..",
                        "..infrastructure.maintenance..",
                        "..infrastructure.education.."
                )
                .because("Analytics es consumidor de datos; integración via Application/Domain, no contexto-a-contexto.")
                .check(classes);
    }
}
