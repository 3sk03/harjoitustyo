package fi.utu.ot_2412201.harjoitustyo;

import java.util.*;
import com.google.gson.*;
import java.io.*;

/**
 * Edustaa yksittäistä symbolia peliruudukossa
 */
class Symboli {
    private final String arvo;

    /**
     * Luo uuden symbolin annetulla arvolla
     * @param arvo Merkkijono joka kuvaa symbolin visuaalista arvoa esim "X", "$"
     */
    public Symboli(String arvo) {
        this.arvo = arvo;
    }

    /**
     * Palauttaa symbolin arvon
     * @return Symbolin merkkijonoarvo
     */
    public String getArvo() {
        return arvo;
    }

    /**
     * Luo satunnaisen Symbolin
     * @return Satunnainen symboli
     */
    public static Symboli satunnainen() {
        String[] symbolit = {"O", "X", "*", "$", "@"};
        Random rand = new Random();
        return new Symboli(symbolit[rand.nextInt(symbolit.length)]);
    }
}

/**
 * Kuvaa yhden pelikierroksen lopputulosta
 */
class Pelitulos {
    public String[][] ruudukko;
    public boolean voitto;

    /**
     * Luo uuden pelituloksen annetulla ruudukolla ja voiton tilalla
     * @param ruudukko 3x3 ruudukko joka edustaa pelikierroksen lopullista tilaa
     * @param voitto true jos pelaaja voitti muuten false
     */
    public Pelitulos(String[][] ruudukko, boolean voitto) {
        this.ruudukko = ruudukko;
        this.voitto = voitto;
    }
}

/**
 * Rajapinta pelitulosten tallentamiseen ja lataamiseen eri toteutuksilla
 * (esimerkiksi tiedostoon tai muistiin).
 */
interface TiedonTallentaja {
    /**
     * Tallentaa annetun pelituloksen
     * @param tulos Pelitulos joka halutaan tallentaa
     * @throws IOException Jos tallennus epäonnistuu
     */
    void tallenna(Pelitulos tulos) throws IOException;

    /**
     * Lataa aiemmat pelitulokset
     * @return Lista pelituloksista
     * @throws IOException Jos lataaminen epäonnistuu
     */
    List<Pelitulos> lataa() throws IOException;
}

/**
 * Tallentaa pelitulokset muistiin ajon aikana jos ei haluta käyttää tallentajaa levylle
 */
class MuistissaTallentaja implements TiedonTallentaja {
    private final List<Pelitulos> tulokset = new ArrayList<>();

    /**
     * Tallentaa pelituloksen sisäiseen muistilistaan
     * @param tulos Pelitulos joka lisätään muistilistaan
     */
    public void tallenna(Pelitulos tulos) {
        tulokset.add(tulos);
    }

    /**
     * Palauttaa kaikki muistiin tallennetut pelitulokset
     * @return Lista pelituloksista jotka on tallennettu tämän ajon aikana
     */
    public List<Pelitulos> lataa() {
        return tulokset;
    }
}

/**
 * Tallentaa ja lataa pelitulokset JSON-tiedostosta
 */
class TiedostonTallentaja implements TiedonTallentaja {
    private final String tiedosto = "tulokset.json";
    private final Gson gson = new Gson();

    /**
     * Tallentaa annetun pelituloksen JSON-tiedostoon
     * @param tulos Pelitulos joka halutaan tallentaa
     * @throws IOException Jos tiedoston kirjoittaminen epäonnistuu
     */
    public void tallenna(Pelitulos tulos) throws IOException {
        List<Pelitulos> nykyiset = lataa();
        nykyiset.add(tulos);
        try (Writer writer = new FileWriter(tiedosto)) {
            gson.toJson(nykyiset, writer);
        }
    }

    /**
     * Lataa kaikki aiemmin tallennetut pelitulokset JSON-tiedostosta
     * @return Lista ladatuista pelituloksista jos tiedostoa ei ole palauttaa tyhjän listan
     * @throws IOException Jos tiedoston lukeminen epäonnistuu
     */
    public List<Pelitulos> lataa() throws IOException {
        File file = new File(tiedosto);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(tiedosto)) {
            Pelitulos[] taulukko = gson.fromJson(reader, Pelitulos[].class);
            return taulukko != null ? new ArrayList<>(Arrays.asList(taulukko)) : new ArrayList<>();
        }
    }
}

/**
 * Kolikkopeli ruudukko
 */
class SlotMachine {
    private final Symboli[][] ruudukko = new Symboli[3][3];
    private final TiedonTallentaja tallentaja;

    /**
     * Asettaa ruudukon testikäyttöä varten
     * @param uusiRuudukko 3x3-taulukko symboleita
     */
    public void setRuudukko(Symboli[][] uusiRuudukko) {
        if (uusiRuudukko.length != 3 || uusiRuudukko[0].length != 3) {
            throw new IllegalArgumentException("Ruudukon on oltava 3x3 kokoinen.");
        }
        for (int i = 0; i < 3; i++) {
            System.arraycopy(uusiRuudukko[i], 0, ruudukko[i], 0, 3);
        }
    }

    public SlotMachine(TiedonTallentaja tallentaja) {
        this.tallentaja = tallentaja;
    }

    /**
     * Pyörittää peliruudukkoa satunnaisilla symboleilla
     */
    public void pyorita() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ruudukko[i][j] = Symboli.satunnainen();
            }
        }
    }

    /**
     * Tarkistaa onko voittoa riveillä
     * @return palauttaa että onko tullut voitto vai ei
     */
    public boolean tarkistaVoitto() {
        for (int i = 0; i < 3; i++) {
            if (ruudukko[i][0].getArvo().equals(ruudukko[i][1].getArvo()) &&
                    ruudukko[i][1].getArvo().equals(ruudukko[i][2].getArvo())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tulostaa ruudukon konsoliin
     */
    public void tulostaRuudukko() {
        for (Symboli[] rivi : ruudukko) {
            for (Symboli s : rivi) {
                System.out.print(s.getArvo() + " ");
            }
            System.out.println();
        }
    }

    /**
     * Pelaa yhden kierroksen ja tallentaa tuloksen
     * @throws IOException Jos tapahtuu virhe kun pelaaja pelaa
     */
    public void pelaa() throws IOException {
        pyorita();
        boolean voitto = tarkistaVoitto();
        tulostaRuudukko();
        System.out.println(voitto ? "Voitit!" : "Ei voittoa tällä kertaa.");

        String[][] tulosRuudukko = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tulosRuudukko[i][j] = ruudukko[i][j].getArvo();
            }
        }

        tallentaja.tallenna(new Pelitulos(tulosRuudukko, voitto));
    }
}

/**
 * Pelin kosnsoli käyttöliittymä
 */
public class Main {
    /**
     * Sovelluksen käynnistyspiste
     * @param args Komentoriviparametri
     * @throws IOException Jos tapahtuu virhe tulosten tallennuksessa
     */
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        TiedonTallentaja tallentaja = new TiedostonTallentaja();
        SlotMachine peli = new SlotMachine(tallentaja);

        while (true) {
            System.out.println("Haluatko pelata? (k/e)");
            String vastaus = scanner.nextLine();
            if (!vastaus.equalsIgnoreCase("k")) break;
            peli.pelaa();
        }

        System.out.println("Kiitos pelaamisesta!");
    }
}
