"""Render the public-domain Mutopia score as a quiet piano performance.
Requires mido, libfluidsynth and TimGM6mb.sf2; then encode using ffmpeg.
"""
import ctypes as C
import wave
from pathlib import Path
import mido

root = Path(__file__).parent
lib = C.CDLL('libfluidsynth.so.3')
def bind(name, result, args):
    f = getattr(lib, name); f.restype = result; f.argtypes = args; return f
ptr, integer = C.c_void_p, C.c_int
settings = bind('new_fluid_settings', ptr, [])()
bind('fluid_settings_setnum', integer, [ptr, C.c_char_p, C.c_double])(settings, b'synth.sample-rate', 44100.)
bind('fluid_settings_setnum', integer, [ptr, C.c_char_p, C.c_double])(settings, b'synth.gain', .35)
synth = bind('new_fluid_synth', ptr, [ptr])(settings)
bind('fluid_synth_sfload', integer, [ptr, C.c_char_p, integer])(synth, b'/usr/share/sounds/sf2/TimGM6mb.sf2', 1)
program = bind('fluid_synth_program_change', integer, [ptr, integer, integer])
on = bind('fluid_synth_noteon', integer, [ptr, integer, integer, integer])
off = bind('fluid_synth_noteoff', integer, [ptr, integer, integer])
write = bind('fluid_synth_write_s16', integer, [ptr, integer, ptr, integer, integer, ptr, integer, integer])
for channel in range(16): program(synth, channel, 0)
with wave.open(str(root / 'piano.wav'), 'wb') as wav:
    wav.setparams((2, 2, 44100, 0, 'NONE', 'not compressed'))
    def render(seconds):
        remaining = round(seconds * 44100)
        while remaining:
            count = min(remaining, 4096)
            buffer = (C.c_short * (count * 2))()
            write(synth, count, buffer, 0, 2, buffer, 1, 2)
            wav.writeframes(bytes(buffer)); remaining -= count
    for event in mido.MidiFile(root / 'marseillaise.mid'):
        render(event.time * 1.25)
        if event.type == 'note_on': on(synth, event.channel, event.note, min(65, max(1, int(event.velocity * .7))) if event.velocity else 0)
        elif event.type == 'note_off': off(synth, event.channel, event.note)
    render(3)
bind('delete_fluid_synth', None, [ptr])(synth)
bind('delete_fluid_settings', None, [ptr])(settings)
print('Rendered tools/audio/piano.wav')
