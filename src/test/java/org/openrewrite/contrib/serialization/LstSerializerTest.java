package org.openrewrite.contrib.serialization;

import org.junit.Test;
import org.openrewrite.SourceFile;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.internal.JavaTypeCache;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LstSerializerTest {

  private final LstSerializer serializer = new LstSerializer();

  private final JavaParser.Builder<? extends JavaParser, ?> javaParserBuilder = JavaParser
    .fromJavaVersion()
    .typeCache(new JavaTypeCache())
    .logCompilationWarningsAndErrors(true)
    .classpath("guava");

    @Test
    public void serializeAndDeserialize() {

        List<SourceFile> sources = javaParserBuilder.build().parse(
          """
            import com.google.common.collect.Lists;
            import java.util.List;

            public class Test {
                public void test() {
                    List<String> lst = Lists.newArrayList("a", "b", "c");
                }
            }
          """
        ).toList();


        byte[] serialized = serializer.serialize(sources);
        List<SourceFile> deserialized = serializer.deserialize(serialized);
        assertEquals(sources, deserialized);
    }


}
