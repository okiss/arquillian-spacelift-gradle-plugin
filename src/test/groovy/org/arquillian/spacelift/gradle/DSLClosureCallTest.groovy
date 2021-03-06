package org.arquillian.spacelift.gradle

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.slf4j.Logger

class DSLClosureCallTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    void "inherit local methods from closure is not allowed"() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'spacelift'

        project.ext.exception = exception
        // enable first profile
        project.ext.set("profile1", "true")

        project.spacelift {
            tests {
                firstTest {
                    project.exception.expect(MissingMethodException)
                    // here you can't define sharedAsFoo
                    sharedAsFoo {x, y -> println x, y}
                }
                secondTest(from:firstTest) { sharedAsFoo("I'm in secondTest", "a") }
            }
        }
    }

    @Test
    void "define own test workflow"() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'spacelift'

        project.ext.exception = exception

        project.spacelift {
            tests {
                def counter = 0
                def value = { return "foo" + (++counter)}

                firstTest(from:MyOwnTestDefinition) {
                    myDSL {
                        String v = value()
                        assertThat v, is("foo"+counter)
                        return v
                    }
                }
                secondTest(from:firstTest) {
                }
            }
        }

        project.spacelift.tests.each { test ->
            test.executeTest(project.logger)
        }
    }

    @Test
    void "define own installation workflow"() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'spacelift'

        project.ext.exception = exception

        project.spacelift {
            installations {
                customInstallation(from:MyOwnInstallationDefinition) {}
            }
        }

        project.spacelift.installations.each { installation ->
            installation.install(project.logger)
        }
    }

}
