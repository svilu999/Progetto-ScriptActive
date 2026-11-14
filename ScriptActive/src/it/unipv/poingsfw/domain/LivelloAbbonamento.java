package it.unipv.poingsfw.domain;

/**
 * L'enumerazione {@code LivelloAbbonamento} definisce i possibili intervalli temporali 
 * di validità per una sottoscrizione all'interno del sistema.
 * <p>
 * L'utilizzo di un costrutto <b>Enum</b> garantisce la <i>Type Safety</i> (sicurezza dei tipi), 
 * impedendo l'assegnazione di valori arbitrari o errati a livello di compilazione. 
 * Standardizza la gestione delle durate in tutto il dominio applicativo, rendendo il 
 * codice più leggibile e meno propenso ad errori (es. errori di battitura nelle stringhe).
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public enum LivelloAbbonamento {
	MENSILE(1),
	SEMESTRALE(6),
	ANNUALE(12);

	private final int mesi;

	LivelloAbbonamento(int mesi) {
		this.mesi = mesi;
	}

	/**
	 * Durata dell'abbonamento espressa in mesi.
	 * È l'unico punto del sistema in cui questa regola è definita.
	 *
	 * @return il numero di mesi di validità del livello
	 */
	public int getMesi() {
		return mesi;
	}
}
