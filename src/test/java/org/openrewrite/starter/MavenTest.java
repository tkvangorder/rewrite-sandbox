package org.openrewrite.starter;

import org.junit.jupiter.api.Test;
import org.openrewrite.maven.ExcludeDependency;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.maven.RemoveDependency;
import org.openrewrite.maven.UpgradeDependencyVersion;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.maven.Assertions.pomXml;

class MavenTest implements RewriteTest {

    @Test
    void parsePom() {
        MavenParser parser = MavenParser.builder().build();

        List<Xml.Document> poms = parser.parse(
          """
                        <project>
                            <groupId>com.mycompany.app</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1</version>
                            <properties>
                                <junit.version>5.7.1</junit.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>com.google.guava</groupId>
                                        <artifactId>guava</artifactId>
                                        <version>28.0-jre</version>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>com.google.guava</groupId>
                                    <artifactId>guava</artifactId>
                                </dependency>
                                <dependency>
                                    <groupId>org.junit.jupiter</groupId>
                                    <artifactId>junit-jupiter-engine</artifactId>
                                    <version>${junit.version}</version>
                                    <scope>test</scope>
                                </dependency>
                            </dependencies>
                        </project>
                """);

        assertThat(poms).hasSize(1);
    }

    @Test
    void removeGuava() {
        rewriteRun(
          (spec) -> spec
            .recipe(new RemoveDependency("com.google.guava", "guava", null)),
          pomXml(
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <junit.version>5.7.1</junit.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>28.0-jre</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>com.google.guava</groupId>
                            <artifactId>guava</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-engine</artifactId>
                            <version>${junit.version}</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
              """,
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <junit.version>5.7.1</junit.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>28.0-jre</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-engine</artifactId>
                            <version>${junit.version}</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
              """
            )
        );
    }

    @Test
    void excludeApiGuardian() {
        rewriteRun(
          (spec) -> spec
            .recipe(new ExcludeDependency("org.apiguardian", "apiguardian-api", "test")),
          pomXml(
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <junit.version>5.7.1</junit.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>28.0-jre</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>com.google.guava</groupId>
                            <artifactId>guava</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-engine</artifactId>
                            <version>${junit.version}</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
              """,
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <junit.version>5.7.1</junit.version>
                    </properties>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>28.0-jre</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>com.google.guava</groupId>
                            <artifactId>guava</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-engine</artifactId>
                            <version>${junit.version}</version>
                            <scope>test</scope>
                            <exclusions>
                                <exclusion>
                                    <groupId>org.apiguardian</groupId>
                                    <artifactId>apiguardian-api</artifactId>
                                </exclusion>
                            </exclusions>
                        </dependency>
                    </dependencies>
                </project>
              """
          )
        );
    }


    @Test
    void updateCommonFileUploads() {
        rewriteRun(
          (spec) -> spec
            .recipe(new UpgradeDependencyVersion("commons-fileupload", "commons-fileupload", "1.4", null, true, null)),
          pomXml(
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <version.commons.fileupload>1.3.1</version.commons.fileupload>
                    </properties>
                    <dependencies>
                        <dependency>
                           <groupId>commons-fileupload</groupId>
                           <artifactId>commons-fileupload</artifactId>
                           <version>${version.commons.fileupload}</version>
                        </dependency>
                    </dependencies>
                </project>
              """,
            """
                <project>
                    <groupId>com.mycompany.app</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1</version>
                    <properties>
                        <version.commons.fileupload>1.4</version.commons.fileupload>
                    </properties>
                    <dependencies>
                        <dependency>
                           <groupId>commons-fileupload</groupId>
                           <artifactId>commons-fileupload</artifactId>
                           <version>${version.commons.fileupload}</version>
                        </dependency>
                    </dependencies>
                </project>
              """
          )
        );
    }

}
