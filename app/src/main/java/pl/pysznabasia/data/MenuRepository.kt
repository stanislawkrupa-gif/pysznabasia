package pl.pysznabasia.data

/**
 * Menu Pysznej Basi. Ceny w groszach.
 * Aby pobierać menu z serwera, wystarczy podmienić listę [products] na dane z API.
 */
object MenuRepository {

    private val halfAndFullTray = { half: Int, full: Int ->
        listOf(Variant("Pół blachy (~12 kawałków)", half), Variant("Cała blacha (~24 kawałki)", full))
    }
    private val platterSizes = { small: Int, large: Int ->
        listOf(Variant("Mała (6–8 osób)", small), Variant("Duża (12–15 osób)", large))
    }
    private val cakeSizes = { s: Int, m: Int, l: Int ->
        listOf(Variant("12 porcji", s), Variant("20 porcji", m), Variant("30 porcji", l))
    }

    val products: List<Product> = listOf(
        // ---------- CATERING ----------
        Product(
            "cat-komunia", "Pakiet komunijny",
            "Rosół z makaronem, schab ze śliwką i rolada z kurczaka, ziemniaki, dwie surówki, deser i owoce.",
            Category.CATERING, "🕊️", listOf(Variant("Za osobę", 129_00)),
            minQty = 10, qtyLabel = "Liczba osób", allowsNote = true, noteLabel = "Diety, alergie, uwagi",
            leadDays = 3,
        ),
        Product(
            "cat-lunch", "Lunch firmowy",
            "Zupa dnia, danie główne i napój. Pakowane w zestawy, idealne do biura.",
            Category.CATERING, "💼", listOf(Variant("Za osobę", 45_00)),
            minQty = 5, qtyLabel = "Liczba osób", allowsNote = true, noteLabel = "Liczba porcji wege, uwagi",
        ),
        Product(
            "cat-stypa", "Obiad okolicznościowy",
            "Barszcz z krokietem, filet drobiowy w sosie, kluski śląskie, modra kapusta, ciasto i kawa.",
            Category.CATERING, "🍽️", listOf(Variant("Za osobę", 69_00)),
            minQty = 10, qtyLabel = "Liczba osób", allowsNote = true, noteLabel = "Diety, alergie, uwagi",
        ),
        Product(
            "cat-grill", "Zestaw grillowy",
            "Karkówka, kiełbaski, szaszłyki drobiowe, pieczywo, sałatki i sosy. Gotowe do położenia na ruszcie.",
            Category.CATERING, "🔥", listOf(Variant("Za osobę", 59_00)),
            minQty = 10, qtyLabel = "Liczba osób",
        ),
        Product(
            "cat-koktajl", "Przekąski koktajlowe",
            "Mini tartaletki, roladki z łososiem, koreczki, wrapy i babeczki – 8 sztuk na osobę.",
            Category.CATERING, "🥂", listOf(Variant("Za osobę", 39_00)),
            minQty = 10, qtyLabel = "Liczba osób",
        ),

        // ---------- ZIMNE PŁYTY ----------
        Product(
            "zp-wedliny", "Półmisek wędlin staropolskich",
            "Szynka wędzona, schab pieczony, polędwica, kiełbasa myśliwska, pasztet, chrzan i ogórki.",
            Category.COLD_PLATTERS, "🥓", platterSizes(169_00, 299_00),
        ),
        Product(
            "zp-sery", "Deska serów",
            "Sery dojrzewające, pleśniowe i oscypek, winogrona, orzechy, konfitura z żurawiny.",
            Category.COLD_PLATTERS, "🧀", platterSizes(189_00, 329_00),
        ),
        Product(
            "zp-sledzie", "Patera śledziowa",
            "Śledzie w oleju z cebulką, w śmietanie z jabłkiem i po kaszubsku.",
            Category.COLD_PLATTERS, "🐟", platterSizes(119_00, 209_00),
        ),
        Product(
            "zp-ryby", "Płyta rybna",
            "Łosoś wędzony, makrela, pstrąg, pasta z wędzonej ryby, cytryna i koperek.",
            Category.COLD_PLATTERS, "🍣", platterSizes(199_00, 349_00),
        ),
        Product(
            "zp-koreczki", "Koreczki mix",
            "Koreczki z serem, wędliną, oliwkami, pomidorkami koktajlowymi i korniszonami.",
            Category.COLD_PLATTERS, "🍢",
            listOf(Variant("30 sztuk", 89_00), Variant("60 sztuk", 169_00)),
        ),

        // ---------- DANIA GORĄCE ----------
        Product(
            "dg-pierogi", "Pierogi ruskie",
            "Ręcznie lepione, z ziemniakami i twarogiem, podane ze złotą cebulką.",
            Category.HOT_DISHES, "🥟",
            listOf(Variant("10 sztuk", 29_00), Variant("50 sztuk", 135_00)),
        ),
        Product(
            "dg-golabki", "Gołąbki w sosie pomidorowym",
            "Mięso z ryżem zawinięte w liście kapusty, duszone w sosie pomidorowym.",
            Category.HOT_DISHES, "🥬",
            listOf(Variant("2 sztuki", 24_00), Variant("10 sztuk", 110_00)),
        ),
        Product(
            "dg-bigos", "Bigos staropolski",
            "Kiszona i słodka kapusta, trzy rodzaje mięsa, suszone grzyby i śliwki.",
            Category.HOT_DISHES, "🍲",
            listOf(Variant("1 kg", 49_00), Variant("3 kg", 139_00)),
        ),
        Product(
            "dg-schab", "Schab pieczony ze śliwką",
            "Porcja schabu z sosem pieczeniowym, ziemniakami i surówką.",
            Category.HOT_DISHES, "🍖", listOf(Variant("Porcja", 34_00)),
        ),
        Product(
            "dg-rolada", "Rolada wołowa",
            "Z ogórkiem, boczkiem i musztardą, kluski śląskie, modra kapusta.",
            Category.HOT_DISHES, "🥩", listOf(Variant("Porcja", 39_00)),
        ),
        Product(
            "dg-zurek", "Żurek staropolski",
            "Na zakwasie Basi, z białą kiełbasą i jajkiem.",
            Category.HOT_DISHES, "🥣", listOf(Variant("1 litr", 32_00), Variant("3 litry", 89_00)),
        ),

        // ---------- TORTY ----------
        Product(
            "t-basia", "Tort Basi (firmowy)",
            "Blaty bezowe, krem mascarpone, świeże maliny i borówki.",
            Category.CAKES, "🎂", cakeSizes(169_00, 259_00, 369_00),
            allowsNote = true, noteLabel = "Napis na torcie", leadDays = 2,
        ),
        Product(
            "t-truskawka", "Tort śmietanowo-truskawkowy",
            "Biszkopt nasączony ponczem, krem śmietankowy i truskawki.",
            Category.CAKES, "🍓", cakeSizes(149_00, 229_00, 329_00),
            allowsNote = true, noteLabel = "Napis na torcie", leadDays = 2,
        ),
        Product(
            "t-czekolada", "Tort czekoladowy z wiśnią",
            "Ciemny biszkopt, ganache z gorzkiej czekolady i wiśnie w nalewce.",
            Category.CAKES, "🍫", cakeSizes(159_00, 249_00, 349_00),
            allowsNote = true, noteLabel = "Napis na torcie", leadDays = 2,
        ),
        Product(
            "t-komunia", "Tort komunijny",
            "Biszkopt z kremem waniliowym i owocami, dekoracja z masy cukrowej z kielichem.",
            Category.CAKES, "🕊️", cakeSizes(199_00, 299_00, 419_00),
            allowsNote = true, noteLabel = "Imię dziecka i napis", leadDays = 3,
        ),
        Product(
            "t-orzech", "Tort orzechowy",
            "Biszkopt orzechowy, krem kawowy i karmelizowane orzechy włoskie.",
            Category.CAKES, "🌰", cakeSizes(159_00, 249_00, 349_00),
            allowsNote = true, noteLabel = "Napis na torcie", leadDays = 2,
        ),

        // ---------- CIASTA ----------
        Product(
            "c-sernik", "Sernik krakowski",
            "Na kruchym spodzie, z rodzynkami i kratką z ciasta.",
            Category.PASTRIES, "🍰", halfAndFullTray(69_00, 129_00),
        ),
        Product(
            "c-szarlotka", "Szarlotka z kruszonką",
            "Dużo jabłek z cynamonem na kruchym cieście.",
            Category.PASTRIES, "🍎", halfAndFullTray(59_00, 109_00),
        ),
        Product(
            "c-plesniak", "Pleśniak",
            "Kruche ciasto, konfitura z porzeczek, piana i kruszonka.",
            Category.PASTRIES, "🫐", halfAndFullTray(59_00, 109_00),
        ),
        Product(
            "c-karpatka", "Karpatka",
            "Ciasto parzone z budyniowym kremem i cukrem pudrem.",
            Category.PASTRIES, "🏔️", halfAndFullTray(62_00, 115_00),
        ),
        Product(
            "c-makowiec", "Makowiec",
            "Drożdżowy wałek z gęstą masą makową i lukrem. Ok. 1 kg.",
            Category.PASTRIES, "🥖", listOf(Variant("Sztuka (~1 kg)", 65_00)),
        ),
        Product(
            "c-paczki", "Pączki z różą",
            "Puszyste, smażone na smalcu, z konfiturą różaną i lukrem.",
            Category.PASTRIES, "🍩", listOf(Variant("6 sztuk", 29_00), Variant("12 sztuk", 54_00)),
        ),
    )

    fun find(id: String): Product? = products.firstOrNull { it.id == id }
}
