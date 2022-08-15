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
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Comparator;


@Value
@EqualsAndHashCode(callSuper = true)
public class AddDeprecatedToMethods extends Recipe {

    @Option(
            displayName = "Method pattern used to determine which method declarations will be marked as deprecated",
            description = "Any method matching this method pattern will be marked as deprecated, if not supplied all method declarations will be marked as deprecated.",
            example = "java.util.List add(..)"
    )
    @Nullable
    String methodPattern;

    @Override
    public String getDisplayName() {
        return "Add `@Deprecated to any method declaration that matches the method pattern.";
    }

    @Override
    public String getDescription() {
        return "This is just a demonstration of how to add annotations to a method declaration";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        MethodMatcher methodMatch = methodPattern != null ? new MethodMatcher(methodPattern) : null;

        return new JavaIsoVisitor<ExecutionContext>() {
            private final JavaTemplate deprecationTemplate = JavaTemplate.builder(this::getCursor, "@Deprecated(since=\"1.1.0\")")
                    .build();

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if ((methodMatch == null || methodMatch.matches(method.getMethodType())) && !methodDeprecated(m)) {

                    //Add Deprecated annotation to method declaration.
                    m = m.withTemplate(deprecationTemplate, m.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                }
                return m;
            }

            boolean methodDeprecated(J.MethodDeclaration m) {
                for (J.Annotation a : m.getAllAnnotations()) {
                    JavaType.FullyQualified fq = TypeUtils.asFullyQualified(a.getType());
                    if (fq != null && "java.lang.Deprecated".equals(fq.getFullyQualifiedName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
