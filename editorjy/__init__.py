import sys
import os

current_folder = os.path.dirname(os.path.abspath(__file__))

jarpath = os.path.join(current_folder, 'javalib/EditorJy-0.1.0-SNAPSHOT.jar')
if not jarpath in sys.path:
    sys.path.append(jarpath)   

import start    