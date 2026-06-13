package com.renzo.beercatalogue;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .importPackages("com.renzo.beercatalogue");

    @Test
    void domainShouldNotDependOnPersistenceOrFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.springframework..",
                        "org.hibernate.."
                );

        rule.check(CLASSES);
    }

    @Test
    void applicationShouldNotDependOnWebInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.web..");

        rule.check(CLASSES);
    }

    @Test
    void webInfrastructureShouldNotBeAccessedByDomainOrApplication() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.web..");

        rule.check(CLASSES);
    }

    @Test
    void jpaEntitiesShouldResideInPersistenceInfrastructure() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..infrastructure.persistence..");

        rule.check(CLASSES);
    }
}
