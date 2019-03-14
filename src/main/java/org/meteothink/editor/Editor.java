/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteothink.editor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.meteothink.util.DataConvert;
import org.meteothink.global.util.FontUtil;
import org.meteothink.global.util.GlobalUtil;
import org.meteothink.editor.gui.FrmMain;
import org.meteothink.editor.gui.MyPythonInterpreter;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;

/**
 *
 * @author Yaqiang Wang
 */
public class Editor {

    /**
     * Disable illegal access warning message
     */
    @SuppressWarnings("unchecked")
    public static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
        } catch (Exception ignored) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Disable illegal access warning message  
        disableAccessWarnings();

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                //fontPath = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns) {
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                args = (String[]) DataConvert.resizeArray(args, args.length - 1);
            }
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-i")) {
                runInteractive();
            } else if (args[0].equalsIgnoreCase("-b")) {
                if (args.length == 1) {
                    System.out.println("Script file name is needed!");
                    System.exit(0);
                } else {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        System.setProperty("java.awt.headless", "true");
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        System.out.println("Headless mode: " + ge.isHeadless());
                        runScript(args, fn, 1);
                    } else {
                        System.out.println("The script file does not exist!");
                        System.exit(0);
                    }
                }
            } else if (args[0].equalsIgnoreCase("-eng")) {
                runApplication(true);
            } else {
                String fn = args[0];
                if (new File(fn).isFile()) {
                    runScript(args, fn, 0);
                } else {
                    System.out.println("The script file does not exist!");
                    System.exit(0);
                }
            }
        } else {
            runApplication();
        }
    }

    private static void runScript(String args[], String fn, int idx) {
        //String ext = GlobalUtil.getFileExtension(fn);
        //registerFonts();
        org.meteothink.global.util.FontUtil.registerWeatherFont();

        System.out.println("Running Jython script...");
        PySystemState state = new PySystemState();
        if (args.length > idx + 1) {
            for (int i = idx + 1; i < args.length; i++) {
                state.argv.append(Py.newStringOrUnicode(args[i]));
                //state.argv.append(new PyString(args[i]));
            }
        }
        //state.setdefaultencoding("utf-8");
        //PythonInterpreter interp = new PythonInterpreter(null, state);
        MyPythonInterpreter interp = new MyPythonInterpreter(null, state);

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        String path, toolboxPath, miPath;
        if (isDebug) {
            path = "C:\\jython2.7.1\\Lib\\site-packages";
            toolboxPath = "D:/MyProgram/java/MeteoThinkDev/toolbox";
            miPath = "D:/MyProgram/Distribution/Java/MeteoInfo/MeteoInfo";
        } else {
            //String pluginPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "plugins";
            //List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
            miPath = GlobalUtil.getAppPath(FrmMain.class);
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
                miPath = miPath.substring(1);
            }
            path = miPath + File.separator + "pylib";
            toolboxPath = miPath + "/toolbox";;
        }

        try {
            interp.execfile(fn);
        } catch (Exception e) {
            e.printStackTrace();
            //System.exit(0);
        } finally {
            System.exit(0);
        }
    }

    private static void runInteractive() {
        String startPath = System.getProperty("user.dir");
        String miPath = GlobalUtil.getAppPath(FrmMain.class);
        String path = miPath + File.separator + "pylib";
        String toolboxPath = miPath + "/toolbox";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
            miPath = miPath.substring(1);
        }
        InteractiveConsole console = new InteractiveConsole();
        console.interact();
    }

    private static void runApplication() {
        runApplication(false);
        //runApplication(true);
    }

    private static void runApplication(final boolean isEng) {
        try {
            /* Set look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
            * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
             */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        //System.setProperty("-Dsun.java2d.dpiaware", "false");

        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            //java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                        getInputArguments().toString().contains("jdwp");
//                if (isDebug) {
//                    Locale.setDefault(Locale.ENGLISH);
//                }

                if (isEng) {
                    Locale.setDefault(Locale.ENGLISH);
                }

                StackWindow sw = null;
                if (!isDebug) {
                    sw = new StackWindow("Show Exception Stack", 600, 400);
                    Thread.UncaughtExceptionHandler handler = sw;
                    Thread.setDefaultUncaughtExceptionHandler(handler);
                    System.setOut(sw.printStream);
                    System.setErr(sw.printStream);
                }

                //registerFonts();
                org.meteothink.global.util.FontUtil.registerWeatherFont();
                FrmMain frame = new FrmMain();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                //frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                if (sw != null) {
                    sw.setLocationRelativeTo(frame);
                }
            }
        });
    }
}
