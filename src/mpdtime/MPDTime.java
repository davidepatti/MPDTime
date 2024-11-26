package mpdtime;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import java.util.Properties;
import java.io.*;
import java.time.format.DateTimeParseException;
import javax.sound.sampled.*;

public class MPDTime extends JFrame {

    private Properties properties;
    private final String PROPERTIES_FILE = System.getProperty("user.home") + File.separator + "mpdtime.properties";
    private final String REPORT_FILE = System.getProperty("user.home") + File.separator + "report.txt";

    // Componenti dell'interfaccia
    private JLabel minutiGiornalieriLabel;
    private JLabel minutiTotaliLabel;
    private JLabel clessidraGiornalieriLabel;
    private JLabel clessidraTotaliLabel;
    private JButton toggleButton;
    private JButton pagamentoButton;
    private JButton reportButton;
    private JButton infoButton;

    // Pannello principale con immagine di sfondo
    private BackgroundPanel mainPanel;

    // Timer e Task
    private Timer timer;
    private CountTask countTask;

    // Variabili per il conteggio
    private int secondiGiornalieri = 0;
    private int secondiTotali = 0;
    // Variabili per i nuovi tipi di Clessidra
    private int clessidraCentoniGiornalieri = 0;
    private int clessidraCentoniTotali = 0;
    private int clessidraDollariGiornalieri = 0;
    private int clessidraDollariTotali = 0;
    private int clessidraCentesimiGiornalieri = 0;
    private int clessidraCentesimiTotali = 0;


    private LocalDate dataCorrente;

    public MPDTime() {
        setTitle("MPDTime");
        setSize(400, 500);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setLocationRelativeTo(null);

        dataCorrente = LocalDate.now();

        // Carica i dati dal file di properties
        loadCounts();
        initComponents();
        aggiornaLabel();
        setVisible(true);
        // Aggiunge un listener per salvare i dati alla chiusura
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveCounts();
                dispose();
            }
        });
    }
    private void loadCounts() {
        properties = new Properties();
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(input);
            secondiGiornalieri = Integer.parseInt(properties.getProperty("secondiGiornalieri", "0"));
            secondiTotali = Integer.parseInt(properties.getProperty("secondiTotali", "0"));
            clessidraDollariGiornalieri = Integer.parseInt(properties.getProperty("clessidraGiornalieri", "0"));
            clessidraDollariTotali = Integer.parseInt(properties.getProperty("clessidraTotali", "0"));
            clessidraCentoniGiornalieri = Integer.parseInt(properties.getProperty("centoniGiornalieri", "0"));
            clessidraCentoniTotali = Integer.parseInt(properties.getProperty("centoniTotali", "0"));
            clessidraCentesimiTotali = Integer.parseInt(properties.getProperty("centesimiTotali", "0"));
            clessidraCentesimiGiornalieri = Integer.parseInt(properties.getProperty("centesimiGiornalieri", "0"));
            String dataSalvata = properties.getProperty("dataCorrente", LocalDate.now().toString());
            try {
                dataCorrente = LocalDate.parse(dataSalvata);
            } catch (DateTimeParseException e) {
                dataCorrente = LocalDate.now();
            }

            // Controlla se la data è cambiata
            if (!dataCorrente.equals(LocalDate.now())) {
                secondiGiornalieri = 0;
                clessidraDollariGiornalieri = 0;
                clessidraCentesimiGiornalieri = 0;
                clessidraCentoniGiornalieri = 0;
                dataCorrente = LocalDate.now();
            }

        } catch (IOException e) {
            System.out.println("Nessun file di properties trovato. Verranno utilizzati i valori di default.");
        }
    }

    private void saveCounts() {
        properties = new Properties();

        properties.setProperty("secondiGiornalieri", Integer.toString(secondiGiornalieri));
        properties.setProperty("secondiTotali", Integer.toString(secondiTotali));
        properties.setProperty("clessidraGiornalieri", Integer.toString(clessidraDollariGiornalieri));
        properties.setProperty("clessidraTotali", Integer.toString(clessidraDollariTotali));
        properties.setProperty("centesimiGiornalieri", Integer.toString(clessidraCentesimiGiornalieri));
        properties.setProperty("centesimiTotali", Integer.toString(clessidraCentesimiTotali));
        properties.setProperty("centoniGiornalieri", Integer.toString(clessidraCentoniGiornalieri));
        properties.setProperty("centoniTotali", Integer.toString(clessidraCentoniTotali));
        properties.setProperty("dataCorrente", dataCorrente.toString());

        try (OutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleTimer(JButton button) {
        if (timer == null) {
            // Avvia il timer
            timer = new Timer();
            countTask = new CountTask();
            timer.scheduleAtFixedRate(countTask, 0, 1000); // Aggiornamento ogni secondo
            // Cambia l'immagine di sfondo a clessidra.png
            mainPanel.setBackgroundImage("clessidra.png");
            // Aggiorna l'icona del pulsante allo stato "Ferma"
            button.setIcon(loadIcon("stop_normal.png", 100, 100));
        } else {
            // Ferma il timer
            timer.cancel();
            timer = null;
            // Cambia l'immagine di sfondo a default.png
            mainPanel.setBackgroundImage("default.png");
            // Aggiorna l'icona del pulsante allo stato "Avvia"
            button.setIcon(loadIcon("play_normal.png", 100, 100));
        }
    }

    private JButton createToggleButton() {
        JButton button = new JButton();
        int newWidth = 70;  // Imposta la larghezza desiderata
        int newHeight = 70; // Imposta l'altezza desiderata
        button.setPreferredSize(new Dimension(newWidth, newHeight)); // Imposta la dimensione del pulsante

        // Carica le immagini per lo stato "Avvia"
        ImageIcon playNormalIcon = loadIcon("play_normal.png", newWidth, newHeight);
        ImageIcon playHoverIcon = loadIcon("play_hover.png", newWidth, newHeight);
        ImageIcon playPressedIcon = loadIcon("play_pressed.png", newWidth, newHeight);

        // Carica le immagini per lo stato "Ferma"
        ImageIcon stopNormalIcon = loadIcon("stop_normal.png", newWidth, newHeight);
        ImageIcon stopHoverIcon = loadIcon("stop_hover.png", newWidth, newHeight);
        ImageIcon stopPressedIcon = loadIcon("stop_pressed.png", newWidth, newHeight);

        // Imposta le icone iniziali (stato "Avvia")
        button.setIcon(playNormalIcon);

        // Rimuove le decorazioni del pulsante
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        // Aggiunge l'ActionListener per gestire il toggle
        button.addActionListener(e -> toggleTimer(button));

        // Aggiunge i listener per gli eventi del mouse
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (timer == null) {
                    button.setIcon(playHoverIcon);
                } else {
                    button.setIcon(stopHoverIcon);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (timer == null) {
                    button.setIcon(playNormalIcon);
                } else {
                    button.setIcon(stopNormalIcon);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (timer == null) {
                    button.setIcon(playPressedIcon);
                } else {
                    button.setIcon(stopPressedIcon);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.contains(evt.getPoint())) {
                    if (timer == null) {
                        button.setIcon(playHoverIcon);
                    } else {
                        button.setIcon(stopHoverIcon);
                    }
                } else {
                    if (timer == null) {
                        button.setIcon(playNormalIcon);
                    } else {
                        button.setIcon(stopNormalIcon);
                    }
                }
            }
        });

        return button;
    }

    private void initComponents() {


        // Inizializzazione del pannello principale con immagine di default
        mainPanel = new BackgroundPanel("default.png");
        mainPanel.setLayout(new GridBagLayout());

        // Inizializzazione dei componenti
        minutiGiornalieriLabel = new JLabel("Minuti Giornalieri: 0");
        minutiTotaliLabel = new JLabel("Minuti Totali: 0");
        clessidraGiornalieriLabel = new JLabel("Clessidra Giornalieri: 0");
        clessidraTotaliLabel = new JLabel("Clessidra Totali: 0");
        // Variabile per la dimensione del carattere
        int fontSize = 16; // Puoi modificare questo valore per cambiare la dimensione

// Creazione del font
        Font labelFont = new Font("Arial", Font.PLAIN, fontSize);

// Impostazione del font per le etichette
        minutiGiornalieriLabel.setFont(labelFont);
        minutiTotaliLabel.setFont(labelFont);
        clessidraGiornalieriLabel.setFont(labelFont);
        clessidraTotaliLabel.setFont(labelFont);

        // Creazione del pannello delle etichette
        JPanel labelsPanel = new JPanel();
        labelsPanel.setOpaque(false); // Rende il pannello trasparente (lo modificheremo per la semi-trasparenza)
        labelsPanel.setLayout(new GridLayout(5, 1, 5, 5)); // 4 righe, 1 colonna, spaziatura di 5 pixel
        labelsPanel.setBackground(new Color(255, 255, 255, 100)); // Colore bianco semi-trasparente

        labelsPanel.add(minutiGiornalieriLabel);
        labelsPanel.add(minutiTotaliLabel);
        labelsPanel.add(clessidraGiornalieriLabel);
        labelsPanel.add(clessidraTotaliLabel);

        labelsPanel.setOpaque(true);

        toggleButton = createToggleButton();
        pagamentoButton = createButtonWithIcon("simbolo");
        reportButton = createButtonWithIcon("report");
        infoButton = createButtonWithIcon("info");

        // Assegnazione degli eventi ai nuovi pulsanti
        pagamentoButton.addActionListener(e -> pagamento());
        reportButton.addActionListener(e -> mostraReport());
        infoButton.addActionListener(e -> mostraInformazioni());

        // Pannello per la prima riga dei pulsanti
        JPanel buttonPanelRow1 = new JPanel();
        buttonPanelRow1.setOpaque(false);
        buttonPanelRow1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanelRow1.add(toggleButton);

        // Pannello per la seconda riga dei pulsanti
        JPanel buttonPanelRow2 = new JPanel();
        buttonPanelRow2.setOpaque(false);
        buttonPanelRow2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanelRow2.add(pagamentoButton);
        buttonPanelRow2.add(reportButton);
        buttonPanelRow2.add(infoButton);


        // Pannello principale dei pulsanti
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(2, 1));

// Aggiunta dei pannelli delle righe al pannello principale
        buttonPanel.add(buttonPanelRow1);
        buttonPanel.add(buttonPanelRow2);

// Aggiunta del bordo al pannello principale dei pulsanti
        Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        //Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, emptyBorder);
        Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, etchedBorder);
        buttonPanel.setBorder(compoundBorder);

        // Aggiunta di un lieve bordo al pannello dei pulsanti
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(new Color(0, 0, 0, 100)); // Colore nero semi-trasparente



        // Creazione del layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        mainPanel.add(labelsPanel, gbc);

        // Spaziatore per occupare lo spazio centrale
        gbc.gridy++;
        gbc.weighty = 1.0;
        mainPanel.add(Box.createVerticalGlue(), gbc);

        // Aggiunta del pannello dei pulsanti in basso
        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(buttonPanel, gbc);

        // Aggiunta del pannello principale al frame
        setContentPane(mainPanel);

    }

    private ImageIcon loadIcon(String imageName, int width, int height) {
        URL imageURL = getClass().getResource("/" + imageName);
        if (imageURL != null) {
            ImageIcon originalIcon = new ImageIcon(imageURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.err.println("Immagine " + imageName + " non trovata.");
            return null;
        }
    }

    private JButton createButtonWithIcon(String baseName) {
        JButton button = new JButton();
        int newWidth = 50;  // Imposta la larghezza desiderata
        int newHeight = 50; // Imposta l'altezza desiderata
        button.setPreferredSize(new Dimension(newWidth, newHeight)); // Imposta la dimensione del pulsante

        // Carica le immagini per i diversi stati
        ImageIcon normalIcon = loadIcon(baseName + "_normal.png", newWidth, newHeight);
        ImageIcon hoverIcon = loadIcon(baseName + "_hover.png", newWidth, newHeight);
        ImageIcon pressedIcon = loadIcon(baseName + "_pressed.png", newWidth, newHeight);

        // Imposta l'icona normale
        button.setIcon(normalIcon);

        // Rimuove le decorazioni del pulsante
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        // Aggiunge i listener per gli eventi del mouse
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setIcon(normalIcon);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.contains(evt.getPoint())) {
                    button.setIcon(hoverIcon);
                } else {
                    button.setIcon(normalIcon);
                }
            }
        });

        return button;
    }


    private void pagamento() {
        // Calcola il codice segreto basato sulla data corrente
        LocalDateTime oggi = LocalDateTime.now();
        String giorno = String.format("%02d", oggi.getDayOfMonth());
        String mese = String.format("%02d", oggi.getMonthValue());
        String codiceSegreto = giorno + mese;

        // Richiede all'utente di inserire il codice
        String codiceInserito = JOptionPane.showInputDialog(this, "Inserisci il codice numerico per confermare pagamento:");

        // Verifica se l'utente ha annullato l'input
        if (codiceInserito == null) {
            return; // L'utente ha premuto Annulla
        }

        // Verifica se il codice inserito è corretto
        if (codiceInserito.equals(codiceSegreto)) {
            // Raccogliere i valori prima del reset
            int centoniDaAzzerare = clessidraCentoniGiornalieri;
            int dollariDaAzzerare = clessidraDollariGiornalieri;
            int centesimiDaAzzerare = clessidraCentesimiGiornalieri;

            // Scrivere nel file report.txt
            scriviReport(oggi, centoniDaAzzerare, dollariDaAzzerare, centesimiDaAzzerare);

            // Resettare i contatori
            resettaContatori();

            // Mostrare un messaggio di conferma
            JOptionPane.showMessageDialog(this, "Azzeramento effettuato con successo.", "Successo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Mostrare un messaggio di errore
            playErrorSound();
            JOptionPane.showMessageDialog(this, "Codice errato. L'azzeramento non è stato effettuato.", "Errore", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void mostraReport() {
        StringBuilder contenutoReport = new StringBuilder();
        File reportFile = new File(REPORT_FILE);
        if (reportFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(reportFile))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    contenutoReport.append(linea).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore durante la lettura del report.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            contenutoReport.append("Nessun report disponibile.");
        }

        // Creazione della finestra per mostrare il report
        JTextArea textArea = new JTextArea(contenutoReport.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JFrame reportFrame = new JFrame("Report");
        reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reportFrame.setSize(500, 400);
        reportFrame.add(scrollPane);
        reportFrame.setLocationRelativeTo(this);
        reportFrame.setVisible(true);
    }


    private void scriviReport(LocalDateTime dataOra, int centoni, int dollari, int centesimi) {
        try (FileWriter fw = new FileWriter(REPORT_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String riga = dataOra.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    " - Centoni: " + centoni + ", Dollari: " + dollari + ", Centesimi: " + centesimi;
            out.println(riga);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playErrorSound() {
        try {
            // Carica il file audio
            URL soundURL = getClass().getResource("/fail.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);

            // Ottiene un clip audio
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            // Riproduce il clip
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void mostraInformazioni() {
        String info = "<html><body>" +
                "<h2>MPDTime</h2>" +
                "<p>Versione: 1.1</p>" +
                "<p>programmed by Davide Patti</p>" +
                "<p>debugging & test by Paolo Patti</p>" +
                "<p>Descrizione: Applicazione per il conteggio del tempo e delle Clessidra.</p>" +
                "</body></html>";

        JLabel label = new JLabel(info);
        label.setFont(new Font("Arial", Font.PLAIN, 14));

        JOptionPane.showMessageDialog(this, label, "Informazioni", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resettaContatori() {
        secondiGiornalieri = 0;
        secondiTotali = 0;
        clessidraCentoniGiornalieri = 0;
        clessidraCentoniTotali = 0;
        clessidraDollariGiornalieri = 0;
        clessidraDollariTotali = 0;
        clessidraCentesimiGiornalieri = 0;
        clessidraCentesimiTotali = 0;

        aggiornaLabel();
        saveCounts();
    }


    private void aggiornaLabel() {

        int minutiGiornalieri = secondiGiornalieri / 60;
        int restSecondiGiornalieri = secondiGiornalieri % 60;

        int minutiTotali = secondiTotali / 60;
        int restSecondiTotali = secondiTotali % 60;

        minutiGiornalieriLabel.setText("<html><b>Tempo:</b> " + minutiGiornalieri + " minuti " + restSecondiGiornalieri + " secondi</html>");
        minutiTotaliLabel.setText("<html><b>Tempo totale:</b> " + minutiTotali + " minuti " + restSecondiTotali + " secondi</html>");
        clessidraGiornalieriLabel.setText("<html><b>Clessidra:</b> " + clessidraCentoniGiornalieri + " centoni,  " + clessidraDollariGiornalieri + " dollari e " + clessidraCentesimiGiornalieri + " centesimi</html>");
        clessidraTotaliLabel.setText("<html><b>Clessidra Totali:</b> " + clessidraCentoniTotali+ " centoni,  " + clessidraDollariTotali + " dollari e " + clessidraCentesimiTotali + " centesimi</html>");

    }

    // Classe interna per il conteggio del tempo
    private class CountTask extends java.util.TimerTask {

        @Override
        public void run() {
            // Controllo del cambio di giorno
            if (!LocalDate.now().equals(dataCorrente)) {
                dataCorrente = LocalDate.now();
                secondiGiornalieri = 0;
                clessidraCentoniGiornalieri = 0;
                clessidraDollariGiornalieri = 0;
                clessidraCentesimiGiornalieri = 0;
            }

            secondiGiornalieri++;
            secondiTotali++;

            // Calcolo dei Clessidra dopo la prima ora (3600 secondi)
            if (secondiGiornalieri > 3600) {
                int secondiValidi = secondiGiornalieri - 3600;

                // Calcolo dei Centoni (ogni 3600 secondi)
                int totalCentoni = secondiValidi / 3600;
                int nuoviCentoni = totalCentoni - clessidraCentoniGiornalieri;
                if (nuoviCentoni > 0) {
                    clessidraCentoniGiornalieri += nuoviCentoni;
                    clessidraCentoniTotali += nuoviCentoni;
                }

                // Calcolo dei Dollari (ogni 1800 secondi)
                int totalDollari = secondiValidi / 1800;
                int nuoviDollari = totalDollari - (clessidraDollariGiornalieri + clessidraCentoniGiornalieri * 2);
                if (nuoviDollari > 0) {
                    clessidraDollariGiornalieri += nuoviDollari;
                    clessidraDollariTotali += nuoviDollari;
                    if (clessidraDollariTotali ==2 ) {
                        clessidraDollariTotali = 0;
                        clessidraCentoniTotali++;
                    }
                }

                // Calcolo dei Centesimi (ogni 900 secondi)
                int totalCentesimi = secondiValidi / 900;
                int nuoviCentesimi = totalCentesimi - (clessidraCentesimiGiornalieri + clessidraDollariGiornalieri * 2 + clessidraCentoniGiornalieri * 4);
                if (nuoviCentesimi > 0) {
                    clessidraCentesimiGiornalieri += nuoviCentesimi;
                    clessidraCentesimiTotali += nuoviCentesimi;
                    if (clessidraCentesimiTotali ==2) {
                        clessidraCentesimiTotali = 0;
                        clessidraDollariTotali++;

                        if (clessidraDollariTotali ==2 ) {
                            clessidraDollariTotali = 0;
                            clessidraCentoniTotali++;
                        }
                    }
                }
            }

            // Aggiornamento dell'interfaccia
            SwingUtilities.invokeLater(() -> {
                aggiornaLabel();
                saveCounts();
            });
        }
    }
}