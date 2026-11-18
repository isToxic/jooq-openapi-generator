package is.toxic;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.TableDefinition;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class SwaggerJavaGenerator extends JavaGenerator {

    @Override
    protected void generatePojo(TableDefinition table) {
        super.generatePojo(table);

        if (generatePojos()) {
            try {
                String className = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.POJO);
                String filePath = getFile(table, GeneratorStrategy.Mode.POJO).getAbsolutePath();
                Path path = Paths.get(filePath);
                String content = new String(Files.readAllBytes(path));

                content = addSwaggerImport(content);
                content = addBuilder(content, className);
                content = addNoArgsConstructor(content, className);
                content = addSchema(content, className, table);

                List<ColumnDefinition> columns = table.getColumns();
                JavaWriter writer = newJavaWriter(getFile(table, GeneratorStrategy.Mode.POJO));
                try {
                    for (ColumnDefinition column : columns) {
                        String fieldName = getStrategy().getJavaMemberName(column, GeneratorStrategy.Mode.POJO);
                        String fieldType = getClassNameFromQualifiedType(getJavaType(column.getType(), writer, GeneratorStrategy.Mode.POJO));
                        String fieldPattern = "(\\s*)((private|protected)\\s+)?final\\s+" +
                                              Pattern.quote(fieldType) + "\\s+" +
                                              Pattern.quote(fieldName) + "\\s*;";

                        if (content.matches("(?s).*" + fieldPattern + ".*")) {

                            String description = column.getComment() != null ?
                                    escapeSchemaDescription(column.getComment()) :
                                    escapeSchemaDescription(fieldName + " field");
                            String required = String.valueOf(!column.getType().isNullable());

                            content = content.replaceAll(
                                    fieldPattern,
                                    "$1@Schema(description = \"" + description + "\", required = " + required + ")\n$1$2 " + fieldType + " " + fieldName + ";"
                            );
                        }
                    }
                } finally {
                    writer.close();
                }

                Files.writeString(path, content, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add Swagger annotations", e);
            }
        }
    }

    public static String getClassNameFromQualifiedType(String qualifiedType) {
        if (qualifiedType == null) {
            return null;
        }
        if (qualifiedType.isEmpty()) {
            return "";
        }

        int lastDotIndex = qualifiedType.lastIndexOf('.');
        return lastDotIndex == -1
                ? qualifiedType
                : qualifiedType.substring(lastDotIndex + 1);
    }

    private String addSwaggerImport(String content){
        return content.contains("import io.swagger.v3.oas.annotations.media.Schema;")
                ? content
                : content.replaceFirst(
                "(package .+;\\s+)",
                "$1\nimport io.swagger.v3.oas.annotations.media.Schema;\n"
        );
    }

    private String addBuilder(String content, String className){
        if (content.contains("@Builder")) {
            return content;
        }
        String pattern = "(public (?:final )?class " + Pattern.quote(className) + ")";
        return content.replaceFirst(
                pattern,
                "@lombok.Builder(toBuilder = true)\n$1"
        );
    }

    private String addNoArgsConstructor(String content, String className){
        if (content.contains("@NoArgsConstructor")) {
            return content;
        }
        String pattern = "(public (?:final )?class " + Pattern.quote(className) + ")";
        return content.replaceFirst(
                pattern,
                "@lombok.NoArgsConstructor\n$1"
        );
    }

    private String addSchema(String content, String className, TableDefinition table){
        if (content.contains("@Schema")) {
            return content;
        }
        String description = table.getComment() != null
                ? escapeSchemaDescription(table.getComment())
                : className;
        String pattern = "(public class " + Pattern.quote(className) + ")";
        return content.replaceFirst(
                pattern,
                "@Schema(description = \"" + description + "\")\n$1"
        );
    }

    /**
     * Escapes special characters in schema description strings.
     * Prevents syntax errors in generated Java code when comments contain
     * quotes, backslashes, or other special characters.
     *
     * @param input the raw description string
     * @return the escaped string safe for use inside @Schema(description = "...")
     */
    public static String escapeSchemaDescription(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\\", "\\\\")   // escape backslashes first
                .replace("\"", "\\\"")   // escape double quotes
                .replace("\n", "\\n")    // escape newlines
                .replace("\r", "\\r")    // escape carriage returns
                .replace("\t", "\\t")    // escape tabs
                .replace("\f", "\\f")    // escape form feed
                .replace("\b", "\\b")    // escape backspace
                .replace("\u0000", "\\u0000"); // escape null bytes
    }

}
