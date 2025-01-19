package com.yourorg.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class SpringBootPropertyReport extends DataTable<SpringBootPropertyReport.Row> {

    public SpringBootPropertyReport(Recipe recipe) {
        super(recipe,
                "Spring property report",
                "Records any spring property referenced in code.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Property name",
                description = "Name of the Spring property.")
        String propertyName;

        @Column(displayName = "Property reference",
                description = "Where the property is referenced codebase.")
        String source;
    }
}
