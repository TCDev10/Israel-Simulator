# Economia e Commercio (Economy & Trading)

La struttura economica di **Israel-Simulator** riflette la vita urbana, rurale e storica d'Israele, con valute dedicate, fluttuazioni di mercato regionali, reputazione con le fazioni e commercio specializzato.

---

## 1. Il Sistema Monetario

Il mod introduce due valute ufficiali e una valuta archeologica/collezionabile:

| Valuta | Nome Item | Valore Base | Descrizione e Utilizzo |
| :--- | :--- | :--- | :--- |
| **Shekel (ILS)** | `israel_simulator:shekel` | 100 Agorot | La valuta principale per acquisti di beni urbani, cibo, tecnologia e Judaica. |
| **Agora** | `israel_simulator:agora` | 1/100 Shekel | Frazione monetaria usata per micropagamenti, resto agricolo e vendita di materie prime. |
| **Ancient Coin** | `israel_simulator:ancient_coin` | ~150 ILS (Rara) | Moneta antica d'alto valore storico per antiquari, collezionisti e mercati storici. |

---

## 2. A Cosa Serve l'Agora (`agora`)?

L'**Agora** è la moneta di taglio piccolo:
- **Come si guadagna**:
  1. Vendendo raccolti agricoli nei Kibbutz / villaggi della Galilea (`AgriculturalTrades`).
  2. Vendendo lana ai nomadi del Deserto della Giudea (4 lana $\rightarrow$ 2 Agorot).
  3. Vendendo fango e sali del Mar Morto ai commercianti (`DeadSeaTrades`).
- **Come si usa e si converte**:
  1. **Cambio presso gli NPC (100 Agorot = 1 Shekel)**: Cliccando con il tasto destro su qualsiasi NPC commerciante tenendo uno stack di almeno 100 Agorot, l'NPC le cambierà automaticamente in Shekel.
  2. **Spezzare uno Shekel in Agorot**: Facendo Shift + Click destro su un NPC tenendo 1 Shekel, riceverai 100 Agorot di resto.
  3. **Commercio Diretto e Acquisti**: Diversi beni e tariffe di base possono essere acquistati direttamente con le monete accumulate.

---

## 3. A Cosa Serve l'Ancient Coin (`ancient_coin`)?

L'**Ancient Coin** (Moneta Antica) è un reperto archeologico prezioso di rarità **RARE**:
- **Come si trova**:
  1. **Drop del Boss Bibi**: Sconfiggere il boss satirico garantisce **6 Ancient Coins**.
  2. **Rovine e Santuari Archeologici**: Forzieri nei templi del deserto della Giudea, ad Ein Gedi e nei siti storici.
- **Come si monetizza**:
  1. **Mercato delle Pulci di Giaffa (Shuk HaPishpeshim)**: I mercanti di antiquariato acquistano ogni Ancient Coin per **6 Shekel**.
  2. **Commercianti Beduini del Deserto**: Comprano ogni Ancient Coin per **5 Shekel**.
  3. **NPC Storici / Investitori / Commercianti**: Vendendola tramite il sistema economico dinamico (`IsraelEconomy`) a storici e mercanti d'antiquariato, puoi guadagnare fino a oltre **100 Shekel** a moneta!
  4. **Commerciante Benedetto (Blessed Trader)**: I commercianti benedetti vendono o richiedono monete antiche per scambi sacri rari.

---

## 4. Mercati Speciali e Commercio Regionale

### Mercato di Giaffa (Jaffa Flea Market & Port)
- **Ancient Coin $\rightarrow$ 6 Shekel**
- **Olive Wood Carving $\rightarrow$ 8 Shekel**
- **1 Shekel (in crouch) $\rightarrow$ 6 Arance di Giaffa (Citrus)**
- **1 Shekel $\rightarrow$ 4 Merluzzi cotti freschi**

### Commercio nel Deserto della Giudea
- **10 Shekel $\rightarrow$ Sella per cammello/cavallo**
- **8 Datteri $\rightarrow$ 1 Shekel**
- **4 Lana $\rightarrow$ 2 Agorot**
- **1 Ancient Coin $\rightarrow$ 5 Shekel**
- **1 Frammento dei Rotoli del Mar Morto $\rightarrow$ 10 Shekel**

### Commercio Agricolo (Kibbutz & Fattorie)
- Acquisto di grano, barbabietole, olive, datteri ed erbe aromatiche con payout istantaneo in Shekel e Agorot.
