/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteothink.editor.gui;

import java.awt.Color;
import org.meteothink.console.JavaCharStream;
import org.meteothink.console.JConsole;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.meteothink.editor.event.ConsoleExecEvent;
import org.meteothink.editor.event.IConsoleExecListener;
import org.python.util.InteractiveConsole;

/**
 *
 * @author yaqiang
 */
public class PythonInteractiveInterpreter extends InteractiveConsole implements Runnable {

    //transient Reader in;
    //transient PrintStream out;
    //transient PrintStream err;
    private Reader in;
    private PrintStream out;
    private PrintStream err;
    JConsole console;
    private final EventListenerList listeners = new EventListenerList();

    public PythonInteractiveInterpreter(JConsole console) {
        super();

        this.cflags.source_is_utf8 = true;
        this.console = console;
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }
    
    /**
     * Get console
     * @return Console
     */
    public JConsole getConsole(){
        return this.console;
    }
    
    /**
     * Set console
     * @param console the JConsole
     */
    public void setConsole(JConsole console) {
        this.console = console;
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }
    
    /**
     * Update in, out and err
     */
    public void upate() {
        console.updateOut();
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }
    
    /**
     * Get this.cflags.source_is_utf8
     * @return Boolean
     */
    public boolean isSourceUTF8(){
        return this.cflags.source_is_utf8;
    }
    
    /**
     * Set this.cflags.source_is_utf8
     * @param value 
     */
    public void setSourceUTF8(boolean value){
        this.cflags.source_is_utf8 = value;
    }

    @Override
    public void run() {
        boolean eof = false;
        JavaCharStream stream = new JavaCharStream(in, 1, 1);

//        exec("_ps1 = sys.ps1");
//        PyObject ps1Obj = get("_ps1");
//        String ps1 = ps1Obj.toString();
        String ps1 = ">>> ";

//        exec("_ps2 = sys.ps2");
//        PyObject ps2Obj = get("_ps2");
//        String ps2 = ps2Obj.toString();
        String ps2 = "... ";
        //out.print(getDefaultBanner() + "\n");
        this.console.print(getDefaultBanner() + "\n", Color.red);
        //out.print(ps1);
        this.console.print(ps1, Color.red);        
        String line;
        boolean retVal = false;
        while (!eof) {
            // try to sync up the console
            System.out.flush();
            System.err.flush();
            Thread.yield();  // this helps a little

            try {
                boolean eol = false;
                line = "";

                while (!eol) {
                    char aChar = stream.readChar();
                    eol = (aChar == '\n');
                    if (!eol) {
                        line = line + aChar;
                    }
                }
                if (!retVal)
                    line = line.trim();

                //hitting Enter at prompt returns a semicolon
                //get rid of it since it returns an error when executed
                if (line.equals(";")) {
                    line = "";
                }

                retVal = push(line);

                if (retVal) {
                    out.print(ps2);                    
                } else {
                    //out.print(ps1);
                    //this.console.print(ps1, Color.red);
                    this.fireConsoleExecEvent();
                }
            } catch (Exception e) {
                out.print(e.toString() + '\n');
                this.resetbuffer();
                try {                
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PythonInteractiveInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.fireConsoleExecEvent();
            }
        }
    }
    
    @Override
    public void execfile(InputStream s){
        this.cflags.source_is_utf8 = false;
        super.execfile(s);
        this.cflags.source_is_utf8 = true;
        this.fireConsoleExecEvent();
    }
    
    @Override
    public void execfile(String fn) {
        this.cflags.source_is_utf8 = false;
        super.execfile(fn);
        this.cflags.source_is_utf8 = true;
        this.fireConsoleExecEvent();
    }

    public void addConsoleExecListener(IConsoleExecListener listener) {
        this.listeners.add(IConsoleExecListener.class, listener);
    }

    public void removeConsoleExecListener(IConsoleExecListener listener) {
        this.listeners.remove(IConsoleExecListener.class, listener);
    }

    public void fireConsoleExecEvent() {
        fireConsoleExecEvent(new ConsoleExecEvent(this));
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(PythonInteractiveInterpreter.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.console.print(">>> ", Color.red);
        this.console.setStyle(Color.black);
        this.console.setForeground(Color.black);
    }

    private void fireConsoleExecEvent(ConsoleExecEvent event) {
        Object[] ls = this.listeners.getListenerList();
        for (int i = 0; i < ls.length; i = i + 2) {
            if (ls[i] == IConsoleExecListener.class) {
                ((IConsoleExecListener) ls[i + 1]).consoleExecEvent(event);
            }
        }
    }
    
}
