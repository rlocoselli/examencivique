"""Run against an already booted emulator with the debug APK installed.
Checks real UI navigation, the persisted music preference and the hymn quiz.
"""
import re
import subprocess
import time
import xml.etree.ElementTree as ET

def adb(*args):
    return subprocess.check_output(['adb', *args], text=True)

def nodes():
    adb('shell', 'uiautomator', 'dump', '/sdcard/citoyen-smoke.xml')
    return list(ET.fromstring(adb('shell', 'cat', '/sdcard/citoyen-smoke.xml')).iter('node'))

def tap(node):
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.attrib['bounds']))
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))

def text_node(label):
    for _ in range(3):
        result = next((n for n in nodes() if label in n.attrib.get('text', '')), None)
        if result is not None:
            return result
        time.sleep(1)
    raise StopIteration(label)

def launch():
    adb('shell', 'am', 'start', '-S', '-n', 'fr.audeladedonnees.examen.civique/.MainActivity')
    time.sleep(2)

launch()
assert text_node('159 questions') is not None
switch = next(n for n in nodes() if n.attrib.get('checkable') == 'true')
initial = switch.attrib['checked']
tap(switch)
expected = 'false' if initial == 'true' else 'true'
assert next(n for n in nodes() if n.attrib.get('checkable') == 'true').attrib['checked'] == expected
launch()
switch = next(n for n in nodes() if n.attrib.get('checkable') == 'true')
assert switch.attrib['checked'] == expected, 'Music preference did not persist'
tap(switch)
tap(text_node('L’hymne de la France'))
assert text_node('L’hymne national français') is not None
assert text_node('Volume de la musique') is not None
for _ in range(5):
    try:
        quiz = text_node('Quiz spécial hymne')
        break
    except StopIteration:
        adb('shell', 'input', 'swipe', '500', '1800', '500', '700', '350')
else:
    raise AssertionError('Hymn quiz is not reachable')
tap(quiz)
tap(text_node('Lancer le dé'))
time.sleep(2)
assert text_node('1/12 • HYMNE') is not None
launch()
tap(text_node('L’hymne de la France'))
print('PASS: question count, music switch, persistence after restart, hymn page and dedicated quiz.')
