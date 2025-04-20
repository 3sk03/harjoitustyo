package fi.utu.ot_2412201.harjoitustyo;

import java.util.*;
import com.google.gson.*;
import java.io.*;

// Symboli-luokka
class Symboli {
    private final String arvo;

    public Symboli(String arvo) {
        this.arvo = arvo;
    }

    public String getArvo() {
        return arvo;
    }

    public static Symboli satunnainen() {
        String[] symbolit = {"O", "X", "*", "$", "@"};
        Random rand = new Random();
        return new Symboli(symbolit[rand.nextInt(symbolit.length)]);
    }
}

// Pelitulos-luokka
class Pelitulos {
    public String[][] ruudukko;
    public boolean voitto;

    public Pelitulos(String[][] ruudukko, boolean voitto) {
        this.ruudukko = ruudukko;
        this.voitto = voitto;
    }
}

// TiedonTallentaja-rajapinta
interface TiedonTallentaja {
    void tallenna(Pelitulos tulos) throws IOException;
    List<Pelitulos> lataa() throws IOException;
}

// MuistissaTallentaja
class MuistissaTallentaja implements TiedonTallentaja {
    private final List<Pelitulos> tulokset = new ArrayList<>();

    public void tallenna(Pelitulos tulos) {
        tulokset.add(tulos);
    }

    public List<Pelitulos> lataa() {
        return tulokset;
    }
}

// TiedostonTallentaja Gsonilla
class TiedostonTallentaja implements TiedonTallentaja {
    private final String tiedosto = "tulokset.json";
    private final Gson gson = new Gson();

    public void tallenna(Pelitulos tulos) throws IOException {
        List<Pelitulos> nykyiset = lataa();
        nykyiset.add(tulos);
        try (Writer writer = new FileWriter(tiedosto)) {
            gson.toJson(nykyiset, writer);
        }
    }

    public List<Pelitulos> lataa() throws IOException {
        File file = new File(tiedosto);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(tiedosto)) {
            Pelitulos[] taulukko = gson.fromJson(reader, Pelitulos[].class);
            return taulukko != null ? new ArrayList<>(Arrays.asList(taulukko)) : new ArrayList<>();
        }
    }
}

// SlotMachine-luokka
class SlotMachine {
    private final Symboli[][] ruudukko = new Symboli[3][3];
    private final TiedonTallentaja tallentaja;

    public SlotMachine(TiedonTallentaja tallentaja) {
        this.tallentaja = tallentaja;
    }

    public void pyorita() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ruudukko[i][j] = Symboli.satunnainen();
            }
        }
    }

    public boolean tarkistaVoitto() {
        for (int i = 0; i < 3; i++) {
            if (ruudukko[i][0].getArvo().equals(ruudukko[i][1].getArvo()) &&
                    ruudukko[i][1].getArvo().equals(ruudukko[i][2].getArvo())) {
                return true;
            }
        }
        return false;
    }

    public void tulostaRuudukko() {
        for (Symboli[] rivi : ruudukko) {
            for (Symboli s : rivi) {
                System.out.print(s.getArvo() + " ");
            }
            System.out.println();
        }
    }

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

// Main-luokka
public class Main {
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
