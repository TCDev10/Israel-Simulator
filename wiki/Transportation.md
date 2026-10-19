# Trasporti

L'idea (**GAME_DESIGN.md §32**) è muoversi tra città e biomi senza rompere l'esplorazione.

---

## 1. La bicicletta (`israel_simulator:bicycle`)

Comoda per girare per Tel Aviv, sul lungomare di Giaffa e sui sentieri.

### Com'è fatta
- **Telaio 3D** verde acqua, come le bici del bike sharing di Tel Aviv.
- **Ruote** con i raggi che girano in base alla velocità.
- **Manubrio** con le manopole in gomma e un campanello d'ottone.
- **Pedali** che girano mentre pedali.
- **Sella** in cuoio, alta giusta per il giocatore.

### Come si usa
1. **Salire**: tasto destro sulla bici.
2. **Campanello**: Shift + tasto destro.
3. **Velocità**: base 0,42, veloce e reattiva su strade e piste ciclabili.
4. **Riprenderla**: colpiscila e torna nel tuo inventario come oggetto.

---

## 2. Il treno Tel Aviv - Gerusalemme

- Le fermate (`transport_stop`) piazzate a Tel Aviv o a Gerusalemme (nella città generata o nel suo bioma) diventano stazioni della linea Israel Railways.
- Tasto destro su una fermata per andare alla stazione più vicina dall'altra parte. Se lì non c'è ancora una stazione, arrivi nella città generata più vicina.
- Il biglietto costa 10 Shekel, oppure niente con la Rav-Kav.
- Tra una corsa e l'altra ci sono 5 secondi di attesa.
- Non c'è un treno vero sui binari: è un teletrasporto con una breve dissolvenza.

---

## 3. La Rav-Kav (`rav_kav`)

La Rav-Kav (`israel_simulator:rav_kav`) è la tessera dei mezzi pubblici:
- **Tasto destro** con la tessera in mano per vedere se è attiva.
- **Con la Rav-Kav il treno è gratis**, niente Shekel.

---

## 4. Le scarpe da camminata (`walking_shoes`)

Le scarpe da camminata (`israel_simulator:walking_shoes`) sono per le lunghe camminate:
- **Effetto**: finché le hai nell'inventario hai Velocità I.
- **Quando servono**: per attraversare le alture di Gerusalemme e il deserto fino al Mar Morto.
