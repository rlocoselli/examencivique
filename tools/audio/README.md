# La Marseillaise — piano

Composition : Claude Joseph Rouget de Lisle, 1792 (domaine public).
Partition : Bignon 1792, transcription Coyau, Mutopia-2013/12/08-1891.
Le transcripteur a placé cette édition dans le domaine public.
Source : https://www.mutopiaproject.org/ftp/RougetdeLisleCJ/Marseillaise1792/

Le MIDI et la source LilyPond sont conservés ici. `render.py` réalise un rendu
instrumental au piano, à tempo ralenti et vélocités réduites, sans utiliser un
enregistrement commercial. Rendu avec FluidSynth et TimGM6mb.sf2.
SoundFont : Tim Brechbill (2004), David Bolton (2010), GPL-2 ; disponible dans
le paquet Debian `timgm6mb-soundfont`. Le SoundFont n’est pas embarqué dans l’APK.
Source : https://github.com/musescore/musescore-old/commit/90c33ef9d87b3f5ff92efd3b07d89eb455fb1fef

Reconstruction (Python mido, libfluidsynth.so.3, ffmpeg requis) :

```
python tools/audio/render.py
ffmpeg -i tools/audio/piano.wav -af 'volume=20dB,afade=t=in:d=1,apad=pad_dur=2' -c:a libvorbis -q:a 4 app/src/main/res/raw/marseillaise_piano.ogg
```

Lecture locale en boucle ; volume initial à 12 % du volume média de l’appareil,
réglable entre 1 % et 30 %. L’interrupteur et le volume sont mémorisés.
La lecture est suspendue hors premier plan, à la perte du focus audio et lors
du débranchement d’un casque. Le bouton Son est accessible pendant le jeu.

Source historique de la rubrique et des questions sur l’hymne :
https://www.elysee.fr/la-presidence/la-marseillaise-de-rouget-de-lisle
https://www.assemblee-nationale.fr/dyn/histoire-et-patrimoine/revolution-francaise/la-marseillaise-hymne-national
