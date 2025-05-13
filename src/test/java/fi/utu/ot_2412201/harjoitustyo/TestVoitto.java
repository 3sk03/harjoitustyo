package fi.utu.ot_2412201.harjoitustyo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestVoitto {

    @Test
    public void testVoittoRivilla() {
        SlotMachine kone = new SlotMachine(new MuistissaTallentaja());

        Symboli[][] voittavaRuudukko = {
                {new Symboli("X"), new Symboli("X"), new Symboli("X")},
                {new Symboli("O"), new Symboli("*"), new Symboli("@")},
                {new Symboli("*"), new Symboli("$"), new Symboli("O")}
        };

        kone.setRuudukko(voittavaRuudukko);
        assertTrue(kone.tarkistaVoitto(), "Voiton pitäisi löytyä ensimmäiseltä riviltä");
    }
    @Test
    public void testHavioRivilla() {
        SlotMachine kone = new SlotMachine(new MuistissaTallentaja());

        Symboli[][] voittavaRuudukko = {
                {new Symboli("*"), new Symboli("X"), new Symboli("*")},
                {new Symboli("O"), new Symboli("*"), new Symboli("X")},
                {new Symboli("*"), new Symboli("$"), new Symboli("O")}
        };

        kone.setRuudukko(voittavaRuudukko);
        assertFalse(kone.tarkistaVoitto(), "Ei pitäisi olla voittoa");
    }
}
