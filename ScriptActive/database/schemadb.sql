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
    --  Gestione Contratti del Personale
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
    FOREIGN KEY (ID_Cliente) REFERENCES Utente(ID_Utente) ON DELETE CASCADE
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
    Tipo VARCHAR(50) NOT NULL,
    Livello VARCHAR(50),
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