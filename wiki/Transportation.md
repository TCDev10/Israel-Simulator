# Trasporti e Mobilità (Transportation)

Il sistema dei trasporti di **Israel-Simulator** risponde al pilastro di design **GAME_DESIGN.md §32**: connettere le città e i biomi del mondo senza frammentare l'esplorazione.

---

## 1. La Bicicletta (Bicycle)

La **Bicicletta** (`israel_simulator:bicycle`) è il mezzo di trasporto ecologico ideale per muoversi agilmente nelle strade di Tel Aviv, sul lungomare di Giaffa e sui sentieri regionali.

### Modello 3D e Aspetto
- **Telaio 3D personalizzato**: Tubo superiore, tubo diagonale, piantone sella e foderi in alluminio/acciaio color verde smeraldo/turchese (stile bike sharing di Tel Aviv).
- **Ruote animate**: Ruota anteriore e posteriore complete di battistrada scuro e raggi che ruotano dinamicamente in base alla velocità del veicolo.
- **Manubrio e Campanello**: Manopole ergonomiche in gomma e un campanello in ottone lucido dorato.
- **Pedali funzionanti**: Pedivelle e pedali che ruotano durante la pedalata.
- **Sella in cuoio**: Posizionamento della seduta studiato per allineare realisticamente il giocatore in sella.

### Meccaniche di Gioco
1. **Salire in sella**: Cliccare con il tasto destro sulla bicicletta per montare a bordo.
2. **Suonare il campanello**: Premere **Shift + Tasto Destro** (o Shift mentre si è vicini) per suonare il campanello personalizzato (`ModSoundEvents.BICYCLE_BELL`), allertando pedoni e NPC.
3. **Velocità e Guida**: Movimento fluido e reattivo (velocità base 0.42), ideale per percorrere le piste ciclabili cittadine e i viali alberati.
4. **Recupero del veicolo**: Colpendo la bicicletta, essa viene distrutta e rilascia nuovamente l'item `Bicycle` nel tuo inventario.

---

## 2. Carta Elettronica Rav-Kav (`rav_kav`)

La **Carta Rav-Kav** (`israel_simulator:rav_kav`) è la tessera di viaggio multi-operatore per i trasporti pubblici:
- **Utilizzo**: Cliccando con il tasto destro con la carta in mano, viene mostrato il resoconto dello stato dell'abbonamento ("Rav-Kav Transit Card active — Unlimited nationwide travel pass").
- **Funzione di Gioco**: Permette l'accesso senza costi di Shekel alle fermate dei bus e ai vagoni delle linee di transito rapido tra Tel Aviv, Gerusalemme e i principali hub del paese.

---

## 3. Scarpe da Camminata (`walking_shoes`)

Le **Scarpe da Camminata** (`israel_simulator:walking_shoes`) sono calzature da trekking resistenti progettate per le lunghe traversate a piedi:
- **Effetto Passivo**: Quando equipaggiate negli slot attivi o tenute a portata di mano, conferiscono un incremento continuo della velocità di movimento (`Speed I`).
- **Scopo**: Perfette per esplorare le alture di Gerusalemme e il deserto della Giudea fino al Mar Morto senza affaticarsi.


