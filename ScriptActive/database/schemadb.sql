DROP DATABASE IF EXISTS scriptactive_db;

-- ==============================================================================
-- Creazione del Database
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS scriptactive_db;
USE scriptactive_db;

-- ==============================================================================
-- 1. TABELLE INDIPENDENTI
-- ==============================================================================

CREATE TABLE Sede (
    ID_Sede INT AUTO_INCREMENT PRIMARY KEY,
    NomeSede VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE Utente (
    ID_Utente INT AUTO_INCREMENT PRIMARY KEY,
    CodiceFiscale CHAR(16) UNIQUE NOT NULL,
    Nome VARCHAR(50) NOT NULL,
    Cognome VARCHAR(50) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Ruolo ENUM('Cliente', 'PersonalTrainer', 'Direttore') NOT NULL,
    Stato ENUM('Attivo', 'Sospeso', 'Inattivo') DEFAULT 'Attivo'
) ENGINE=InnoDB;

-- ==============================================================================
-- 2. TABELLE FIGLIE DELLA GERARCHIA IS-A
-- ==============================================================================

CREATE TABLE Direttore (
    ID_Direttore INT PRIMARY KEY,
    CodiceAutorizzazione VARCHAR(50) UNIQUE NOT NULL,
    FOREIGN KEY (ID_Direttore) REFERENCES Utente(ID_Utente) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE PersonalTrainer (
    ID_Trainer INT PRIMARY KEY,
    Specializzazione VARCHAR(100),
    TipoContratto VARCHAR(50),

    -- ==========================================================================
    -- Gestione Contratti del Personale
    -- ==========================================================================
    StatoContratto VARCHAR(50) NOT NULL DEFAULT 'ATTIVO',
    Attivo BOOLEAN NOT NULL DEFAULT TRUE,
    TipoRetribuzione VARCHAR(50) NOT NULL DEFAULT 'FISSA_MENSILE',
    StipendioMensile DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CompensoPerLezione DECIMAL(10,2) NULL,

    ID_Direttore INT NOT NULL,

    FOREIGN KEY (ID_Trainer) REFERENCES Utente(ID_Utente) ON DELETE CASCADE,
    FOREIGN KEY (ID_Direttore) REFERENCES Direttore(ID_Direttore) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Cliente (
    ID_Cliente INT PRIMARY KEY,
    ID_Sede INT NOT NULL, -- <--- Sede scelta dal cliente in fase di registrazione
    
    FOREIGN KEY (ID_Cliente) REFERENCES Utente(ID_Utente) ON DELETE CASCADE,
    FOREIGN KEY (ID_Sede) REFERENCES Sede(ID_Sede) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ==============================================================================
-- 3. TABELLE DIPENDENTI
-- ==============================================================================

CREATE TABLE SpesaFissa (
    ID_Spesa INT AUTO_INCREMENT PRIMARY KEY,
    Descrizione VARCHAR(255) NOT NULL,
    Importo DECIMAL(10,2) NOT NULL,
    Data DATE NOT NULL,
    ID_Sede INT NOT NULL,

    FOREIGN KEY (ID_Sede) REFERENCES Sede(ID_Sede) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Corso (
    ID_Corso INT AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(100) NOT NULL,
    DataOra DATETIME NOT NULL,
    CapienzaMassima INT NOT NULL CHECK (CapienzaMassima > 0),
    PostiDisponibili INT NOT NULL CHECK (PostiDisponibili >= 0),
    Stato ENUM('Pianificato', 'InCorso', 'Completato', 'Annullato') DEFAULT 'Pianificato',

    ID_Sede INT NOT NULL,
    ID_Trainer INT NOT NULL,
    ID_Direttore INT NOT NULL,

    FOREIGN KEY (ID_Sede) REFERENCES Sede(ID_Sede) ON DELETE CASCADE,
    FOREIGN KEY (ID_Trainer) REFERENCES PersonalTrainer(ID_Trainer) ON DELETE RESTRICT,
    FOREIGN KEY (ID_Direttore) REFERENCES Direttore(ID_Direttore) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Abbonamento (
    ID_Abbonamento INT AUTO_INCREMENT PRIMARY KEY,
    Tipo VARCHAR(50) NOT NULL,    -- <--- Allineato al Java: conterrà 'BASE' o 'PREMIUM'
    Livello VARCHAR(50),          -- <--- Allineato al Java: conterrà 'MENSILE', 'SEMESTRALE' o 'ANNUALE'
    DataScadenza DATE NOT NULL,
    RinnovoAutomatico BOOLEAN DEFAULT FALSE,
    IBAN VARCHAR(34),
    ID_Cliente INT UNIQUE NOT NULL,

    FOREIGN KEY (ID_Cliente) REFERENCES Cliente(ID_Cliente) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Pagamento (
    ID_Pagamento INT AUTO_INCREMENT PRIMARY KEY,
    Importo DECIMAL(10,2) NOT NULL CHECK (Importo > 0),
    DataTransazione DATETIME NOT NULL,
    Esito ENUM('Successo', 'Fallito', 'InElaborazione') NOT NULL,
    ID_Abbonamento INT NOT NULL,

    FOREIGN KEY (ID_Abbonamento) REFERENCES Abbonamento(ID_Abbonamento) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Prenotazione (
    ID_Prenotazione INT AUTO_INCREMENT PRIMARY KEY,
    DataCreazione DATETIME DEFAULT CURRENT_TIMESTAMP,
    Stato ENUM('Confermata', 'Annullata', 'InListaAttesa') DEFAULT 'Confermata',
    ID_Cliente INT NOT NULL,
    ID_Corso INT NOT NULL,

    FOREIGN KEY (ID_Cliente) REFERENCES Cliente(ID_Cliente) ON DELETE CASCADE,
    FOREIGN KEY (ID_Corso) REFERENCES Corso(ID_Corso) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE SessioneAllenamento (
    ID_Sessione INT AUTO_INCREMENT PRIMARY KEY,
    Data DATE NOT NULL,
    ID_Cliente INT NOT NULL,

    FOREIGN KEY (ID_Cliente) REFERENCES Cliente(ID_Cliente) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE EsercizioRegistrato (
    ID_Esercizio INT AUTO_INCREMENT PRIMARY KEY,
    GruppoMuscolare VARCHAR(50) NOT NULL,
    Macchinario VARCHAR(100),
    Serie INT NOT NULL CHECK (Serie > 0),
    Ripetizioni INT NOT NULL CHECK (Ripetizioni > 0),
    Carico DECIMAL(5,2) NOT NULL CHECK (Carico >= 0),
    ID_Sessione INT NOT NULL,

    FOREIGN KEY (ID_Sessione) REFERENCES SessioneAllenamento(ID_Sessione) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================================
-- 4. POPOLAMENTO: INSERIMENTO SEDI 
-- ==============================================================================
INSERT INTO Sede (NomeSede) VALUES
('Palestra Centrale Milano'),
('Palestra Roma Nord');

-- ==============================================================================
-- 5. POPOLAMENTO: INSERIMENTO UTENTI (Tabella Padre)
-- ==============================================================================
-- NOTA PER IL TEST LOGIN: ID generati automaticamente (1=Anna, 2=Marco, 3=Mario, 4=Lorenzo)
-- La password valida per il form di login sarà 'hash_finto_per_ora'
INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo, Stato) VALUES
('DRTXYZ70M01F205A', 'Anna', 'Bianchi', 'direttore@scriptactive.it', 'hash_finto_per_ora', 'Direttore', 'Attivo'),
('PTRXYZ80M01F205B', 'Marco', 'Verdi', 'trainer@scriptactive.it', 'hash_finto_per_ora', 'PersonalTrainer', 'Attivo'),
('RSSMRA80A01H501Z', 'Mario', 'Rossi', 'mario.rossi@email.it', 'hash_finto_per_ora', 'Cliente', 'Attivo'),
('VRNLRN99M21F205W', 'Lorenzo', 'Varano', 'lorenzo@studenti.unipv.it', 'hash_finto_per_ora', 'Cliente', 'Attivo');

-- ==============================================================================
-- 6. POPOLAMENTO: INSERIMENTO GERARCHIA UTENTI (Tabelle Figlie)
-- ==============================================================================

-- Direttore (Anna Bianchi, ID_Utente = 1)
INSERT INTO Direttore (ID_Direttore, CodiceAutorizzazione)
VALUES (1, 'DIR-001');

-- Personal Trainer (Marco Verdi, ID_Utente = 2, ID_Direttore = 1)
INSERT INTO PersonalTrainer (ID_Trainer, Specializzazione, TipoContratto, StatoContratto, Attivo, TipoRetribuzione, StipendioMensile, ID_Direttore)
VALUES (2, 'Pesistica e Forza', 'Indeterminato', 'ATTIVO', TRUE, 'FISSA_MENSILE', 1500.00, 1);

-- Clienti (Mario Rossi, ID_Utente = 3 abbinato a Milano [Sede 1], Lorenzo Varano, ID_Utente = 4 abbinato a Roma [Sede 2])
INSERT INTO Cliente (ID_Cliente, ID_Sede)
VALUES 
(3, 1), 
(4, 2);

-- ==============================================================================
-- 7. POPOLAMENTO: INSERIMENTO ABBONAMENTI (Sincronizzato con Enum Java)
-- ==============================================================================
-- Mario (ID_Cliente = 3) -> Fascia PREMIUM, durata ANNUALE
INSERT INTO Abbonamento (Tipo, Livello, DataScadenza, RinnovoAutomatico, ID_Cliente)
VALUES ('PREMIUM', 'ANNUALE', '2026-12-31', TRUE, 3);

-- Lorenzo (ID_Cliente = 4) -> Fascia BASE, durata MENSILE
INSERT INTO Abbonamento (Tipo, Livello, DataScadenza, RinnovoAutomatico, ID_Cliente)
VALUES ('BASE', 'MENSILE', '2026-07-31', FALSE, 4);

-- ==============================================================================
-- 8. POPOLAMENTO: INSERIMENTO CORSI (Richiede Sede, Trainer e Direttore)
-- ==============================================================================
INSERT INTO Corso (Nome, DataOra, CapienzaMassima, PostiDisponibili, Stato, ID_Sede, ID_Trainer, ID_Direttore)
VALUES
('Corso di Funzionale', '2026-07-01 18:00:00', 15, 15, 'Pianificato', 1, 2, 1),
('Corso di Pilates', '2026-07-02 10:00:00', 20, 20, 'Pianificato', 2, 2, 1);

-- ==============================================================================
-- 9. POPOLAMENTO: INSERIMENTO STORICO ALLENAMENTI (Per testare la StoricoAllenamentiView)
-- ==============================================================================
-- Creiamo una sessione per Mario Rossi (ID_Cliente = 3)
INSERT INTO SessioneAllenamento (Data, ID_Cliente)
VALUES ('2026-06-10', 3);

-- Inseriamo gli esercizi collegati a quella sessione (ID_Sessione = 1)
INSERT INTO EsercizioRegistrato (GruppoMuscolare, Macchinario, Serie, Ripetizioni, Carico, ID_Sessione)
VALUES
('Petto', 'Panca Piana', 4, 10, 60.00, 1),
('Dorso', 'Lat Machine', 3, 12, 50.00, 1),
('Gambe', 'Pressa 45', 4, 8, 120.00, 1);