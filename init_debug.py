import sys

libpaths = []
libpaths.append("D:/MyProgram/java/MeteoThinkDev/NumJy")
libpaths.append("D:/MyProgram/java/MeteoThinkDev/PlotJy")
libpaths.append("D:/MyProgram/java/MeteoThinkDev/DataframeJy")
libpaths.append("D:/MyProgram/java/MeteoThinkDev/DatasetJy")
for libpath in libpaths:
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