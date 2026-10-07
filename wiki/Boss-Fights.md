# Boss Fights — Il Boss Satirico Bibi e il Miniboss Donald Trump

In conformità con **GAME_DESIGN.md §41–44** e le regole etiche di **AGENTS.md §22**, il boss Bibi e il miniboss Donald Trump sono figure fantasy satiriche e parodistiche di finzione videoludica.

---

## 1. Come Evocare il Boss (Stella di David)

Il premier satirico Bibi non spawna casualmente nel mondo, ma deve essere invocato tramite un rituale volontario:
- **Oggetto Richiesto**: **Stella di David Cerimoniale (`israel_simulator:star_of_david`)**.
- **Procedura**: Cliccare con il tasto destro impugnando la Stella di David. Se non ci sono altri boss già attivi nell'area, il boss Bibi verrà generato accompagnato da tuoni, particelle oscure e un annuncio in chat per tutti i giocatori nel raggio di 96 blocchi.
- **Protezione Anti-Exploit**: È vietato lo spawn multiplo simultaneo all'interno della stessa arena.

---

## 2. Fase 1: Il Leader Storico (`bibi_boss`)


- **Salute Massima**: 10.000 HP (configurabile) con Boss Bar blu a 10 tacche.
- **Resistenza Anti-Cheese**: Tetto massimo di 500 danni per colpo singolo (`MAX_SINGLE_HIT_DAMAGE`) per prevenire one-shot exploit.
- **Attacchi e Meccaniche della Fase 1**:
  1. **Attacco Missilistico Mirato (`BibiMissileEntity`)**: Spara un missile tattico ad alta velocità direttamente contro il giocatore, dotato di manovre di tracking a inseguimento morbido. All'impatto esplode danneggiando il bersaglio e l'area circostante.
  2. **Bombardamento Aereo ad Area**: Fa piovere dal cielo una salva di 8 missili tattici su un largo raggio attorno a sé, bombardando, incendiando e distruggendo la vegetazione e l'ambiente circostante.
  3. **Onda d'Urto del Filibustiere**: Un'onda sonica d'urto ad area che respinge con forza i giocatori e infligge l'effetto Lentezza (la cecità è stata rimossa per garantire migliore visibilità).
  4. **Guardie d'Élite della Coalizione (`bibi_guard`)**: Evocate a frequenza ridotta, equipaggiate con pistole di sicurezza (`pistol`) con cui bersagliano i giocatori a distanza.

---

## 3. Fase 2: Miniboss Donald Trump e Scudo Diplomatico (50% HP)

Quando la salute di Bibi scende al **50% HP** (`ENRAGE_HEALTH_FRACTION = 0.50F`):
- Bibi entra in modalità **INFURIATO** (Boss bar rossa a 10 tacche).
- **Aumento del Ritmo di Combattimento**:
  - L'attacco missilistico mirato raddoppia i proiettili sparando due missili in salva rapida.
  - Il bombardamento aereo ha un tempo di ricarica dimezzato (da 180 a 90 tick) e scatena ben 16 missili dal cielo.
- **Spawn del Miniboss Donald Trump (`trump_miniboss`)**: Le guardie standard cessano di spawnare; al loro posto scende in campo il miniboss alleato Donald Trump (1.500 HP, Boss bar dorata/gialla a 6 tacche).
- **Scudo di Immunità Totale**: Finché Donald Trump è in vita, Bibi è protetto da un'immunità totale indistruttibile. Qualsiasi attacco rivolto a Bibi viene bloccato (particelle Totem e suono scudo).
- **Attacchi del Miniboss Donald Trump**:
  1. **Enorme Muro di Soldi ("Wall of Money")**: Un imponente muro di banconote e monete che travolge i giocatori a cono aperto, infliggendo ingente danno, respingimento e **distruggendo direttamente la durabilità dell'armatura** indossata (35 punti di durabilità per pezzo). Attacco bilanciato da un lungo tempo di ricarica.
  2. **Chiamata ICE ("Call ICE")**: Suona l'allarme ed evoca 3 agenti federali ICE d'élite muniti di **armatura completa in Netherite e spada in Netherite**.
- **Infrangimento dello Scudo**: Alla morte di Donald Trump, lo scudo di Bibi si spezza con effetto sonoro e visivo di scudo rotto, rendendolo nuovamente vulnerabile agli attacchi per concludere la battaglia.

---

## 4. Arsenale Balistico del Giocatore

Nel combattimento contro le forze nemiche, i giocatori possono fabbricare e impiegare armi da fuoco e da lancio dedicate:
- **Pistola di Sicurezza (`israel_simulator:pistol`)**:
  - Arma da fuoco di precisione a raggio immediato (raycast balistico su 32 blocchi).
  - Infligge **9.0 cuori di danno** al bersaglio colpito. Durabilità: 250 colpi. Incantabile con *Unbreaking* e *Mending*.
- **Missile Tattico Tascabile (`israel_simulator:missile`)**:
  - Lanciabile direttamente dall'inventario per innescare un proiettile a razzo a impatto esplosivo.

---

## 5. Ricompense e Drop della Boss Fight

Sconfiggere il Boss Bibi sul server garantisce ricompense leggendarie e prestigiose:
- **Disco Musicale Hava Nagila (`hava_nagila_disc`)**: Ricompensa LEGGENDARIA garantita al 100%, riproducibile nel Jukebox.
- **48x Shekel (`shekel`)** & **12x Lingotti d'Oro**: Valuta e metalli preziosi.
- **6x Ancient Coins (`ancient_coin`)**: Monete antiche rare per il collezionismo e il commercio specializzato.
- **5x Diamanti**: Risorse per equipaggiamento endgame.
- **Avanzamento "Hava Nagila"**: Sbloccato per tutti i partecipanti che hanno contribuito a infliggere danni nella battaglia.


