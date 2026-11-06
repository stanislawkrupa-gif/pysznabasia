# Pyszna Basia – aplikacja na Androida

Aplikacja do zamawiania cateringu, zimnych płyt, dań gorących, tortów i ciast
(w stylu pyszne.pl). Kotlin + Jetpack Compose (Material 3).

## Funkcje
- **Menu** z 5 kategoriami (Catering, Zimne płyty, Dania gorące, Torty, Ciasta), wyszukiwarka i filtry.
- **Karta produktu**: warianty (np. tort 12/20/30 porcji, pół/cała blacha), ilość,
  napis na torcie / uwagi, minimalna liczba osób dla cateringu.
- **Koszyk** ze zmianą ilości, usuwaniem i licznikiem do darmowej dostawy.
- **Składanie zamówienia**: dane kontaktowe, dostawa lub odbiór osobisty, wybór dnia
  i przedziału godzin (najwcześniejsza data zależy od produktów – torty min. 2 dni,
  komunia min. 3 dni), płatność (BLIK, karta/gotówka przy odbiorze, przelew), uwagi, walidacja pól.
- **Potwierdzenie** z numerem zamówienia (PB-xxxxxx).
- **Historia zamówień** (zapisywana w telefonie) z opcją „Zamów ponownie”.
- Ciemny motyw, ikona aplikacji, polskie formatowanie cen.

Zasady cenowe (do zmiany w `data/Models.kt` → `Pricing`): dostawa 15 zł, gratis od 300 zł.
Menu i ceny: `data/MenuRepository.kt`.

## Uruchomienie
1. Otwórz folder `PysznaBasia` w **Android Studio** (Ladybug lub nowsze).
2. Poczekaj na synchronizację Gradle (Studio samo pobierze wrapper i SDK 35).
3. Uruchom na emulatorze lub telefonie (Android 8.0+). ▶️

## Co dalej (do wersji produkcyjnej)
Aplikacja działa w pełni lokalnie – zamówienia nie trafiają jeszcze na serwer.
Żeby Basia faktycznie je otrzymywała:
- w `AppViewModel.placeOrder()` (miejsce oznaczone `TODO`) wyślij zamówienie do backendu
  (np. Retrofit → własne API, Firebase Firestore lub choćby e-mail/SMS przez usługę zewnętrzną),
- podepnij prawdziwą płatność BLIK/kartą (np. Przelewy24, PayU, Stripe),
- pobieraj menu z serwera zamiast z `MenuRepository`,
- dodaj zdjęcia produktów (obecnie emoji jako miniatury) – np. biblioteką Coil,
- uzupełnij adres odbioru osobistego w `CheckoutScreen.kt` i regulamin.

## Struktura
```
app/src/main/java/pl/pysznabasia/
├── MainActivity.kt          – nawigacja + dolny pasek (Menu / Koszyk / Zamówienia)
├── AppViewModel.kt          – koszyk, składanie zamówień, historia
├── data/
│   ├── Models.kt            – modele, ceny, dostawa, formatowanie
│   ├── MenuRepository.kt    – menu z cenami
│   └── OrderStorage.kt      – zapis historii zamówień
└── ui/
    ├── theme/Theme.kt
    ├── components/Components.kt
    └── screens/             – Menu, Produkt, Koszyk, Zamówienie, Potwierdzenie, Historia
```
