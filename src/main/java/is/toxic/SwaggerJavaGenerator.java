package is.toxic;

import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.TableDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.UnaryOperator;
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
                JavaWriter tempWriter = newJavaWriter(getFile(table, GeneratorStrategy.Mode.POJO));

                content =  UnaryOperator.<String>identity()
                        .andThen(this::addSwaggerImport)
                        .andThen(s-> addBuilder(s, className))
                        .andThen(s-> addNoArgsConstructor(s, className))
                        .andThen(s-> addSchema(s,className, table))
                        .apply(content);

                List<ColumnDefinition> columns = table.getColumns();
                for (ColumnDefinition column : columns) {
                    String fieldName = getStrategy().getJavaMemberName(column, GeneratorStrategy.Mode.POJO);
                    String fieldType = getClassNameFromQualifiedType(getJavaType(column.getType(), tempWriter, GeneratorStrategy.Mode.POJO));
                    String fieldPattern = "(\\s*)((private|protected)\\s+)?final\\s+" +
                                          Pattern.quote(fieldType) + "\\s+" +
                                          Pattern.quote(fieldName) + "\\s*;";

                    if (content.matches("(?s).*" + fieldPattern + ".*")) {

                        String description = column.getComment() != null ?
                                column.getComment() :
                                fieldName + " field";
                        String required = String.valueOf(!column.getType().isNullable());

                        content = content.replaceAll(
                                fieldPattern,
                                "$1@Schema(description = \"" + description + "\", required = " + required + ")\n$1$2 " + fieldType + " " + fieldName + ";"
                        );
                        tempWriter.print(content);
                    }
                }

                tempWriter.close();

                Files.write(path, content.getBytes());
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
        return content.contains("@Builder")
                ? content
                : content.replaceFirst(
                "(public (?:final )?class " + className + ")",
                "@lombok.Builder(toBuilder = true)\n$1"
        );
    }

    private String addNoArgsConstructor(String content, String className){
        return content.contains("@NoArgsConstructor")
                ? content
                : content.replaceFirst(
                "(public (?:final )?class " + className + ")",
        "@lombok.NoArgsConstructor\n$1"
        );
    }

    private String addSchema(String content, String className, TableDefinition table){
        return content.contains("@Schema")
                ? content
                : content.replaceFirst(
                "(public class " + className + ")",
                "@Schema(description = \"" + table.getComment() + " \")\n$1"
        );
    }


}
