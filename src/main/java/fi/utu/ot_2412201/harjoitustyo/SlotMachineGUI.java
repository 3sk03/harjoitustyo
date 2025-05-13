package fi.utu.ot_2412201.harjoitustyo;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * SlotMachineGUI on graafinen versio kolikkopelille
 */

// KÄYTIN OPENAI CHATGBPT:TÄ TÄSSÄ AVUSTUKSESSA JOTTA SAISIN ALKUPERÄISEN KOLIKKOPELIN GRAAFISESTI NÄYTTÄMÄÄN HYVÄLTÄ.
// KOSKA TÄMÄ OLI VÄHÄN VAIKEAMPAA KUIN LUULIN MUTTA TEIN ITSE KUNNES TULI JOKIN VIRHE MISTÄ EN PÄÄSSYT MILLÄÄN LÄPI.
public class SlotMachineGUI extends JFrame {

    private static final int RIVIT = 3;
    private static final int SARAKKEET = 3;
    private static final int PYORAYTYS_HINTA = 10;
    private static final int VOITTO_SUMMA = 50;

    private JLabel[][] slotit = new JLabel[RIVIT][SARAKKEET];
    private JLabel saldoLabel;
    private JLabel viestiLabel;
    private JButton pyoraytaButton;

    private int saldo = 100;

    private final String[] symbolit = {"@", "X", "$", "*", "O"};

    private final Random rand = new Random();

    /**
     * Luo käyttöliittymän ja alustaa komponentit
     */
    public SlotMachineGUI() {
        setTitle("Kolikkopeli");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel slotPaneeli = new JPanel(new GridLayout(RIVIT, SARAKKEET));
        for (int i = 0; i < RIVIT; i++) {
            for (int j = 0; j < SARAKKEET; j++) {
                slotit[i][j] = new JLabel("?", SwingConstants.CENTER);
                slotit[i][j].setFont(new Font("Monospaced", Font.BOLD, 32));
                slotit[i][j].setOpaque(true);
                slotit[i][j].setBackground(Color.WHITE);
                slotPaneeli.add(slotit[i][j]);
            }
        }
        add(slotPaneeli, BorderLayout.CENTER);

        JPanel alaPaneeli = new JPanel(new GridLayout(3, 1));

        saldoLabel = new JLabel("Saldo: " + saldo + " kolikkoa", SwingConstants.CENTER);
        viestiLabel = new JLabel("Tervetuloa pelaamaan!", SwingConstants.CENTER);

        pyoraytaButton = new JButton("🎰 Pyöräytä");
        pyoraytaButton.addActionListener(e -> pyoraytaAnimoidusti());

        alaPaneeli.add(saldoLabel);
        alaPaneeli.add(pyoraytaButton);
        alaPaneeli.add(viestiLabel);

        add(alaPaneeli, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Käynnistää animoidun pyöräytyksen
     */
    private void pyoraytaAnimoidusti() {
        if (saldo < PYORAYTYS_HINTA) {
            viestiLabel.setText("Saldo loppui! Peli päättyi.");
            pyoraytaButton.setEnabled(false);
            return;
        }

        saldo -= PYORAYTYS_HINTA;
        saldoLabel.setText("Saldo: " + saldo + " kolikkoa");
        viestiLabel.setText("Pyöräytetään...");
        soitaAani("spin.wav"); // placeholder

        Timer[] ajastimet = new Timer[SARAKKEET];

        for (int sarake = 0; sarake < SARAKKEET; sarake++) {
            int finalSarake = sarake;
            ajastimet[sarake] = new Timer(200 * (sarake + 1), new ActionListener() {
                int count = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    count++;
                    for (int i = 0; i < RIVIT; i++) {
                        slotit[i][finalSarake].setText(symbolit[rand.nextInt(symbolit.length)]);
                    }
                    if (count >= 5) {
                        ((Timer) e.getSource()).stop();
                        if (finalSarake == SARAKKEET - 1) tarkistaVoitto();
                    }
                }
            });
            ajastimet[sarake].start();
        }
    }

    /**
     * Tarkistaa onko pelaaja voittanut ja päivittää näkymän
     */
    private void tarkistaVoitto() {
        boolean voitto = false;
        for (int i = 0; i < RIVIT; i++) {
            String eka = slotit[i][0].getText();
            if (eka.equals(slotit[i][1].getText()) && eka.equals(slotit[i][2].getText())) {
                voitto = true;
                break;
            }
        }

        if (voitto) {
            saldo += VOITTO_SUMMA;
            saldoLabel.setText("Saldo: " + saldo + " kolikkoa");
            viestiLabel.setText("🎉 Voitit " + VOITTO_SUMMA + " kolikkoa!");
            soitaAani("win.wav");
            naytaVoittoAnimaatio();
        } else {
            viestiLabel.setText("Ei voittoa tällä kertaa.");
        }
    }

    /**
     * Näyttää animaation voiton yhteydessä
     */
    private void naytaVoittoAnimaatio() {
        Timer animaatio = new Timer(50, null);
        animaatio.addActionListener(new ActionListener() {
            int i = 0;
            boolean kirkas = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                Color väri = kirkas ? Color.YELLOW : Color.WHITE;
                for (int r = 0; r < RIVIT; r++) {
                    for (int s = 0; s < SARAKKEET; s++) {
                        slotit[r][s].setBackground(väri);
                    }
                }
                kirkas = !kirkas;
                i++;
                if (i > 6) {
                    ((Timer) e.getSource()).stop();
                    for (int r = 0; r < RIVIT; r++) {
                        for (int s = 0; s < SARAKKEET; s++) {
                            slotit[r][s].setBackground(Color.WHITE);
                        }
                    }
                }
            }
        });
        animaatio.start();
    }

    /**
     * Toistaa äänitiedoston
     * @param tiedostoNimi äänitiedoston nimi
     */
    private void soitaAani(String tiedostoNimi) {
        try {
            File tiedosto = new File(tiedostoNimi);
            if (!tiedosto.exists()) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(tiedosto);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            System.out.println("Äänen toisto epäonnistui: " + e.getMessage());
        }
    }

    /**
     * Käynnistää GUI:n Swingin säikeessä
     * @param args Komentoriviparametri
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SlotMachineGUI::new);
    }
}
