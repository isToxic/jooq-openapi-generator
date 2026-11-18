package is.toxic;

import org.jooq.codegen.JavaGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SwaggerJavaGenerator}.
 */
class SwaggerJavaGeneratorTest {

    private final SwaggerJavaGenerator generator = new SwaggerJavaGenerator();

    @Nested
    @DisplayName("getClassNameFromQualifiedType tests")
    class GetClassNameFromQualifiedTypeTests {

        @Test
        @DisplayName("should extract simple name from fully qualified type")
        void shouldExtractSimpleNameFromFullyQualifiedType() {
            assertEquals("String", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.String"));
            assertEquals("Long", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Long"));
            assertEquals("Integer", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Integer"));
            assertEquals("BigDecimal", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.math.BigDecimal"));
            assertEquals("LocalDate", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.time.LocalDate"));
            assertEquals("Timestamp", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.sql.Timestamp"));
        }

        @Test
        @DisplayName("should return input unchanged when no package")
        void shouldReturnInputUnchangedWhenNoPackage() {
            assertEquals("String", SwaggerJavaGenerator.getClassNameFromQualifiedType("String"));
            assertEquals("Long", SwaggerJavaGenerator.getClassNameFromQualifiedType("Long"));
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNullInput() {
            assertNull(SwaggerJavaGenerator.getClassNameFromQualifiedType(null));
        }

        @Test
        @DisplayName("should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            assertEquals("", SwaggerJavaGenerator.getClassNameFromQualifiedType(""));
        }

        @Test
        @DisplayName("should handle complex qualified types")
        void shouldHandleComplexQualifiedTypes() {
            assertEquals("UUID", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.util.UUID"));
            assertEquals("Boolean", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Boolean"));
            assertEquals("Byte", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Byte"));
            assertEquals("Short", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Short"));
            assertEquals("Float", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Float"));
            assertEquals("Double", SwaggerJavaGenerator.getClassNameFromQualifiedType("java.lang.Double"));
        }

        @Test
        @DisplayName("should handle generics type - returns class name after last dot (known limitation)")
        void shouldHandleGenericsType() {
            // NOTE: Current implementation extracts only the part after the last dot.
            // For "java.util.List<java.lang.Integer>" it returns "Integer>" (with closing bracket).
            // This is a known limitation (P2 in improvement plan) to be fixed in Phase 3.
            String result = SwaggerJavaGenerator.getClassNameFromQualifiedType("java.util.List<java.lang.Integer>");
            assertNotNull(result);
            // Current behavior: returns "Integer>" (part after last dot, including trailing >)
            assertEquals("Integer>", result);
        }
    }

    @Nested
    @DisplayName("Inheritance tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend JavaGenerator")
        void shouldExtendJavaGenerator() {
            assertInstanceOf(JavaGenerator.class, generator);
        }
    }

    @Nested
    @DisplayName("escapeSchemaDescription tests")
    class EscapeSchemaDescriptionTests {

        @Test
        @DisplayName("should return empty string for null input")
        void shouldReturnEmptyForNull() {
            assertEquals("", SwaggerJavaGenerator.escapeSchemaDescription(null));
        }

        @Test
        @DisplayName("should return unchanged text without special characters")
        void shouldReturnUnchangedForSafeText() {
            assertEquals("User name", SwaggerJavaGenerator.escapeSchemaDescription("User name"));
            assertEquals("Description text", SwaggerJavaGenerator.escapeSchemaDescription("Description text"));
        }

        @Test
        @DisplayName("should escape double quotes")
        void shouldEscapeDoubleQuotes() {
            assertEquals("He said \\\"hello\\\"", SwaggerJavaGenerator.escapeSchemaDescription("He said \"hello\""));
        }

        @Test
        @DisplayName("should escape backslashes")
        void shouldEscapeBackslashes() {
            assertEquals("C:\\\\path", SwaggerJavaGenerator.escapeSchemaDescription("C:\\path"));
        }

        @Test
        @DisplayName("should escape newlines")
        void shouldEscapeNewlines() {
            assertEquals("line1\\nline2", SwaggerJavaGenerator.escapeSchemaDescription("line1\nline2"));
        }

        @Test
        @DisplayName("should escape carriage returns")
        void shouldEscapeCarriageReturns() {
            assertEquals("line1\\rline2", SwaggerJavaGenerator.escapeSchemaDescription("line1\rline2"));
        }

        @Test
        @DisplayName("should escape tabs")
        void shouldEscapeTabs() {
            assertEquals("col1\\tcol2", SwaggerJavaGenerator.escapeSchemaDescription("col1\tcol2"));
        }

        @Test
        @DisplayName("should handle multiple special characters together")
        void shouldHandleMultipleSpecialChars() {
            String input = "Description: \"User's field\"\nNew line\ttab";
            String expected = "Description: \\\"User's field\\\"\\nNew line\\ttab";
            assertEquals(expected, SwaggerJavaGenerator.escapeSchemaDescription(input));
        }

        @Test
        @DisplayName("should escape form feed")
        void shouldEscapeFormFeed() {
            assertEquals("page1\\fpage2", SwaggerJavaGenerator.escapeSchemaDescription("page1\fpage2"));
        }

        @Test
        @DisplayName("should escape backspace")
        void shouldEscapeBackspace() {
            assertEquals("col1\\bcol2", SwaggerJavaGenerator.escapeSchemaDescription("col1\bcol2"));
        }

        @Test
        @DisplayName("should escape null bytes")
        void shouldEscapeNullBytes() {
            String input = "start\u0000end";
            String expected = "start\\u0000end";
            assertEquals(expected, SwaggerJavaGenerator.escapeSchemaDescription(input));
        }
    }
}
