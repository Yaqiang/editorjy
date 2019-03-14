import sys

libpath = "C:/jython2.7.1/Lib/site-packages"
if not libpath in sys.path:
    sys.path.append(libpath)
    
import plotjy
plotjy.jyplot.figure_parent = editor_app.getFigureDock()
#print plotjy.jyplot.figure_parent

def clear():
    """
    Clear all variables.
    """
    editor_app.delVariables()