from org.meteothink.editor.gui import FrmMain
from org.meteothink.editor import StackWindow
from java.lang import Thread
from java.lang import System
import javax.swing as swing
import os
import sys

import numjy
import plotjy

current_folder = os.path.dirname(os.path.abspath(__file__))
editor_app = None

def main():
    sw = StackWindow("Show Exception Stack", 600, 400)
    handler = sw
    Thread.setDefaultUncaughtExceptionHandler(handler)
    System.setOut(sw.printStream)
    System.setErr(sw.printStream)

    swing.UIManager.setLookAndFeel(swing.UIManager.getSystemLookAndFeelClassName())

    frm = FrmMain(current_folder)
    frm.visible = True
    
    editor_app = frm
    plotjy.jyplot.figure_parent = editor_app.getFigureDock()
    #print plotjy.jyplot.figure_parent
    
    interp = frm.getConsoleDockable().getInterpreter()
    interp.getSystemState().path = sys.path
    interp.exec('import plotjy')

def clear():
    """
    Clear all variables.
    """
    if not editor_app is None:
        editor_app.delVariables()
        
def gc_collect():
    '''
    Clear variables and release memory
    '''
    clear()
    import gc
    gc.collect()