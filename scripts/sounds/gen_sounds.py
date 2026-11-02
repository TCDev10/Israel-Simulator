"""Procedural, original sound effects and music for Israel Simulator (no samples, no recordings)."""
import numpy as np, subprocess, os, sys
from scipy import signal
SR = 44100
OUT = sys.argv[1]
rng = np.random.default_rng(777)

def t_(d): return np.arange(int(SR*d))/SR
def env(n, a=0.01, r=0.1, d=None):
    e = np.ones(n); A=int(a*SR); R=int(r*SR)
    if A: e[:A] = np.linspace(0,1,A)
    if R: e[-R:] *= np.linspace(1,0,R)
    return e
def adsr_exp(n, decay):  # percussive
    return np.exp(-np.arange(n)/SR/decay)
def bp(x, lo, hi, order=2):
    b,a = signal.butter(order, [lo/(SR/2), hi/(SR/2)], 'band'); return signal.lfilter(b,a,x)
def lp(x, f, order=2):
    b,a = signal.butter(order, f/(SR/2), 'low'); return signal.lfilter(b,a,x)
def hp(x, f, order=2):
    b,a = signal.butter(order, f/(SR/2), 'high'); return signal.lfilter(b,a,x)
def noise(d): return rng.standard_normal(int(SR*d))
def norm(x, peak=0.89):
    m = np.max(np.abs(x)); return x*(peak/m) if m>0 else x
def fade(x, a=0.005, r=0.05):
    if x.ndim==1: return x*env(len(x),a,r)
    return x*env(x.shape[0],a,r)[:,None]
def write(name, x, q=3):
    path = os.path.join(OUT, name + '.ogg'); os.makedirs(os.path.dirname(path), exist_ok=True)
    x = np.clip(x, -1, 1); ch = 1 if x.ndim==1 else 2
    pcm = (x*32767).astype('<i2').tobytes()
    subprocess.run(['ffmpeg','-v','error','-y','-f','s16le','-ar',str(SR),'-ac',str(ch),'-i','-',
                    '-c:a','libvorbis','-q:a',str(q),'-map_metadata','-1',path], input=pcm, check=True)
    print(name, ch, round(len(x)/SR,2), os.path.getsize(path))
def place(buf, x, at):
    i=int(at*SR); n=min(len(x), len(buf)-i)
    if n>0: buf[i:i+n]+=x[:n]
def bell(f, d, partials=((1,1),(2.0,.5),(2.76,.35),(4.07,.2),(5.4,.1)), decay=0.8):
    t=t_(d); y=sum(a*np.sin(2*np.pi*f*r*t)*np.exp(-t/(decay/ r**0.5)) for r,a in partials); return y
def mtof(m): return 440*2**((m-69)/12)

# ---------- voices (formant synthesis, no real speech) ----------
VOW = {'a':(800,1200,2500),'o':(500,850,2400),'u':(350,700,2300),'e':(450,1900,2600),'i':(300,2200,2900)}
def voice(d, f0, vowels, rough=0.0, breath=0.05):
    n=int(SR*d); t=np.arange(n)/SR
    f0c = f0 if np.ndim(f0) else np.full(n,f0)
    vib = 1+0.01*np.sin(2*np.pi*5.5*t)
    ph = 2*np.cumsum(f0c*vib)/SR*np.pi
    src = signal.sawtooth(ph)+ breath*rng.standard_normal(n)
    if rough: src *= 1+rough*rng.standard_normal(n).clip(-2,2)*0.5
    # split in segments per vowel
    seg = np.array_split(np.arange(n), len(vowels)); out=np.zeros(n)
    for idx,v in zip(seg, vowels):
        s = src[idx]; y=np.zeros(len(idx))
        for k,F in enumerate(VOW[v]):
            y += bp(s, F*0.85, F*1.15)*(1.0,0.6,0.3)[k]
        w = np.hanning(len(idx)+800)[400:-400] if len(idx)>800 else np.ones(len(idx))
        out[idx] += y*np.maximum(w,0.35)
    return out

def bibi():
    # ambient: two low mumbles
    for i,(f,v) in enumerate([(105,'ou a'),(98,'e o')],1):
        d=1.3; f0=np.linspace(f, f*0.9, int(SR*d))
        write(f'entity/bibi/ambient{i}', norm(fade(voice(d,f0,[c for c in v if c!=' '], breath=0.03),0.05,0.25),0.7))
    for i,(f,v) in enumerate([(150,'a'),(165,'e')],1):
        d=0.45; f0=np.linspace(f*1.2, f*0.8, int(SR*d))
        write(f'entity/bibi/hurt{i}', norm(fade(voice(d,f0,[v], rough=0.4),0.005,0.12),0.85))
    d=2.2; f0=np.geomspace(170,55,int(SR*d))
    x=voice(d,f0,['a','o','u'],rough=0.3)*np.linspace(1,0.3,int(SR*d))
    write('entity/bibi/death', norm(fade(x,0.01,0.6),0.85))
    # speech: syllable-rhythm babble, rising intonation, never real words
    d=2.6; n=int(SR*d); t=np.arange(n)/SR
    f0=120+18*np.sin(2*np.pi*0.9*t)+10*np.sin(2*np.pi*2.3*t)
    x=voice(d,f0,list('aeoiaeuoae'))
    syl=0.55+0.45*np.clip(np.sin(2*np.pi*4.2*t),0,1)
    write('entity/bibi/speech', norm(fade(x*syl,0.02,0.3),0.8))
    # enrage: layered roar with growl and noise
    d=2.4; n=int(SR*d); f0=np.concatenate([np.geomspace(70,140,n//3), np.full(n-n//3,140)])
    x=voice(d,f0,['o','a','a'],rough=0.9,breath=0.3)
    x+= 0.5*voice(d,f0*0.5,['o','a','a'],rough=0.9)
    x+= 0.3*lp(noise(d),900)*np.linspace(0.2,1,n)
    x=np.tanh(2.5*norm(x))
    write('entity/bibi/enrage', norm(fade(x,0.05,0.5),0.9))

def transport():
    # bicycle bell: two rings
    b=np.zeros(int(SR*1.3)); r=bell(2650,1.0,((1,1),(1.52,.4),(2.43,.3)),decay=0.25)
    place(b,r,0.0); place(b,r*0.9,0.28)
    write('transport/bicycle_bell', norm(fade(b,0.001,0.15),0.8))
    # bus horn: two-tone, square-ish, slightly detuned
    d=1.1; t=t_(d); e=env(len(t),0.02,0.08)
    x=sum(signal.square(2*np.pi*f*t, 0.4)*a for f,a in ((330,1),(415,0.9),(332,0.4)))
    x=lp(x,2500)*e; x=np.tanh(1.5*x)
    write('transport/bus_horn', norm(x,0.5))
    # train whistle: chord of breathy pipes, two blasts
    def blast(d):
        t=t_(d); y=sum(np.sin(2*np.pi*f*t*(1+0.003*np.sin(2*np.pi*6*t))) for f in (523,659,784))
        y+=0.25*bp(noise(d),500,1600)
        return y*env(len(t),0.06,0.15)
    w=np.zeros(int(SR*2.6)); place(w,blast(0.55),0); place(w,blast(1.7),0.8)
    write('transport/train_whistle', norm(w,0.8))
    # travel: whoosh with filter sweep
    d=2.0; n=int(SR*d); x=noise(d); k=np.sin(np.pi*np.arange(n)/n)**2
    y=bp(x,250,900)*(1-k)+bp(x,900,4500)*k
    y=lp(y,6000)*np.sin(np.pi*np.arange(n)/n)**1.5
    write('transport/travel', norm(y,0.7))

def events():
    # air-raid siren: wailing rise/fall, mono, 5 s
    d=5.0; n=int(SR*d); t=np.arange(n)/SR
    f=420+300*(0.5-0.5*np.cos(2*np.pi*t/2.5))
    ph=2*np.pi*np.cumsum(f)/SR
    x=np.sin(ph)+0.5*np.sin(2*ph)+0.25*np.sin(3*ph)+0.1*signal.square(ph)
    x=lp(x,3000)*env(n,0.3,0.6)
    write('event/air_raid_siren', norm(x,0.6))
    # plane jet: broadband roar + whine, doppler-ish pitch drop
    d=5.0; n=int(SR*d); t=np.arange(n)/SR
    roar=lp(noise(d),1800)+0.4*bp(noise(d),2000,5000)
    whine_f=np.where(t<2.5,3100,3100*0.82)+0*t; whine_f=signal.savgol_filter(whine_f,40001,1)
    whine=0.08*np.sin(2*np.pi*np.cumsum(whine_f)/SR)
    swell=np.exp(-((t-2.5)/1.4)**2)*0.85+0.15
    x=(roar+whine)*swell
    write('event/plane_jet', norm(fade(x,0.3,0.6),0.85))

def ui_festival():
    # landmark fanfare: bright brass-like arpeggio C E G C'
    d=2.0; b=np.zeros(int(SR*d))
    for i,m in enumerate((60,64,67,72)):
        nd=0.9 if i==3 else 0.35; t=t_(nd); f=mtof(m+12)
        y=sum(np.sin(2*np.pi*f*k*t)/k**1.3 for k in range(1,7))*np.exp(-t/0.5)*env(len(t),0.01,0.08)
        place(b,y,i*0.16)
    write('ui/landmark_discovered', norm(fade(b,0.002,0.3),0.75))
    # hanukkah chime: 8 small bells rising (one per night) + shamash
    b=np.zeros(int(SR*3.0))
    for i,m in enumerate((74,76,77,79,81,82,84,86)):
        place(b, bell(mtof(m),1.6,decay=0.6)*0.5, i*0.13)
    place(b, bell(mtof(91),1.8,decay=0.9)*0.6, 8*0.13+0.1)
    write('festival/hanukkah_chime', norm(fade(b,0.001,0.4),0.7))
    # shabbat candle: match strike + soft warm double chime
    b=np.zeros(int(SR*2.6))
    strike=bp(noise(0.25),1500,7000)*np.exp(-t_(0.25)/0.05)
    flame=lp(noise(0.9),600)*np.sin(np.pi*np.arange(int(SR*0.9))/int(SR*0.9))*0.3
    place(b,strike*0.6,0); place(b,flame,0.12)
    place(b,bell(mtof(79),2.0,decay=0.9)*0.6,0.45); place(b,bell(mtof(86),1.8,decay=0.8)*0.4,0.75)
    write('festival/shabbat_candle', norm(fade(b,0.001,0.4),0.65))

def babble(d, voices=10, base=0.6):
    n=int(SR*d); out=np.zeros(n)
    for k in range(voices):
        f0=rng.uniform(95,230); seg=rng.uniform(0.6,1.6); st=rng.uniform(0,d-0.3)
        L=min(seg,d-st); vs=list(rng.choice(list('aeiou'),5))
        tt=np.arange(int(SR*L))/SR
        x=voice(L,f0*(1+0.08*np.sin(2*np.pi*rng.uniform(1,3)*tt)),vs)*(0.6+0.4*np.clip(np.sin(2*np.pi*rng.uniform(3,5)*tt),0,1))
        x=fade(x,0.05,0.15); place(out,x*rng.uniform(0.4,1.0),st)
    room=lp(noise(d),400)*0.15
    return lp(out,3000)+room

def ambience():
    d=6.0; n=int(SR*d); t=np.arange(n)/SR
    # Tel Aviv: sea waves + distant traffic + a scooter pass
    waves=lp(noise(d),900)*(0.5+0.5*np.sin(2*np.pi*t/3.0-1)**2)
    traffic=lp(noise(d),250)*0.6
    scooter_f=180+20*np.sin(2*np.pi*t*0.4); sc=signal.sawtooth(2*np.pi*np.cumsum(scooter_f)/SR)*np.exp(-((t-3.8)/0.9)**2)*0.12
    write('ambient/tel_aviv', norm(fade(waves+traffic+lp(sc,1500),0.5,0.8),0.55))
    # Jerusalem: wind over stone, distant low bell and soft birds
    wind=bp(noise(d),200,1200)*(0.6+0.4*np.sin(2*np.pi*t/4.0))
    b=np.zeros(n); place(b,bell(mtof(50),3.5,decay=1.6)*0.35,1.0)
    for st in (0.6,2.2,4.5):
        L=0.12; tt=t_(L); place(b,np.sin(2*np.pi*(3200+1500*tt/L)*tt)*np.hanning(len(tt))*0.08,st)
    write('ambient/jerusalem', norm(fade(wind+b,0.5,0.8),0.5))
    # Jaffa: harbor lapping water, creaking rope, ship bell
    lap=bp(noise(d),150,700)*(0.5+0.5*np.abs(np.sin(2*np.pi*t/1.7)))
    b=np.zeros(n); place(b,bell(mtof(81),2.5,decay=0.7)*0.25,1.5); place(b,bell(mtof(81),2.5,decay=0.7)*0.2,1.85)
    creak=np.zeros(n); L=0.5; tt=t_(L); place(creak,bp(signal.sawtooth(2*np.pi*(140+40*tt)*tt),300,1500)*np.hanning(len(tt))*0.1,4.0)
    write('ambient/jaffa', norm(fade(lap+b+creak,0.5,0.8),0.55))
    # crowd at a speech: murmur
    write('ambient/speech_crowd', norm(fade(babble(5.0,14),0.6,0.8),0.55))
    # market: murmur + coin clinks + crates
    x=babble(5.0,10); b=np.zeros(len(x))
    for st in rng.uniform(0.2,4.6,6): place(b,bell(rng.uniform(3500,5200),0.3,((1,1),(2.3,.5)),decay=0.05)*0.15,st)
    for st in (1.3,3.4): place(b,lp(noise(0.08),1500)*np.exp(-t_(0.08)/0.02)*0.4,st)
    write('ambient/market_bustle', norm(fade(x+b,0.5,0.8),0.55))

# ---------- music (stereo, original melodies) ----------
def inst_clarinet(f, d, vel=1.0):
    t=t_(d); vib=1+0.006*np.sin(2*np.pi*5.2*t)*np.clip(t*3,0,1)
    ph=2*np.pi*np.cumsum(f*vib)/SR
    y=sum(np.sin(k*ph)/k for k in (1,3,5,7,9))+0.04*rng.standard_normal(len(t))
    return lp(y,3500)*env(len(t),0.03,min(0.08,d/3))*vel
def inst_pluck(f,d,vel=1.0):
    t=t_(d); y=sum(np.sin(2*np.pi*f*k*t)*np.exp(-t*k*3)/k for k in range(1,6)); return y*env(len(t),0.003,0.03)*vel
def inst_pad(f,d,vel=1.0):
    t=t_(d); y=sum(np.sin(2*np.pi*f*r*t+ k) for k,r in enumerate((1,1.003,0.997,2.001)))/4
    return lp(y,1800)*env(len(t),min(0.6,d/3),min(0.8,d/3))*vel
def inst_kick(d=0.25):
    t=t_(d); return np.sin(2*np.pi*np.cumsum(np.geomspace(120,45,len(t)))/SR)*np.exp(-t/0.12)
def inst_snare(d=0.15):
    t=t_(d); return hp(noise(d),1500)*np.exp(-t/0.05)*0.6
def mix_stereo(n): return np.zeros((n,2))
def put(buf,x,at,pan=0.0,g=1.0):
    i=int(at*SR); m=min(len(x),buf.shape[0]-i)
    if m<=0: return
    l=np.cos((pan+1)*np.pi/4); r=np.sin((pan+1)*np.pi/4)
    buf[i:i+m,0]+=x[:m]*l*g; buf[i:i+m,1]+=x[:m]*r*g
def reverb(x, wet=0.25):
    out=x.copy()
    for dl,g in ((0.031,0.5),(0.047,0.4),(0.071,0.3),(0.113,0.22),(0.163,0.15)):
        k=int(dl*SR)
        out[k:,0]+=x[:-k,1]*g*wet*2; out[k:,1]+=x[:-k,0]*g*wet*2
    return out

def klezmer():
    # original freygish (D Ahava Rabbah: D Eb F# G A Bb C) bulgar, 132 bpm, 32 bars ~58 s
    bpm=132; beat=60/bpm; bars=32; n=int(SR*(bars*4*beat+1.0)); buf=mix_stereo(n)
    sc=[62,63,66,67,69,70,72,74,75,78]
    phrase_a=[(0,1),(2,.5),(3,.5),(4,1),(3,.5),(2,.5),(1,1.5),(0,.5),(2,1),(4,1),(5,.5),(4,.5),(3,.5),(2,.5),(0,2),(-1,0)]
    phrase_b=[(4,.5),(5,.5),(6,1),(5,.5),(4,.5),(3,1),(4,.5),(3,.5),(2,1),(1,.5),(2,.5),(3,1),(2,.5),(1,.5),(0,2)]
    phrase_c=[(7,1),(6,.5),(5,.5),(4,1),(6,.5),(5,.5),(4,.5),(3,.5),(2,1),(4,1.5),(3,.5),(2,.5),(1,.5),(0,2)]
    def play_phrase(ph, start, octave=0):
        tt=start
        for deg,dur in ph:
            if dur==0: continue
            put(buf, inst_clarinet(mtof(sc[deg]+octave), dur*beat*0.95, 0.5), tt, -0.2)
            tt+=dur*beat
        return tt
    tt=0.0; seq=[phrase_a,phrase_a,phrase_b,phrase_c]*4
    for i,p in enumerate(seq):
        if tt>=bars*4*beat-2*beat: break
        tt=play_phrase(p, tt, 12 if i>=8 and p is phrase_c else 0)
    # oom-pah: bass on 1/3, chords on 2/4 (D major / C minor / G minor style freygish harmony)
    prog=[(50,[62,66,69]),(50,[62,66,69]),(48,[63,67,72]),(50,[62,66,69])]
    for bar in range(bars):
        root,ch=prog[(bar//2)%4]
        for b in range(4):
            at=(bar*4+b)*beat
            if b%2==0: put(buf, inst_pluck(mtof(root-12 if b==0 else root-5), beat*0.9, 0.7), at, 0.0)
            else:
                for m in ch: put(buf, inst_pluck(mtof(m-12), beat*0.5, 0.18), at, 0.35)
            put(buf, inst_kick(), at, 0, 0.35 if b%2==0 else 0)
            if b%2==1: put(buf, inst_snare(), at, 0.1, 0.25)
    write('music/klezmer', norm(fade(reverb(buf,0.2),0.05,1.0),0.85), q=3)

def shabbat():
    # original calm piece in D major, 72 bpm, 16 bars ~53 s: pads + gentle pluck melody
    bpm=72; beat=60/bpm; bars=16; n=int(SR*(bars*4*beat+3)); buf=mix_stereo(n)
    chords=[[50,57,62,66],[47,54,59,62],[43,55,59,62],[45,52,57,61]]  # D Bm G A
    for bar in range(bars):
        ch=chords[bar%4]
        for i,m in enumerate(ch): put(buf, inst_pad(mtof(m),4*beat+0.6,0.16), bar*4*beat, -0.5+i*0.33)
    mel=[(74,2),(76,1),(78,1),(76,3),(74,1),(71,2),(73,1),(74,1),(76,4),
         (78,2),(81,1),(78,1),(76,2),(74,2),(73,2),(71,1),(73,1),(74,4),
         (69,1),(71,1),(74,2),(76,2),(78,2),(79,2),(78,1),(76,1),(74,4),
         (76,2),(74,1),(73,1),(71,2),(69,2),(71,1),(73,1),(74,6)]
    tt=0.0
    for m,dd in mel:
        put(buf, inst_pluck(mtof(m), dd*beat+0.4, 0.35), tt, 0.25)
        put(buf, bell(mtof(m+12), dd*beat+0.4, decay=0.6)*0.04, tt, -0.3)
        tt+=dd*beat
    write('music/shabbat_shalom', norm(fade(reverb(buf,0.35),0.8,2.5),0.8), q=3)

bibi(); transport(); events(); ui_festival(); ambience(); klezmer(); shabbat()
