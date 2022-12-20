/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.starter;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.RemoveAnnotation;
import org.openrewrite.java.tree.J;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveAnnotationFromMethodsOnly extends Recipe {

    @Option(
            displayName = "Method pattern used to determine which method declarations will have their deprecated annotations removed",
            description = "Any method matching this method pattern and having a matching 1.2.0 deprecation will have that annotation removed.",
            example = "java.util.List add(..)"
    )
    @Nullable
    String methodPattern;

    @Option(
            displayName = "Annotation pattern used to determine which deprecated annotations removed",
            description = "Any deprecated annotation matching this pattern and any method matching the method pattern will have that annotation removed.",
            example = "@java.lang.Deprecated(since=\"1.2.0\")"
    )
    String annotationPattern;

    @Override
    public String getDisplayName() {
        return "Remove an annotation from any method declaration that matches a given method pattern";
    }

    @Override
    public String getDescription() {
        return "This is just a demonstration of how to remove annotations from a method declaration.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        MethodMatcher methodMatch = methodPattern != null ? new MethodMatcher(methodPattern) : null;

        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if ((methodMatch == null || methodMatch.matches(method.getMethodType()))) {
                    m = new RemoveAnnotation(annotationPattern).getVisitor().visitMethodDeclaration(method, executionContext);
                }
                return m;
            }
        };
    }
}
