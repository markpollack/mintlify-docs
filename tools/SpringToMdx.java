///usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Converts Spring.io blog markdown files to Mintlify-compatible MDX.
 *
 * Usage:
 *   jbang SpringToMdx.java input.md -o output.mdx
 *   jbang SpringToMdx.java --input-dir /path/to/blog --mapping topic-mapping.properties --output-dir blog/
 *   jbang SpringToMdx.java --validate blog/    # validate existing MDX files
 *
 * The --output-dir should point to the blog/ directory at the repo root (NOT docs/blog/).
 */
public class SpringToMdx {

    private static final Set<String> KNOWN_HTML_TAGS = Set.of(
        "img", "iframe", "div", "span", "br", "hr", "p", "a", "b", "i", "em", "strong",
        "table", "thead", "tbody", "tr", "th", "td", "ul", "ol", "li", "h1", "h2", "h3",
        "h4", "h5", "h6", "pre", "code", "blockquote", "video", "source", "details", "summary",
        "sup", "sub", "section", "article", "nav", "header", "footer", "figure", "figcaption"
    );

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage:");
            System.err.println("  jbang SpringToMdx.java input.md -o output.mdx");
            System.err.println("  jbang SpringToMdx.java --input-dir DIR --mapping FILE --output-dir DIR");
            System.err.println("  jbang SpringToMdx.java --validate DIR");
            System.exit(1);
        }

        if (args[0].equals("--validate")) {
            validate(args.length > 1 ? args[1] : "blog/");
        } else if (args[0].equals("--input-dir")) {
            batchMode(args);
        } else {
            singleMode(args);
        }
    }

    // --- Validation ---

    static void validate(String dir) throws Exception {
        System.out.println("Validating MDX files in " + dir + " ...\n");
        Path root = Path.of(dir);
        List<Path> mdxFiles;
        try (var walk = Files.walk(root)) {
            mdxFiles = walk.filter(p -> p.toString().endsWith(".mdx")).sorted().collect(Collectors.toList());
        }

        int errors = 0;
        for (Path f : mdxFiles) {
            List<String> issues = validateFile(f);
            if (!issues.isEmpty()) {
                System.err.println("FAIL: " + f);
                for (String issue : issues) {
                    System.err.println("  " + issue);
                }
                errors++;
            }
        }

        System.out.println("\nValidated " + mdxFiles.size() + " files, " + errors + " with issues.");
        if (errors > 0) {
            System.out.println("\nRun `mintlify broken-links` for full Mintlify-level validation.");
            System.exit(1);
        } else {
            System.out.println("All files passed static checks.");
            // Also run mintlify broken-links if available
            runMintlifyValidation();
        }
    }

    static List<String> validateFile(Path file) throws Exception {
        List<String> issues = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);
        boolean inCodeBlock = false;
        int lineNum = 0;

        for (String line : lines) {
            lineNum++;
            String trimmed = line.trim();

            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (inCodeBlock) continue;

            // Check for unescaped curly braces outside code spans
            if (hasUnescapedBraces(line)) {
                issues.add("Line " + lineNum + ": unescaped curly braces: " + trimmed.substring(0, Math.min(60, trimmed.length())));
            }

            // Check for unclosed <br> (not <br /> or <br/>)
            if (line.matches(".*<br\\s*>.*")) {
                issues.add("Line " + lineNum + ": unclosed <br> tag (needs <br />)");
            }

            // Check for HTML comments outside code blocks
            if (trimmed.contains("<!--") || trimmed.contains("-->")) {
                issues.add("Line " + lineNum + ": HTML comment outside code block");
            }

            // Check for bare angle brackets that look like generic types
            if (hasBareAngleBrackets(line)) {
                issues.add("Line " + lineNum + ": possible bare angle brackets: " + trimmed.substring(0, Math.min(60, trimmed.length())));
            }
        }

        // Check for unclosed code blocks
        if (inCodeBlock) {
            issues.add("Unclosed code block (mismatched ``` fences)");
        }

        return issues;
    }

    static boolean hasUnescapedBraces(String line) {
        boolean inCodeSpan = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '`') { inCodeSpan = !inCodeSpan; continue; }
            if (!inCodeSpan && (c == '{' || c == '}')) {
                // Check if escaped
                if (i > 0 && line.charAt(i - 1) == '\\') continue;
                return true;
            }
        }
        return false;
    }

    static boolean hasBareAngleBrackets(String line) {
        boolean inCodeSpan = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '`') { inCodeSpan = !inCodeSpan; continue; }
            if (!inCodeSpan && c == '<') {
                String rest = line.substring(i);
                // Check if known HTML tag
                Matcher m = Pattern.compile("^</?([a-zA-Z][a-zA-Z0-9]*)([\\s>!/])").matcher(rest);
                if (m.find() && KNOWN_HTML_TAGS.contains(m.group(1).toLowerCase())) continue;
                if (rest.startsWith("<http") || rest.startsWith("<mailto")) continue;
                // Check if it's in a markdown image/link
                if (i > 0 && (line.charAt(i-1) == '(' || line.charAt(i-1) == '"')) continue;
                // Bare angle bracket found
                int close = rest.indexOf('>');
                if (close > 0 && close < 60) return true;
            }
        }
        return false;
    }

    static void runMintlifyValidation() {
        try {
            System.out.println("\nRunning `mintlify broken-links` ...");
            ProcessBuilder pb = new ProcessBuilder("mintlify", "broken-links");
            pb.inheritIO();
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                System.err.println("mintlify broken-links found issues (exit " + exit + ")");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Could not run mintlify broken-links: " + e.getMessage());
            System.err.println("Install with: npm i -g mintlify");
        }
    }

    // --- Single file mode ---

    static void singleMode(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        Path output = args.length >= 3 && args[1].equals("-o") ? Path.of(args[2]) : null;
        String result = convert(input, null);
        if (output != null) {
            Files.createDirectories(output.getParent());
            Files.writeString(output, result);
            System.out.println("Converted: " + output);
        } else {
            System.out.print(result);
        }
    }

    // --- Batch mode ---

    static void batchMode(String[] args) throws Exception {
        String inputDir = null, mappingFile = null, outputDir = null;
        boolean skipValidation = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input-dir" -> inputDir = args[++i];
                case "--mapping" -> mappingFile = args[++i];
                case "--output-dir" -> outputDir = args[++i];
                case "--skip-validation" -> skipValidation = true;
            }
        }
        if (inputDir == null || mappingFile == null || outputDir == null) {
            System.err.println("Batch mode requires --input-dir, --mapping, and --output-dir");
            System.exit(1);
        }

        Properties mapping = new Properties();
        try (var reader = Files.newBufferedReader(Path.of(mappingFile))) {
            mapping.load(reader);
        }

        Path inputPath = Path.of(inputDir);
        List<Path> mdFiles;
        try (var walk = Files.walk(inputPath)) {
            mdFiles = walk.filter(p -> p.toString().endsWith(".md")).collect(Collectors.toList());
        }

        int converted = 0, warnings = 0;
        for (Path mdFile : mdFiles) {
            String slug = mdFile.getFileName().toString().replace(".md", "");
            String target = mapping.getProperty(slug);
            if (target == null) {
                System.err.println("WARNING: No mapping for: " + slug);
                warnings++;
                continue;
            }
            Path outputFile = Path.of(outputDir, target + ".mdx");
            Files.createDirectories(outputFile.getParent());

            String datePart = extractDateFromPath(mdFile.toString());
            String result = convert(mdFile, datePart);
            Files.writeString(outputFile, result);
            converted++;
            System.out.println("  " + slug + " -> " + target + ".mdx");
        }

        System.out.println("\nConverted: " + converted + " files, Warnings: " + warnings);

        if (!skipValidation) {
            System.out.println();
            validate(outputDir);
        }
    }

    // --- Conversion logic ---

    static String extractDateFromPath(String path) {
        Matcher m = Pattern.compile("/(\\d{4})/(\\d{2})/").matcher(path);
        if (m.find()) {
            return m.group(1) + "-" + m.group(2) + "-01";
        }
        return null;
    }

    static String convert(Path input, String dateFromPath) throws Exception {
        String content = Files.readString(input);
        List<String> lines = new ArrayList<>(content.lines().collect(Collectors.toList()));

        // Parse frontmatter
        Map<String, String> frontmatter = new LinkedHashMap<>();
        int bodyStart = 0;
        if (!lines.isEmpty() && lines.get(0).equals("---")) {
            int end = -1;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).equals("---")) {
                    end = i;
                    break;
                }
            }
            if (end > 0) {
                for (int i = 1; i < end; i++) {
                    String line = lines.get(i);
                    int colon = line.indexOf(':');
                    if (colon > 0) {
                        String key = line.substring(0, colon).trim();
                        String val = line.substring(colon + 1).trim();
                        if (val.startsWith("\"") && val.endsWith("\"")) {
                            val = val.substring(1, val.length() - 1);
                        }
                        if (val.startsWith("'") && val.endsWith("'")) {
                            val = val.substring(1, val.length() - 1);
                        }
                        frontmatter.put(key, val);
                    }
                }
                bodyStart = end + 1;
            }
        }

        String title = frontmatter.getOrDefault("title", "Untitled");
        String sidebarTitle = generateSidebarTitle(title);
        String date = frontmatter.get("publishedAt");
        if (date == null && dateFromPath != null) {
            date = dateFromPath;
        }
        String description = extractDescription(lines, bodyStart);

        List<String> bodyLines = new ArrayList<>(lines.subList(bodyStart, lines.size()));
        List<String> processed = processBody(bodyLines, input.getFileName().toString());

        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: \"").append(escapeYamlString(title)).append("\"\n");
        sb.append("sidebarTitle: \"").append(escapeYamlString(sidebarTitle)).append("\"\n");
        if (description != null) {
            sb.append("description: \"").append(escapeYamlString(description)).append("\"\n");
        }
        if (frontmatter.containsKey("author")) {
            sb.append("author: ").append(frontmatter.get("author")).append("\n");
        }
        if (date != null) {
            sb.append("date: ").append(date).append("\n");
        }
        sb.append("---\n");
        for (String line : processed) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    static String generateSidebarTitle(String title) {
        String t = title
            .replaceFirst("^Spring AI\\s*:?\\s*", "")
            .replaceFirst("^Spring Tips:\\s*", "")
            .replaceFirst("^AI Meets Spring Petclinic:\\s*", "Petclinic: ")
            .trim();
        if (t.length() > 35) {
            int cut = t.lastIndexOf(' ', 35);
            if (cut > 15) t = t.substring(0, cut) + "...";
            else t = t.substring(0, 35) + "...";
        }
        return t;
    }

    static String extractDescription(List<String> lines, int bodyStart) {
        for (int i = bodyStart; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("<") || line.startsWith("!") || line.startsWith("```")) continue;
            if (line.length() > 20) {
                if (line.length() > 160) {
                    int cut = line.lastIndexOf(' ', 160);
                    if (cut > 80) return line.substring(0, cut) + "...";
                    return line.substring(0, 160) + "...";
                }
                return line;
            }
        }
        return null;
    }

    static List<String> processBody(List<String> lines, String filename) {
        List<String> result = new ArrayList<>();
        boolean inCodeBlock = false;

        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                result.add(line);
                continue;
            }

            if (inCodeBlock) {
                result.add(line);
                continue;
            }

            String fixed = fixMdxLine(line, filename);
            result.add(fixed);
        }

        return result;
    }

    static String fixMdxLine(String line, String filename) {
        // Fix unclosed <img> tags
        line = line.replaceAll("<img\\s([^>]*[^/])>", "<img $1 />");

        // Fix unclosed <br> tags -> <br />
        line = line.replaceAll("<br\\s*>", "<br />");

        // Fix unclosed <source> tags
        line = line.replaceAll("<source\\s([^>]*[^/])>", "<source $1 />");

        // Remove HTML comments outside code blocks
        line = line.replaceAll("<!--.*?-->", "");
        // Handle stray comment fragments
        line = line.replaceAll("\\s*-->\\s*$", "");
        line = line.replaceAll("<!--.*$", "");

        // Convert HTML style="..." to JSX style={{...}}
        line = convertStyleToJsx(line);

        // Escape curly braces outside code spans
        line = escapeCurlyBraces(line);

        // Escape angle brackets that look like generic types
        line = escapeAngleBrackets(line);

        return line;
    }

    static String convertStyleToJsx(String line) {
        // Match style="..." in HTML tags
        Matcher m = Pattern.compile("style=\"([^\"]*)\"").matcher(line);
        if (!m.find()) return line;

        StringBuffer sb = new StringBuffer();
        m.reset();
        while (m.find()) {
            String cssText = m.group(1);
            String jsxStyle = cssToJsxObject(cssText);
            m.appendReplacement(sb, Matcher.quoteReplacement("style={" + jsxStyle + "}"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static String cssToJsxObject(String css) {
        // Parse "display: block; margin: auto;" into {display: "block", margin: "auto"}
        StringBuilder sb = new StringBuilder("{");
        String[] parts = css.split(";");
        boolean first = true;
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            int colon = part.indexOf(':');
            if (colon < 0) continue;
            String prop = part.substring(0, colon).trim();
            String val = part.substring(colon + 1).trim();
            // camelCase the property: font-size -> fontSize
            prop = camelCase(prop);
            if (!first) sb.append(", ");
            sb.append(prop).append(": \"").append(val).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    static String camelCase(String cssProp) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : cssProp.toCharArray()) {
            if (c == '-') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String escapeCurlyBraces(String line) {
        if (line.trim().startsWith("<") && (line.trim().endsWith(">") || line.trim().endsWith("/>"))) return line;

        StringBuilder sb = new StringBuilder();
        boolean inCodeSpan = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '`') {
                inCodeSpan = !inCodeSpan;
                sb.append(c);
            } else if (!inCodeSpan && c == '{') {
                sb.append("\\{");
            } else if (!inCodeSpan && c == '}') {
                sb.append("\\}");
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }

    static String escapeAngleBrackets(String line) {
        StringBuilder sb = new StringBuilder();
        boolean inCodeSpan = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '`') {
                inCodeSpan = !inCodeSpan;
                sb.append(c);
                i++;
            } else if (!inCodeSpan && c == '<') {
                String rest = line.substring(i);
                boolean isHtmlTag = false;
                Matcher tagMatch = Pattern.compile("^</?([a-zA-Z][a-zA-Z0-9]*)([\\s>!/])").matcher(rest);
                if (tagMatch.find()) {
                    String tagName = tagMatch.group(1).toLowerCase();
                    if (KNOWN_HTML_TAGS.contains(tagName)) {
                        isHtmlTag = true;
                    }
                }
                if (rest.startsWith("<http") || rest.startsWith("<mailto")) {
                    isHtmlTag = true;
                }
                if (isHtmlTag) {
                    sb.append(c);
                } else {
                    int closeIdx = rest.indexOf('>');
                    if (closeIdx > 0 && closeIdx < 60) {
                        String inner = rest.substring(0, closeIdx + 1);
                        sb.append('`').append(inner).append('`');
                        i += closeIdx + 1;
                        continue;
                    } else {
                        sb.append("&lt;");
                    }
                }
                i++;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    static String escapeYamlString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
