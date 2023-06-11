import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

//Klasse die verantwoordelijk is voor het afronden van de prijzen.
class PrijsAfronden {
    public static BigDecimal rondPrijsAf(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}

//Klasse die verantwoordelijk is voor de berekeningen
class Calculator {
    public static BigDecimal berekenPrijsZonderKorting(int aantal, BigDecimal productPrijs) {
        return PrijsAfronden.rondPrijsAf(productPrijs.multiply(BigDecimal.valueOf(aantal)));
    }

    public static BigDecimal berekenPrijsNaKorting(BigDecimal prijs, BigDecimal kortingPercentage) {
        BigDecimal korting = prijs.multiply(kortingPercentage.divide(BigDecimal.valueOf(100)));
        return prijs.subtract(korting);
    }
}
// Klasse die verantwoordelijk is voor het toepassen van de korting strategieën
class KortingUtility {
    public static BigDecimal pasKortingenToe(BigDecimal prijs, List<KortingStrategie> kortingen) {
        BigDecimal totaalPrijs = prijs;
        for (KortingStrategie korting : kortingen) {
            totaalPrijs = korting.pasKortingStrategieToe(totaalPrijs);
        }
        return PrijsAfronden.rondPrijsAf(totaalPrijs);
    }
}

// Algemene uitlijning voor facturen
class Factuur {
    private int aantal;
    private BigDecimal productPrijs;

    public BigDecimal berekenPrijs() {

        return Calculator.berekenPrijsZonderKorting(aantal, productPrijs);
    }

    public void setAantal(int aantal) {
        this.aantal = aantal;
    }

    public void setProductPrijs(BigDecimal productPrijs) {
        this.productPrijs = productPrijs;
    }
}

// Klasse extend deze klasse indien de facturen in aanmerking komen voor kortingen.
class FactuurMetKorting extends Factuur {
    private List<KortingStrategie> kortingen;

    @Override
    public BigDecimal berekenPrijs() {
        BigDecimal prijs = super.berekenPrijs();
        return KortingUtility.pasKortingenToe(prijs, kortingen);
    }

    public void setKortingen(List<KortingStrategie> kortingen) {
        this.kortingen = kortingen;
    }
}

//Examen facturen hebben geen kortingen. Alleen een lager tarief indien het rustig is (korte wachttijd).
class ExamenFactuur extends Factuur {
    private final BigDecimal EXAMEN_PRIJS = BigDecimal.valueOf(150.0);
    //Toeslag indien lange wachttijd (isDruk)
    private final int TOESLAG_IN_PROCENTEN = 10;
    private int aantal;
    private boolean isDruk;
    //constructor
    public ExamenFactuur(int aantalExamens, boolean isDruk) {
        this.aantal = aantalExamens;
        this.isDruk = isDruk;
    }

    //Eigen implementatie van berekenPrijs. ExamenFactuur kent geen kortingen, alleen een toeslag van 10% indien de wachttijd lang is.
    @Override
    public BigDecimal berekenPrijs() {
        if (isDruk) {
            return PrijsAfronden.rondPrijsAf(EXAMEN_PRIJS.multiply(BigDecimal.valueOf(1.0 + (double) TOESLAG_IN_PROCENTEN /100).multiply(BigDecimal.valueOf(aantal))));
        }
        return PrijsAfronden.rondPrijsAf(EXAMEN_PRIJS.multiply(BigDecimal.valueOf(aantal)));
    }
}
//Factuur voor losse lessen
class LesFactuur extends FactuurMetKorting{

    public LesFactuur(BigDecimal lesPrijs, int aantalLessen, List<KortingStrategie> kortingen) {
        super.setProductPrijs(lesPrijs);
        super.setAantal(aantalLessen);
        this.setKortingen(kortingen);
    }
}


class LesPakketFactuur extends FactuurMetKorting {
    private final Pakket pakket;

    public LesPakketFactuur(Pakket pakket, int aantalPakketten, List<KortingStrategie> kortingen) {
        this.pakket = pakket;
        this.setKortingen(kortingen);
        super.setAantal(aantalPakketten);
        super.setProductPrijs(pakket.prijsVanPakket());
    }
}

//Pakket, hier kunnen standaard pakketten van gemaakt worden.
class Pakket {
    private int aantalLessen;
    private final BigDecimal LES_PRIJS = BigDecimal.valueOf(55.0);

    public Pakket(int aantalLessen) {
        this.aantalLessen = aantalLessen;
    }

    public BigDecimal prijsVanPakket() {
        return PrijsAfronden.rondPrijsAf(LES_PRIJS.multiply(BigDecimal.valueOf(aantalLessen)));
    }
}

//Interface voor de Korting Strategieën. De List in de facturen bevatten objecten hiervan.
//List met kortingen bevordert flexibele toepassing van kortingen.
interface KortingStrategie {
    //Instanties kunnen een eigen implementatie hebben, bijvoorbeeld GroteAankoop, deze kijkt eerst naar
    //Bepaalde voorwaarden en bepaalt dan wat er gebeurt.
    BigDecimal pasKortingStrategieToe(BigDecimal prijs);

}

class EersteAankoop implements KortingStrategie {
    private final BigDecimal KORTING_IN_PROCENTEN = BigDecimal.valueOf(3);

    @Override
    public BigDecimal pasKortingStrategieToe(BigDecimal prijs) {
        return Calculator.berekenPrijsNaKorting(prijs, KORTING_IN_PROCENTEN);
    }
}

class GroteAankoop implements KortingStrategie {
    private final BigDecimal KORTING_IN_PROCENTEN = BigDecimal.valueOf(5);
    private final BigDecimal KORTING_VANAF_PRIJS = BigDecimal.valueOf(500);

    //Alleen korting verrekenen als aankoop groter is dan of gelijk is aan 500.
    @Override
    public BigDecimal pasKortingStrategieToe(BigDecimal prijs) {
        if (prijs.compareTo(KORTING_VANAF_PRIJS) >= 0) {
            return Calculator.berekenPrijsNaKorting(prijs, KORTING_IN_PROCENTEN);
        }
        return prijs;
    }

}

class VakantieKorting implements KortingStrategie {
    private final BigDecimal KORTING_IN_PROCENTEN = BigDecimal.valueOf(10);

    @Override
    public BigDecimal pasKortingStrategieToe(BigDecimal prijs) {
        return Calculator.berekenPrijsNaKorting(prijs, KORTING_IN_PROCENTEN);
    }
}

class FamilieKorting implements KortingStrategie {
    private final BigDecimal KORTING_IN_PROCENTEN = BigDecimal.valueOf(15);

    @Override
    public BigDecimal pasKortingStrategieToe(BigDecimal prijs) {
        return Calculator.berekenPrijsNaKorting(prijs, KORTING_IN_PROCENTEN);
    }
}
class Main {
    public void main(String[] args) {
        // Kortingen configureren
        List<KortingStrategie> kortingen = new ArrayList<>();
        kortingen.add(new EersteAankoop());
        kortingen.add(new GroteAankoop());
        kortingen.add(new FamilieKorting());
        kortingen.add(new VakantieKorting());

        // Les factuur maken met kortingen
        BigDecimal lesPrijs = BigDecimal.valueOf(55);
        int aantalLessen = 10;
        LesFactuur lesFactuur = new LesFactuur(lesPrijs, aantalLessen, kortingen);

        // LesPakketFactuur maken met kortingen
        int aantalPakketten = 2;
        Pakket pakket = new Pakket(10);
        LesPakketFactuur pakketFactuur = new LesPakketFactuur(pakket, aantalPakketten, kortingen);

        // Examen factuur maken zonder extra kortingen
        int aantalExamens = 3;
        ExamenFactuur examenFactuur = new ExamenFactuur(aantalExamens, true);



        // Prijs berekenen inclusief kortingen
        BigDecimal lesFactuurPrijsMetKorting = lesFactuur.berekenPrijs();
        BigDecimal pakketFactuurPrijsMetKorting = pakketFactuur.berekenPrijs();

        // Prijs berekenen zonder kortingen
        BigDecimal lesFactuurPrijsZonderKorting = lesFactuur.berekenPrijs();
        BigDecimal pakketFactuurPrijsZonderKorting = pakketFactuur.berekenPrijs();
        BigDecimal examenFactuurPrijsZonderKorting = examenFactuur.berekenPrijs();

        // Resultaten weergeven
        System.out.println("Lesfactuur prijs zonder korting: " + lesFactuurPrijsZonderKorting);
        System.out.println("Lesfactuur prijs met korting: " + lesFactuurPrijsMetKorting);
        System.out.println("Pakketfactuur prijs zonder korting: " + pakketFactuurPrijsZonderKorting);
        System.out.println("Pakketfactuur prijs met korting: " + pakketFactuurPrijsMetKorting);
        System.out.println("Examenfactuur prijs: " + examenFactuurPrijsZonderKorting);
    }
}
