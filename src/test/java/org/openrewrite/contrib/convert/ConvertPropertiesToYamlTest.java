package org.openrewrite.contrib.convert;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class ConvertPropertiesToYamlTest implements RewriteTest {

    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertPropertiesToYaml(null, true, null));
    }

    @Test
    void convertSimplePropertiesToYaml() {
        rewriteRun(
          properties(
            """
                my.string=value
                my.boolean=true
                my.int=1
                my.float=1.0
              """,
            null,
            spec -> spec.path("application.properties")
          ),
          yaml(
            null,
            """
                my:
                  boolean: true
                  float: 1.0
                  int: 1
                  string: value
              """,
            spec -> spec.path("application.yml")
          )

        );
    }


    @Test
    void convertPropertiesWithCommentsToYaml() {
        rewriteRun(
          properties(
            """
                # This is a string
                # And there are two comments here
                my.string=value
                # This is a boolean
                my.boolean=true
                # This is an int
                my.int=1
                # This is a float
                my.float=1.0
              """,
            null,
            spec -> spec.path("application.properties")
          ),
          yaml(
            null,
            """
                my:
                  # This is a boolean
                  boolean: true
                  # This is a float
                  float: 1.0
                  # This is an int
                  int: 1
                  # This is a string
                  # And there are two comments here
                  string: value
              """,
            spec -> spec.path("application.yml")
          )

        );
    }

}