package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Test che proteggono l'architettura a livelli (MVC + DAO) da regressioni.
 * <p>
 * Ogni test legge i sorgenti di un package e verifica che non importi classi degli
 * strati che non deve conoscere. Se in futuro qualcuno, ad esempio, istanzia un DAO
 * MySQL dentro una View o importa Swing in un Controller, il test corrispondente
 * fallisce indicando il file e la riga.
 * </p>
 * <p>
 * Le regole verificate:
 * </p>
 * <ul>
 *   <li><b>domain</b> (Model): non dipende da nessun altro strato né da librerie grafiche o JDBC;</li>
 *   <li><b>controller</b>: non conosce le View concrete, Swing/AWT, JDBC né le implementazioni MySQL;</li>
 *   <li><b>service</b>: come il controller, e in più non dipende dai controller;</li>
 *   <li><b>view</b>: non accede alla persistenza (né interfacce DAO né implementazioni) e non assembla il sistema;</li>
 *   <li><b>dao</b>: sono solo interfacce, indipendenti dall'implementazione MySQL.</li>
 * </ul>
 * <p>
 * I sorgenti vengono cercati nella cartella {@code src} rispetto alla directory di lavoro
 * (quella predefinita quando i test sono eseguiti da Eclipse). Se non sono disponibili i
 * test vengono saltati.
 * </p>
 */
class ArchitetturaTest {

    private static final String PACCHETTO_BASE = "it.unipv.poingsfw.";
    private static final Path RADICE = Paths.get("src", "it", "unipv", "poingsfw");

    /**
     * Cerca nel package indicato gli import che iniziano con uno dei prefissi vietati.
     *
     * @return l'elenco delle violazioni nel formato "file:riga: import"
     */
    private List<String> violazioni(String pacchetto, String... prefissiVietati) throws IOException {
        Path cartella = RADICE.resolve(pacchetto);
        assumeTrue(Files.isDirectory(cartella), "Sorgenti non disponibili in " + cartella.toAbsolutePath());

        List<String> trovate = new ArrayList<>();
        try (Stream<Path> percorsi = Files.walk(cartella)) {
            List<Path> sorgenti = percorsi.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path sorgente : sorgenti) {
                List<String> righe = Files.readAllLines(sorgente, StandardCharsets.UTF_8);
                for (int i = 0; i < righe.size(); i++) {
                    String riga = righe.get(i).trim();
                    if (!riga.startsWith("import ")) {
                        continue;
                    }
                    String importato = riga.substring("import ".length()).replace("static ", "").trim();
                    for (String vietato : prefissiVietati) {
                        if (importato.startsWith(vietato)) {
                            trovate.add(RADICE.relativize(sorgente) + ":" + (i + 1) + ": " + riga);
                        }
                    }
                }
            }
        }
        return trovate;
    }

    private void verifica(String regola, List<String> violazioni) {
        assertTrue(violazioni.isEmpty(), regola + "\nViolazioni:\n" + String.join("\n", violazioni));
    }

    @Test
    void ilDominioNonDipendeDaAltriStrati() throws IOException {
        verifica("Il Model (domain) non deve dipendere da controller, view, persistenza, Swing o JDBC.",
                violazioni("domain",
                        PACCHETTO_BASE + "controller.", PACCHETTO_BASE + "view.", PACCHETTO_BASE + "database.",
                        PACCHETTO_BASE + "dao.", PACCHETTO_BASE + "service.", PACCHETTO_BASE + "bootstrap.",
                        "javax.swing.", "java.awt.", "java.sql."));
    }

    @Test
    void iControllerNonConosconoViewConcreteNeImplementazioniDiPersistenza() throws IOException {
        verifica("I Controller devono dipendere solo da astrazioni: niente View concrete, Swing/AWT, JDBC o DAO MySQL.",
                violazioni("controller",
                        PACCHETTO_BASE + "view.", PACCHETTO_BASE + "database.", PACCHETTO_BASE + "bootstrap.",
                        "javax.swing.", "java.awt.", "java.sql."));
    }

    @Test
    void iServiziNonDipendonoDaInterfacciaUtenteControllerOImplementazioniDiPersistenza() throws IOException {
        verifica("I Service non devono dipendere da View, Controller, Swing/AWT, JDBC o DAO MySQL.",
                violazioni("service",
                        PACCHETTO_BASE + "view.", PACCHETTO_BASE + "controller.", PACCHETTO_BASE + "database.",
                        PACCHETTO_BASE + "bootstrap.", "javax.swing.", "java.awt.", "java.sql."));
    }

    @Test
    void leViewNonAccedonoAllaPersistenza() throws IOException {
        verifica("Le View non devono accedere alla persistenza né assemblare il sistema: passano dal Controller.",
                violazioni("view",
                        PACCHETTO_BASE + "database.", PACCHETTO_BASE + "dao.", PACCHETTO_BASE + "bootstrap.",
                        PACCHETTO_BASE + "service.", "java.sql."));
    }

    @Test
    void leInterfacceDaoSonoIndipendentiDallImplementazione() throws IOException {
        verifica("Le interfacce DAO non devono dipendere dall'implementazione MySQL né dagli strati superiori.",
                violazioni("dao",
                        PACCHETTO_BASE + "database.", PACCHETTO_BASE + "view.", PACCHETTO_BASE + "controller.",
                        PACCHETTO_BASE + "bootstrap.", "javax.swing.", "java.awt.", "java.sql."));
    }
}
