import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.expr.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Genera il sorgente PlantUML del diagramma delle classi leggendo i sorgenti Java,
 * così il diagramma resta allineato al codice.
 *
 * Richiede la libreria JavaParser (javaparser-core). Dalla cartella ScriptActive:
 *
 *   java -cp javaparser-core.jar "Documenti/07. Diagramma delle Classi/GeneraDiagrammaClassi.java" \
 *        src "Documenti/07. Diagramma delle Classi/Diagramma delle Classi_code.puml"
 *
 * Aggiungendo "test" come terzo argomento genera il diagramma delle classi di test,
 * in cui le classi dell'applicazione usate dai test compaiono senza membri.
 * Le immagini si ottengono poi con PlantUML (opzione -tsvg).
 */
public class GeneraDiagrammaClassi {
    static final String BASE = "it.unipv.poingsfw.";
    static final List<String> ORDINE = List.of("bootstrap", "view", "controller", "controller.vista", "controller.mapper",
            "service", "dao", "database", "util", "domain", "dto", "strategy", "exceptions",
            "test.unit", "test.integration", "test.system", "test.fake");

    record Tipo(String pkg, String nome, TypeDeclaration<?> decl, String outer) {}
    static Map<String, Tipo> tipi = new LinkedHashMap<>();           // nome diagramma -> tipo
    static Map<String, List<String>> perPacchetto = new TreeMap<>();
    static Set<String> relazioni = new TreeSet<>();
    static boolean usaException = false, usaRuntime = false;

    public static void main(String[] a) throws Exception {
        Path src = Paths.get(a[0]);
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        List<Path> files;
        try (Stream<Path> s = Files.walk(src)) { files = s.filter(p -> p.toString().endsWith(".java")).sorted().toList(); }
        for (Path f : files) {
            CompilationUnit cu = StaticJavaParser.parse(f);
            String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
            if (!pkg.startsWith(BASE)) continue;
            String p = pkg.substring(BASE.length());
            for (TypeDeclaration<?> t : cu.getTypes()) registra(p, t, null);
        }
        boolean soloTest = a.length > 2 && a[2].equals("test");
        // prima passata: popola tutte le relazioni
        for (Tipo x : new ArrayList<>(tipi.values())) classe(x);
        Set<String> inclusi = new LinkedHashSet<>();
        for (Tipo x : tipi.values()) if (x.pkg().startsWith("test") == soloTest) inclusi.add(x.nome());
        Set<String> filtrate = new TreeSet<>();
        Set<String> esterni = new TreeSet<>();
        for (String rel : relazioni) {
            String[] parole = rel.replace("\"", " ").split("\\s+");
            List<String> nomi = new ArrayList<>();
            for (String w : parole) if (tipi.containsKey(w) || w.equals("Exception") || w.equals("RuntimeException")) nomi.add(w);
            boolean tocca = nomi.stream().anyMatch(inclusi::contains);
            boolean tutti = nomi.stream().allMatch(n -> inclusi.contains(n) || n.equals("Exception") || n.equals("RuntimeException"));
            if (soloTest ? tocca : tutti) { filtrate.add(rel); for (String n : nomi) if (!inclusi.contains(n) && tipi.containsKey(n)) esterni.add(n); }
        }
        relazioni = filtrate;
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\ntitle " + (soloTest ? "Diagramma delle Classi di Test" : "Diagramma delle Classi") + "\ntop to bottom direction\nskinparam classAttributeIconSize 0\nskinparam packageStyle rectangle\nhide circle\n\n");
        List<String> pacchetti = new ArrayList<>(ORDINE);
        for (String p : perPacchetto.keySet()) if (!pacchetti.contains(p)) pacchetti.add(p);
        StringBuilder corpo = new StringBuilder();
        for (String p : pacchetti) {
            List<String> nomi = perPacchetto.get(p);
            if (nomi == null || p.startsWith("test") != soloTest) continue;
            Collections.sort(nomi);
            corpo.append("package \"").append(p).append("\" {\n");
            for (String n : nomi) corpo.append(classe(tipi.get(n))).append("\n");
            corpo.setLength(corpo.length() - 1);
            corpo.append("}\n\n");
        }
        sb.append(corpo);
        if (soloTest && !esterni.isEmpty()) {
            // classi dell'applicazione usate dai test: mostrate senza membri, raggruppate per package
            Map<String, List<String>> perPkg = new TreeMap<>();
            for (String n : esterni) perPkg.computeIfAbsent(tipi.get(n).pkg(), k -> new ArrayList<>()).add(n);
            for (var e : perPkg.entrySet()) {
                sb.append("package \"").append(e.getKey()).append("\" #EEEEEE {\n");
                for (String n : e.getValue()) {
                    TypeDeclaration<?> t = tipi.get(n).decl();
                    boolean interf = t instanceof ClassOrInterfaceDeclaration c && c.isInterface();
                    sb.append(interf ? "interface " : t instanceof EnumDeclaration ? "enum " : "class ").append(n).append("\n");
                }
                sb.append("}\n\n");
            }
            sb.append("hide empty members\n\n");
        }
        if (usaException || usaRuntime) {
            sb.append("package \"java.lang\" {\nclass Exception {\n}\n");
            if (usaRuntime) sb.append("\nclass RuntimeException {\n}\n");
            sb.append("}\n\n");
            if (usaRuntime) relazioni.add("Exception <|-- RuntimeException");
        }
        for (String r : relazioni) sb.append(r).append("\n");
        sb.append("@enduml\n");
        Files.writeString(Paths.get(a[1]), sb.toString());
        System.out.println("classi: " + tipi.size() + ", relazioni: " + relazioni.size());
    }

    static void registra(String pkg, TypeDeclaration<?> t, String outer) {
        String nome = outer == null ? t.getNameAsString() : outer + "_" + t.getNameAsString();
        tipi.put(nome, new Tipo(pkg, nome, t, outer));
        perPacchetto.computeIfAbsent(pkg, k -> new ArrayList<>()).add(nome);
        for (BodyDeclaration<?> m : t.getMembers())
            if (m instanceof TypeDeclaration<?> in) { registra(pkg, in, nome); relazioni.add(nome + " \"1\" *-- \"1\" " + nome + "_" + in.getNameAsString() + " : inner"); }
    }

    static String vis(com.github.javaparser.ast.nodeTypes.NodeWithModifiers<?> n, boolean inInterfaccia) {
        if (n.hasModifier(Modifier.Keyword.PUBLIC) || inInterfaccia) return "+";
        if (n.hasModifier(Modifier.Keyword.PRIVATE)) return "-";
        if (n.hasModifier(Modifier.Keyword.PROTECTED)) return "#";
        return "~";
    }
    static String tipo(Type t) { return t.asString().replaceAll("\\bjava\\.util\\.", "").replaceAll("\\bjava\\.sql\\.", "").replaceAll("\\bit\\.unipv\\.poingsfw\\.[a-z.]+\\.", ""); }

    static String classe(Tipo x) {
        TypeDeclaration<?> t = x.decl();
        StringBuilder sb = new StringBuilder();
        boolean interfaccia = t instanceof ClassOrInterfaceDeclaration c && c.isInterface();
        String kw = interfaccia ? "interface" : t instanceof EnumDeclaration ? "enum"
                : t instanceof RecordDeclaration ? "class"
                : (t.hasModifier(Modifier.Keyword.ABSTRACT) ? "abstract class" : "class");
        sb.append(kw).append(" ").append(x.nome());
        if (t instanceof RecordDeclaration) sb.append(" <<record>>");
        sb.append(" {\n");
        if (t instanceof EnumDeclaration e) for (EnumConstantDeclaration c : e.getEntries()) sb.append("  ").append(c.getNameAsString()).append("\n");
        if (t instanceof RecordDeclaration r) for (Parameter p : r.getParameters()) {
            sb.append("  - {readOnly} ").append(p.getNameAsString()).append(" : ").append(tipo(p.getType())).append("\n");
            associa(x.nome(), p.getType(), p.getNameAsString());
        }
        List<String> campi = new ArrayList<>(), metodi = new ArrayList<>();
        for (BodyDeclaration<?> m : t.getMembers()) {
            if (m instanceof FieldDeclaration f) {
                for (VariableDeclarator v : f.getVariables()) {
                    String s = "  " + vis(f, false) + " " + (f.isStatic() ? "{static} " : "") + (f.isFinal() ? "{readOnly} " : "") + v.getNameAsString() + " : " + tipo(v.getType());
                    campi.add(s);
                    if (!f.isStatic()) associa(x.nome(), v.getType(), v.getNameAsString());
                }
            } else if (m instanceof ConstructorDeclaration c) {
                metodi.add("  " + vis(c, false) + " " + c.getNameAsString() + "(" + parametri(c.getParameters()) + ")");
                lancia(x.nome(), c.getThrownExceptions());
            } else if (m instanceof MethodDeclaration md) {
                boolean astratto = md.isAbstract() && !interfaccia;
                metodi.add("  " + vis(md, interfaccia) + " " + (md.isStatic() ? "{static} " : "") + (astratto ? "{abstract} " : "")
                        + md.getNameAsString() + "(" + parametri(md.getParameters()) + ") : " + tipo(md.getType()));
                lancia(x.nome(), md.getThrownExceptions());
            }
        }
        for (String s : campi) sb.append(s).append("\n");
        if ((!campi.isEmpty() || t instanceof EnumDeclaration || t instanceof RecordDeclaration) && !metodi.isEmpty()) sb.append("  --\n");
        for (String s : metodi) sb.append(s).append("\n");
        sb.append("}\n");

        // generalizzazione e realizzazione
        if (t instanceof ClassOrInterfaceDeclaration c) {
            for (ClassOrInterfaceType e : c.getExtendedTypes()) eredita(e.getNameAsString(), x.nome(), interfaccia ? "<|--" : "<|--");
            for (ClassOrInterfaceType i : c.getImplementedTypes()) eredita(i.getNameAsString(), x.nome(), "<|..");
        } else if (t instanceof EnumDeclaration e) {
            for (ClassOrInterfaceType i : e.getImplementedTypes()) eredita(i.getNameAsString(), x.nome(), "<|..");
        } else if (t instanceof RecordDeclaration r) {
            for (ClassOrInterfaceType i : r.getImplementedTypes()) eredita(i.getNameAsString(), x.nome(), "<|..");
        }
        return sb.toString();
    }
    static void eredita(String sup, String sub, String freccia) {
        if (sup.equals("Exception")) usaException = true;
        if (sup.equals("RuntimeException")) { usaException = true; usaRuntime = true; }
        if (tipi.containsKey(sup) || sup.equals("Exception") || sup.equals("RuntimeException")) relazioni.add(sup + " " + freccia + " " + sub);
    }
    static String parametri(NodeList<Parameter> ps) {
        return ps.stream().map(p -> p.getNameAsString() + " : " + tipo(p.getType()) + (p.isVarArgs() ? "..." : "")).collect(Collectors.joining(", "));
    }
    static void lancia(String da, NodeList<ReferenceType> ecc) {
        for (ReferenceType r : ecc) { String n = r.asString(); n = n.substring(n.lastIndexOf('.') + 1); if (tipi.containsKey(n)) relazioni.add(da + " ..> " + n + " : throws"); }
    }
    static void associa(String da, Type t, String campo) {
        String s = t.asString();
        boolean molti = t.isArrayType() || s.matches("^(java\\.util\\.)?(List|Set|Collection|Map|ArrayList)<.*");
        Set<String> trovati = new LinkedHashSet<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[A-Z][A-Za-z0-9_]*").matcher(s);
        while (m.find()) if (tipi.containsKey(m.group()) && !m.group().equals(da)) trovati.add(m.group());
        for (String a : trovati) relazioni.add(da + " \"1\" --> \"" + (molti ? "0..*" : "1") + "\" " + a + " : " + campo);
    }
}
