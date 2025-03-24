package org.ps.archjunit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@AnalyzeClasses(packages = "org.ps")
public class OverallArchitectureTest {

    /**
     * 1) Classes in package X should only depend on classes in package Y.
     * (Also allow some core Java / framework packages as needed.)
     */
    @ArchTest
    static final ArchRule classesInXShouldOnlyDependOnClassesInY =
            ArchRuleDefinition
                    .classes()
                    .that().resideInAPackage("..x..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "..y..",
                            "java.."
                    );

    /**
     * 2) Classes in the service layer should not access (depend on) controller layer classes.
     */
    @ArchTest
    static final ArchRule serviceLayerShouldNotAccessControllers =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..controller..");

    /**
     * 3) No cyclic dependencies should exist among these packages.
     * This example checks for cycles in all sub-packages under "com.example.myapp".
     * You can refine the pattern if you only want to check specific sub-packages.
     */
    @ArchTest
    static final ArchRule noCyclicDependencies =
            SlicesRuleDefinition.slices()
                    .matching("org.ps.(*)..")
                    .should()
                    .beFreeOfCycles();

    /**
     * 4) Prevent field and setter based injection
     */
    @ArchTest
    static final ArchRule noFieldInjection =
            CompositeArchRule.of(
                    ArchRuleDefinition.noFields()
                            .should().beAnnotatedWith(Autowired.class)
                            .because("Use constructor injection instead of field injection.")
            ).and(
                    ArchRuleDefinition.noMethods()
                            .that().haveNameMatching("set[A-Z].*")
                            .should().beAnnotatedWith(Autowired.class)
                            .because("Use constructor injection instead of setter injection.")
            );

    /**
     * 5) Ensure @Transactional annotation is only used in the service layer.
     */
    @ArchTest
    static final ArchRule transactionalAnnotationOnlyInService =
            CompositeArchRule.of(
                    ArchRuleDefinition.classes()
                            .that().areAnnotatedWith(Transactional.class)
                            .should().resideInAPackage("..service..")
                            .because("Class-level @Transactional belongs in the service layer only.")
            ).and(
                    ArchRuleDefinition.methods()
                            .that().areAnnotatedWith(Transactional.class)
                            .should().beDeclaredInClassesThat().resideInAPackage("..service..")
                            .because("Method-level @Transactional belongs in the service layer only.")
            );

    /**
     * 6) Enforce @Repository and @Service annotation usage in specific packages:
     * - @Repository only in ..repository..
     * - @Service only in ..service..
     */
    @ArchTest
    static final ArchRule repositoryAnnotationInRepositoryPackage =
            ArchRuleDefinition.classes()
                    .that().areAnnotatedWith(Repository.class)
                    .should().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule serviceAnnotationInServicePackage =
            ArchRuleDefinition.classes()
                    .that().areAnnotatedWith(Service.class)
                    .should().resideInAPackage("..service..");
}