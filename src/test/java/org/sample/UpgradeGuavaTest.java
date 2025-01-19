package org.sample;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

public class UpgradeGuavaTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.sample.UpgradeGuava");
    }

    @Test
    void upgradeGuava() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.springframework.samples</groupId>
                <artifactId>spring-petclinic</artifactId>
                <version>1.5.1</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>31.0-jre</version>
                    </dependency>
                  </dependencies>
              </project>
              """,
            """              
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>org.springframework.samples</groupId>
                  <artifactId>spring-petclinic</artifactId>
                  <version>1.5.1</version>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>33.4.0-jre</version>
                      </dependency>
                    </dependencies>
                </project>
              """
          )
        );
    }

    @Test
    void upgradeGuavaAndroid() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.springframework.samples</groupId>
                <artifactId>spring-petclinic</artifactId>
                <version>1.5.1</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>31.0-android</version>
                    </dependency>
                  </dependencies>
              </project>
              """,
            """              
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>org.springframework.samples</groupId>
                  <artifactId>spring-petclinic</artifactId>
                  <version>1.5.1</version>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>33.4.0-jre</version>
                      </dependency>
                    </dependencies>
                </project>
              """
          )
        );
    }

}
