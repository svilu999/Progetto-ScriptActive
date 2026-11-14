package it.unipv.poingsfw.controller.vista;

/**
 * Ciò che il {@code GestorePersonale} si aspetta dal modulo di gestione del personale.
 * <p>
 * La vista non espone i propri componenti grafici (bottoni, tabella): il controller
 * registra le reazioni ai gesti dell'utente come {@link Runnable} e legge i valori
 * inseriti come tipi semplici. In questo modo la struttura dell'interfaccia resta
 * incapsulata (<i>Information Hiding</i>) e il controller non dipende da Swing.
 * </p>
 */
public interface VistaGestionePersonale {

    // --- gesti dell'utente ---

    void onAggiorna(Runnable azione);

    void onAssumi(Runnable azione);

    void onLicenziaSenzaSostituto(Runnable azione);

    void onLicenziaConSostituto(Runnable azione);

    void onCalcolaRetribuzioni(Runnable azione);

    /** L'azione viene eseguita quando l'utente seleziona un Personal Trainer in tabella. */
    void onSelezioneTrainer(Runnable azione);

    // --- lettura dell'input ---

    String getNomeInserito();

    String getCognomeInserito();

    String getEmailInserita();

    String getSpecializzazioneInserita();

    String getImportoRetribuzioneInserito();

    String getTipoRetribuzioneSelezionato();

    String getIdTrainerDaLicenziare();

    int getIndiceTrainerSelezionato();

    int getIndiceSostitutoSelezionato();

    // --- aggiornamento della schermata ---

    void mostraRigheTrainer(Object[][] righe);

    void mostraTipiRetribuzione(String[] tipiRetribuzione);

    void mostraSostituti(String[] descrizioni);

    void mostraIdTrainerDaLicenziare(String idTrainer);

    void mostraStato(String messaggio);

    void mostraSuccesso(String messaggio);

    void mostraErrore(String messaggio);

    void mostraTotaleRetribuzioni(double totale);

    void pulisciFormAssunzione();

    void pulisciFormLicenziamento();
}
