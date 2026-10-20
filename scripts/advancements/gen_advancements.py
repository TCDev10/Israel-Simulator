#!/usr/bin/env python3
"""Generates every advancement JSON under data/israel_simulator/advancement and their
en_us / it_it lang keys. Edit the ADVANCEMENTS table and rerun:

    python3 scripts/advancements/gen_advancements.py

Old files in the advancement folder are removed first, so the folder always matches the table.
"""
import json
import os
import shutil

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ADV_DIR = os.path.join(ROOT, "src/main/resources/data/israel_simulator/advancement")
LANG_DIR = os.path.join(ROOT, "src/main/resources/assets/israel_simulator/lang")
NS = "israel_simulator"
KEY = "advancements.israel_simulator."


def mod(path):
    return path if ":" in path else f"{NS}:{path}"


# ---- criteria -------------------------------------------------------------------------------

def player_pred(predicate, *extra):
    return [{"condition": "minecraft:entity_properties", "entity": "this", "predicate": predicate}, *extra]


def in_structure(structure, *extra):
    return {"trigger": "minecraft:location",
            "conditions": {"player": player_pred({"minecraft:location": {"structures": mod(structure)}}, *extra)}}


def in_biome(biome):
    return {"trigger": "minecraft:location",
            "conditions": {"player": player_pred({"minecraft:location": {"biomes": mod(biome)}})}}


def wearing(**slots):
    return {"trigger": "minecraft:location",
            "conditions": {"player": player_pred({"minecraft:equipment": {s: {"items": mod(i)} for s, i in slots.items()}})}}


def has(item, count=None):
    pred = {"items": mod(item)}
    if count:
        pred["count"] = {"min": count}
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [pred]}}


def eat(item):
    return {"trigger": "minecraft:consume_item", "conditions": {"item": {"items": mod(item)}}}


def kill(entity):
    return {"trigger": "minecraft:player_killed_entity",
            "conditions": {"entity": [{"condition": "minecraft:entity_properties", "entity": "this",
                                       "predicate": {"minecraft:entity_type": mod(entity)}}]}}


def trade(villager, item=None, player=None):
    c = {"villager": [{"condition": "minecraft:entity_properties", "entity": "this",
                       "predicate": {"minecraft:entity_type": mod(villager)}}]}
    if item:
        c["item"] = {"items": mod(item)}
    if player:
        c["player"] = player
    return {"trigger": "minecraft:villager_trade", "conditions": c}


def effect(eff):
    return {"trigger": "minecraft:effects_changed", "conditions": {"effects": {mod(eff): {}}}}


def event(name):
    return {"trigger": f"{NS}:mod_event", "conditions": {"event": name}}


NIGHT = {"condition": "minecraft:time_check", "clock": "minecraft:overworld", "period": 24000,
         "value": {"min": 13000, "max": 23000}}

FESTIVALS = ["shabbat", "rosh_hashanah", "yom_kippur", "sukkot", "hanukkah", "purim", "pesach"]
MOD_FOODS = ["tahini", "dates", "grapes", "olives", "citrus", "chickpeas", "tomato", "eggplant", "sesame_seeds",
             "challah", "rugelach", "falafel", "hummus", "shakshuka", "sabich", "matzo", "sufganiyah", "hamantash"]
EXPLORER_STRUCTURES = ["jerusalem_city", "western_wall", "grand_market", "great_synagogue", "tel_aviv_city",
                       "jaffa_port", "government_building", "startup_office", "twin_towers", "gaza_strip",
                       "ein_gedi_oasis", "ancient_sanctuary", "dead_sea_resort", "agricultural_farm",
                       "mediterranean_village", "tropical_island"]

AND = "and"
OR = "or"

# ---- the tree -------------------------------------------------------------------------------
# (folder/name, parent name, icon, frame, hidden, criteria dict, AND|OR, xp,
#  en title, en description, it title, it description)

A = []


def adv(path, parent, icon, frame, criteria, mode, en, it, hidden=False, xp=0):
    A.append(dict(path=path, parent=parent, icon=icon, frame=frame, criteria=criteria, mode=mode,
                  en=en, it=it, hidden=hidden, xp=xp))


# Root
adv("exploration/welcome_to_israel", None, "israel_map", "task",
    {"in_israel": {"trigger": "minecraft:location", "conditions": {"player": player_pred(
        {"minecraft:location": {"biomes": f"#{NS}:is_israel_region"}})}}}, OR,
    ("Welcome to Israel", "Set foot in one of Israel's biomes"),
    ("Benvenuto in Israele", "Metti piede in uno dei biomi d'Israele"))

# Jerusalem
adv("exploration/visit_jerusalem", "welcome_to_israel", "jerusalem_stone", "task",
    {"jerusalem": in_biome("jerusalem")}, OR,
    ("Visit Jerusalem", "Reach the hills of Jerusalem"),
    ("Visita Gerusalemme", "Raggiungi le colline di Gerusalemme"))
adv("exploration/western_wall", "visit_jerusalem", "western_wall_stone", "task",
    {"western_wall": in_structure("western_wall")}, OR,
    ("The Kotel", "Stand before the Western Wall"),
    ("Il Kotel", "Arriva davanti al Muro del Pianto"))
adv("economy/five_diamonds", "western_wall", "minecraft:diamond", "goal",
    {"prayed": event("western_wall_prayer")}, OR,
    ("Five Diamonds", "Pray at the Western Wall with a Kippah and a Prayer Note"),
    ("Cinque Diamanti", "Prega al Muro del Pianto con la Kippah e un Biglietto di preghiera"))
adv("exploration/grand_market", "visit_jerusalem", "olives", "task",
    {"grand_market": in_structure("grand_market")}, OR,
    ("Shuk Day", "Get lost in the Grand Market"),
    ("Giornata al Shuk", "Perditi nel Grande mercato"))
adv("economy/shuk_haggler", "grand_market", "citrus", "task",
    {"traded": trade("shuk_vendor")}, OR,
    ("Best Price for You, My Friend", "Buy something from a Shuk vendor"),
    ("Prezzo Speciale per Te, Amico", "Compra qualcosa da un venditore dello Shuk"))
adv("cultural/ark_prayer", "visit_jerusalem", "prayer_note", "task",
    {"prayed": event("ark_prayer")}, OR,
    ("Before the Ark", "Pray at the Torah Ark of a synagogue"),
    ("Davanti all'Arca", "Prega davanti all'Arca santa di una sinagoga"))

# Tel Aviv and the coast
adv("exploration/tel_aviv", "welcome_to_israel", "smartphone", "task",
    {"tel_aviv": in_structure("tel_aviv_city")}, OR,
    ("The Bubble", "Arrive in Tel Aviv"),
    ("La Bolla", "Arriva a Tel Aviv"))
adv("exploration/tel_aviv_nights", "tel_aviv", "falafel", "task",
    {"tel_aviv_at_night": in_structure("tel_aviv_city", NIGHT)}, OR,
    ("Tel Aviv Nights", "Be in Tel Aviv after dark. The city never sleeps"),
    ("Notti a Tel Aviv", "Resta a Tel Aviv dopo il tramonto. La città non dorme mai"))
adv("exploration/government_building", "tel_aviv", "first_amendment", "task",
    {"government": in_structure("government_building")}, OR,
    ("Coalition Talks", "Walk into the Government Building"),
    ("Trattative di Coalizione", "Entra nel Palazzo del governo"))
adv("exploration/twin_towers", "tel_aviv", "minecraft:glass", "task",
    {"twin_towers": in_structure("twin_towers")}, OR,
    ("Skyline", "Find the Twin Towers"),
    ("Skyline", "Trova le Twin Towers"))
adv("events/plane_crash", "twin_towers", "missile", "goal",
    {"witnessed": event("plane_crash")}, OR,
    ("Brace for Impact", "Be near the Twin Towers when a plane hits them"),
    ("Prepararsi all'Impatto", "Trovati vicino alle Twin Towers quando un aereo le colpisce"))
adv("exploration/jaffa", "tel_aviv", "citrus", "task",
    {"jaffa_port": in_structure("jaffa_port")}, OR,
    ("Jaffa Oranges", "Visit the old port of Jaffa"),
    ("Arance di Giaffa", "Visita il porto antico di Giaffa"))
adv("exploration/gaza_strip", "jaffa", "minecraft:sandstone_wall", "task",
    {"gaza": in_structure("gaza_strip")}, OR,
    ("Behind the Fence", "Enter the Gaza Strip"),
    ("Oltre la Recinzione", "Entra nella Striscia di Gaza"))
adv("exploration/tropical_island", "jaffa", "dates", "task",
    {"island": in_structure("tropical_island")}, OR,
    ("Private Island", "Land on the tropical island. Don't ask about the guest list"),
    ("Isola Privata", "Sbarca sull'isola tropicale. Non chiedere della lista degli ospiti"))

# Desert and Dead Sea
adv("exploration/judean_desert", "welcome_to_israel", "minecraft:sand", "task",
    {"desert": in_biome("judean_desert")}, OR,
    ("Into the Wilderness", "Enter the Judean Desert. Bring water"),
    ("Nel Deserto", "Entra nel Deserto della Giudea. Porta dell'acqua"))
adv("exploration/ein_gedi", "judean_desert", "date_palm_leaves", "task",
    {"ein_gedi": in_structure("ein_gedi_oasis")}, OR,
    ("Oasis", "Find the Ein Gedi oasis"),
    ("Oasi", "Trova l'oasi di Ein Gedi"))
adv("exploration/ancient_sanctuary", "judean_desert", "dead_sea_scroll_fragment", "task",
    {"sanctuary": in_structure("ancient_sanctuary")}, OR,
    ("Lost Sanctuary", "Discover the Ancient Sanctuary in the desert"),
    ("Santuario Perduto", "Scopri il Santuario antico nel deserto"))
adv("exploration/dead_sea_tourist", "judean_desert", "dead_sea_mud", "goal",
    {"floated": event("dead_sea_float"), "mud_bath": event("mud_bath")}, AND,
    ("Dead Sea Tourist", "Float on the Dead Sea and soak in a mud bath"),
    ("Turista del Mar Morto", "Galleggia sul Mar Morto e fai un bagno di fango"))
adv("exploration/dead_sea_souvenirs", "dead_sea_tourist", "salt_block", "task",
    {"mud": has("dead_sea_mud"), "salt": has("salt_block")}, AND,
    ("Salt and Mud", "Take home Dead Sea Mud and a Salt Block"),
    ("Sale e Fango", "Porta a casa il Fango del Mar Morto e un Blocco di sale del Mar Morto"))

adv("exploration/master_explorer", "welcome_to_israel", "walking_shoes", "challenge",
    {s: in_structure(s) for s in EXPLORER_STRUCTURES}, AND,
    ("Master Explorer", "Visit every major place in Israel, from the Kotel to the Gaza Strip"),
    ("Grande Esploratore", "Visita tutti i luoghi principali d'Israele, dal Kotel alla Striscia di Gaza"),
    xp=500)

# Culture and wearables
adv("cultural/shalom", "welcome_to_israel", "kippah", "task",
    {"kippah": wearing(head="kippah")}, OR,
    ("Shalom!", "Put on a Kippah"),
    ("Shalom!", "Indossa una Kippah"))
adv("cultural/dressed_for_prayer", "shalom", "talit", "task",
    {"dressed": wearing(head="kippah", chest="talit", legs="tefillin")}, OR,
    ("Fully Dressed", "Wear a Kippah, a Talit and Tefillin at the same time"),
    ("Vestito di Tutto Punto", "Indossa insieme Kippah, Talit e Tefillin"))
adv("cultural/tefillin_prayer", "dressed_for_prayer", "tefillin", "task",
    {"bound": event("tefillin_bound")}, OR,
    ("Morning Prayer", "Bind the Tefillin during the day while wearing a Kippah"),
    ("Preghiera del Mattino", "Lega i Tefillin di giorno indossando la Kippah"))
adv("cultural/shofar", "shalom", "shofar", "task",
    {"blown": event("shofar_blow")}, OR,
    ("Sound the Shofar", "Blow a Shofar"),
    ("Suona lo Shofar", "Suona uno Shofar"))
adv("cultural/freedom_of_speech", "shalom", "first_amendment", "goal",
    {"has_first_amendment": has("first_amendment")}, OR,
    ("Freedom of Speech", "Grab the First Amendment at the end of the public speech"),
    ("Libertà di Parola", "Prendi il Primo emendamento alla fine del discorso in piazza"))
adv("secret/secret_kippah_cat", "shalom", "kippah", "task",
    {"cat_approves": event("kippah_cat")}, OR,
    ("Honorary Feline Citizen", "Show your Kippah to a cat"),
    ("Gatto Onorario", "Mostra la tua Kippah a un gatto"),
    hidden=True)

# Shabbat and festivals
adv("festival/shabbat_shalom", "welcome_to_israel", "challah", "task",
    {"rested": event("shabbat_rest")}, OR,
    ("Shabbat Shalom", "Rest on Shabbat: no breaking, building or fighting, and get the rest bonus"),
    ("Shabbat Shalom", "Riposati di Shabbat: niente scavare, costruire o combattere, e ricevi il bonus riposo"))
adv("secret/not_on_shabbat", "shabbat_shalom", "laptop", "task",
    {"tried": event("shabbat_work_blocked")}, OR,
    ("Not Today", "Try to work on Shabbat"),
    ("Non Oggi", "Prova a lavorare di Shabbat"),
    hidden=True)
adv("festival/hanukkah", "shabbat_shalom", "menorah", "task",
    {"hanukkah": event("festival_hanukkah"), "sufganiyah": eat("sufganiyah")}, AND,
    ("Festival of Lights", "Be around for Hanukkah and eat a Sufganiyah"),
    ("Festa delle Luci", "Vivi Hanukkah e mangia una Sufganiyah"))
adv("festival/purim", "shabbat_shalom", "hamantash", "task",
    {"purim": event("festival_purim"), "hamantash": eat("hamantash")}, AND,
    ("Chag Purim Sameach", "Be around for Purim and eat a Hamantash"),
    ("Chag Purim Sameach", "Vivi Purim e mangia un Hamantash"))
adv("festival/pesach", "shabbat_shalom", "matzo", "task",
    {"pesach": event("festival_pesach"), "matzo": eat("matzo")}, AND,
    ("Let My People Go", "Be around for Pesach and eat Matzo"),
    ("Lascia Andare il Mio Popolo", "Vivi Pesach e mangia la Matzah"))
adv("festival/all_festivals", "hanukkah", "shofar", "challenge",
    {f: event("festival_" + f) for f in FESTIVALS}, AND,
    ("A Year in Israel", "Live through Shabbat and every festival: Rosh Hashanah, Yom Kippur, Sukkot, Hanukkah, Purim and Pesach"),
    ("Un Anno in Israele", "Vivi lo Shabbat e tutte le feste: Rosh Hashanah, Yom Kippur, Sukkot, Hanukkah, Purim e Pesach"),
    xp=300)

# Money
adv("economy/first_shekel", "welcome_to_israel", "shekel", "task",
    {"shekel": has("shekel")}, OR,
    ("Your First Shekel", "Get a Shekel"),
    ("Il Tuo Primo Shekel", "Procurati uno Shekel"))
adv("economy/small_change", "first_shekel", "agora", "task",
    {"agora": has("agora")}, OR,
    ("Small Change", "Find an Agora. A hundred make a Shekel"),
    ("Spiccioli", "Trova un'Agorà. Ne servono cento per fare uno Shekel"))
adv("economy/money_changer", "first_shekel", "ancient_coin", "task",
    {"traded": trade("money_changer")}, OR,
    ("Exchange Rate", "Trade with a Money Changer"),
    ("Tasso di Cambio", "Fai uno scambio con un Cambiavalute"))

# Bosses
adv("combat/summon_bibi", "welcome_to_israel", "star_of_david", "task",
    {"summoned": event("bibi_summon")}, OR,
    ("He's Back", "Summon Bibi with the Star of David"),
    ("È Tornato", "Evoca Bibi con la Stella di Davide"))
adv("combat/hava_nagila", "summon_bibi", "hava_nagila_disc", "challenge",
    {"kill_bibi_boss": kill("bibi_boss"), "has_disc": has("hava_nagila_disc")}, AND,
    ("Hava Nagila", "Defeat Bibi and pick up the Hava Nagila disc"),
    ("Hava Nagila", "Sconfiggi Bibi e prendi il disco Hava Nagila"),
    xp=500)
adv("economy/blessed_trader", "hava_nagila", "rabbis_crown", "challenge",
    {"crowned_trade": {"trigger": "minecraft:villager_trade", "conditions": {
        "player": player_pred({"minecraft:equipment": {"head": {"items": mod("rabbis_crown")}}})}}}, OR,
    ("Blessed Trader", "Trade with a villager while wearing the Rabbi's Crown"),
    ("Mercante Benedetto", "Fai uno scambio con un abitante del villaggio indossando la Corona del rabbino"),
    xp=200)
adv("combat/kill_trump", "summon_bibi", "minecraft:golden_helmet", "goal",
    {"trump": kill("trump_miniboss")}, OR,
    ("You're Fired!", "Defeat Donald Trump during the Bibi fight"),
    ("Sei Licenziato!", "Sconfiggi Donald Trump durante lo scontro con Bibi"))
adv("combat/kill_epstein", "summon_bibi", "minecraft:paper", "challenge",
    {"epstein": kill("jeffrey_epstein")}, OR,
    ("Case Closed", "Defeat Jeffrey Epstein. This time it's for real"),
    ("Caso Chiuso", "Sconfiggi Jeffrey Epstein. Stavolta sul serio"),
    hidden=True, xp=300)

# Mossad
adv("mossad/mossad_recruit", "welcome_to_israel", "sealed_dossier", "task",
    {"mission": event("mossad_mission")}, OR,
    ("Recruited", "Complete a mission for the Mossad Handler"),
    ("Reclutato", "Completa una missione per il referente del Mossad"))
adv("mossad/counterintelligence", "mossad_recruit", "pistol", "task",
    {"agent": kill("mossad_agent")}, OR,
    ("Counterintelligence", "Take down a hostile Mossad agent"),
    ("Controspionaggio", "Elimina un agente del Mossad ostile"))
adv("mossad/mossad_veteran", "mossad_recruit", "assault_rifle", "goal",
    {"veteran": event("mossad_veteran")}, OR,
    ("Katsa", "Complete 5 Mossad missions"),
    ("Katsa", "Completa 5 missioni del Mossad"))

# Startup Nation
adv("technology/startup_office", "welcome_to_israel", "drone_part", "task",
    {"office": in_structure("startup_office")}, OR,
    ("Startup Nation", "Visit a Startup Office"),
    ("Nazione Startup", "Visita l'Ufficio di una startup"))
adv("technology/startup_founder", "startup_office", "laptop", "goal",
    {"work_session": event("work_session"), "delivery_gig": event("delivery_gig")}, OR,
    ("Startup Founder", "Work a session on the Laptop or do a delivery with the Smartphone"),
    ("Fondatore di Startup", "Fai una sessione di lavoro col Portatile o una consegna con lo Smartphone"))
adv("technology/startup_exit", "startup_founder", "laptop", "challenge",
    {"exit": event("startup_exit")}, OR,
    ("Exit!", "Work your startup all the way to an exit"),
    ("Exit!", "Porta la tua startup fino all'exit"),
    xp=300)
adv("technology/drone_launch", "startup_office", "palantrio_drone", "task",
    {"launched": event("drone_launch")}, OR,
    ("Eye in the Sky", "Launch a Palantrio Drone"),
    ("Occhio dal Cielo", "Lancia un Palantrio Drone"))

# Transport
adv("transport/first_train", "welcome_to_israel", "transport_stop", "task",
    {"ride": event("train_ride")}, OR,
    ("All Aboard", "Take the train between Tel Aviv and Jerusalem"),
    ("Tutti a Bordo", "Prendi il treno tra Tel Aviv e Gerusalemme"))
adv("transport/rav_kav", "first_train", "rav_kav", "task",
    {"rav_kav_ride": event("train_ride_rav_kav")}, OR,
    ("Tap and Go", "Ride the train with a Rav-Kav"),
    ("Appoggia e Vai", "Viaggia in treno con la Rav-Kav"))

# Farm and food
adv("food/moshav", "welcome_to_israel", "chickpeas", "task",
    {"farm": in_structure("agricultural_farm")}, OR,
    ("Moshav Life", "Visit a farm in the Israeli Farmland"),
    ("Vita da Moshav", "Visita una fattoria nelle campagne israeliane"))
adv("food/harvest", "moshav", "eggplant", "task",
    {c: has(c) for c in ["chickpeas", "tomato", "eggplant", "sesame_seeds"]}, AND,
    ("Fruits of the Land", "Collect chickpeas, tomatoes, eggplants and sesame seeds"),
    ("Frutti della Terra", "Raccogli ceci, pomodori, melanzane e semi di sesamo"))
adv("food/hummus_connoisseur", "harvest", "hummus", "goal",
    {f: eat(f) for f in ["hummus", "falafel", "shakshuka", "sabich"]}, AND,
    ("Hummus Connoisseur", "Eat Hummus, Falafel, Shakshuka and Sabich"),
    ("Intenditore di Hummus", "Mangia Hummus, Falafel, Shakshuka e Sabich"))
adv("food/taste_of_israel", "hummus_connoisseur", "sabich", "challenge",
    {f: eat(f) for f in MOD_FOODS}, AND,
    ("Taste of Israel", "Eat every Israeli food in the mod"),
    ("Sapore d'Israele", "Mangia tutti i cibi israeliani della mod"),
    xp=300)


# ---- writer ---------------------------------------------------------------------------------

def name_of(path):
    return path.split("/")[-1]


def build():
    by_name = {}
    for a in A:
        n = name_of(a["path"])
        assert n not in by_name, "duplicate advancement name " + n
        by_name[n] = a
    out = {}
    for a in A:
        n = name_of(a["path"])
        display = {
            "icon": {"id": mod(a["icon"])},
            "title": {"translate": KEY + n + ".title"},
            "description": {"translate": KEY + n + ".description"},
            "frame": a["frame"],
            "show_toast": True,
            "announce_to_chat": a["parent"] is not None,
            "hidden": a["hidden"],
        }
        j = {}
        if a["parent"] is None:
            display = {"background": f"{NS}:gui/advancements/backgrounds/israel_flag", **display}
        else:
            j["parent"] = f"{NS}:" + by_name[a["parent"]]["path"]
        j["display"] = display
        j["criteria"] = a["criteria"]
        keys = list(a["criteria"])
        j["requirements"] = [keys] if a["mode"] == OR else [[k] for k in keys]
        if a["xp"]:
            j["rewards"] = {"experience": a["xp"]}
        out[a["path"]] = j
    return out


def main():
    advs = build()
    if os.path.isdir(ADV_DIR):
        shutil.rmtree(ADV_DIR)
    for path, j in advs.items():
        f = os.path.join(ADV_DIR, path + ".json")
        os.makedirs(os.path.dirname(f), exist_ok=True)
        with open(f, "w", encoding="utf-8") as fh:
            json.dump(j, fh, indent=2, ensure_ascii=False)
            fh.write("\n")
    for lang, idx in (("en_us", "en"), ("it_it", "it")):
        f = os.path.join(LANG_DIR, lang + ".json")
        with open(f, encoding="utf-8") as fh:
            data = json.load(fh)
        data = {k: v for k, v in data.items() if not k.startswith(KEY)}
        for a in A:
            n = name_of(a["path"])
            title, desc = a[idx]
            data[KEY + n + ".title"] = title
            data[KEY + n + ".description"] = desc
        with open(f, "w", encoding="utf-8") as fh:
            json.dump(data, fh, indent=2, ensure_ascii=False)
            fh.write("\n")
    print(f"{len(advs)} advancements written")


if __name__ == "__main__":
    main()
