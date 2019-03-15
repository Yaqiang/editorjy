/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteothink.editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import org.meteothink.editor.event.CurrentPathChangedEvent;
import org.meteothink.editor.event.ICurrentPathChangedListener;
import org.meteothink.editor.ui.IconRenderer;
import org.meteothink.editor.ui.IconText;

/**
 *
 * @author wyq
 */
public class FileExplorer extends JPanel implements MouseListener{
    
    private final EventListenerList listeners = new EventListenerList();
    //private final JButton jbUp;
    //private final JComboBox jcbPath;
    private final JTable jtFile;
    private final DefaultTableModel dtmFile;
    //private final JLabel jlLocal;
    private File path;
    private String currentPath;
    private int currentIndex;
    private boolean init = false;

    /**
     * Constructor
     * @param path Path
     */
    public FileExplorer(File path) {
        super(new BorderLayout());
        
        this.path = path;
        this.setForeground(Color.white);
        dtmFile = new LocalTableModel();
        dtmFile.addColumn("Name");
        dtmFile.addColumn("Size");
        dtmFile.addColumn("File Type");
        dtmFile.addColumn("Date Modified");
        jtFile = new JTable(dtmFile);
        jtFile.getColumnModel().getColumn(0).setCellRenderer(new IconRenderer());
        jtFile.setShowGrid(false);
        jtFile.addMouseListener(this);

        add(new JScrollPane(jtFile), "Center");

        //Show current path files
        //path = new File(System.getProperty("user.dir"));
        if (path != null)
            listFiles(path);    

        init = true;
    }
    
    /**
     * Set path
     * @return Path
     */
    public File getPath(){
        return this.path;
    }
    
    /**
     * Set path
     * @param path Path
     */
    public void setPath(File path){
        this.path = path;
        this.listFiles(path);
        this.fireCurrentPathChangedEvent();
    }
    
    /**
     * Get table
     * @return Table
     */
    public JTable getTable(){
        return this.jtFile;
    }
    
    public void addCurrentPathChangedListener(ICurrentPathChangedListener listener) {
        this.listeners.add(ICurrentPathChangedListener.class, listener);
    }

    public void removeCurrentPathChangedListener(ICurrentPathChangedListener listener) {
        this.listeners.remove(ICurrentPathChangedListener.class, listener);
    }

    public void fireCurrentPathChangedEvent() {
        fireCurrentPathChangedEvent(new CurrentPathChangedEvent(this));
    }

    private void fireCurrentPathChangedEvent(CurrentPathChangedEvent event) {
        Object[] ls = this.listeners.getListenerList();
        for (int i = 0; i < ls.length; i = i + 2) {
            if (ls[i] == ICurrentPathChangedListener.class) {
                ((ICurrentPathChangedListener) ls[i + 1]).currentPathChangedEvent(event);
            }
        }
    }

    //JTable mouse click event
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount()==2) {
            int row = ((JTable)e.getSource()).getSelectedRow();
            File newPath;
            if (((JTable)e.getSource()).getValueAt(row, 2).toString().equals("Folder"))
            {
                newPath = new File(currentPath + "/" + ((JTable)e.getSource()).getValueAt(row, 0).toString());
                if (!newPath.exists()) {
                    //Root path
                    newPath = new File(currentPath + ((JTable)e.getSource()).getValueAt(row, 0).toString());
                }
                if (newPath.isDirectory()){
                    listFiles(newPath);
                    this.fireCurrentPathChangedEvent();
                }
            }
            else if (((JTable)e.getSource()).getValueAt(row, 0).toString().equals("")
                    && ((JTable)e.getSource()).getValueAt(row, 2).toString().equals(""))
            {
                newPath = new File(currentPath).getParentFile();
                listFiles(newPath);
                this.fireCurrentPathChangedEvent();
            }
        }
    }
    //The not used mouse events
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    /**
     * List files
     * @return 
     */
    public boolean listFiles() {   
        return this.listFiles(path);
    }

    /**
     * List files
     * @param path File path
     * @return 
     */
    public boolean listFiles(File path) {        
        if (!path.isDirectory())
        {
            JOptionPane.showMessageDialog(this, "The file not exists!");
            return false;
        }
        
        this.path = path;
        currentPath = path.getAbsolutePath();
        init = false;

        //Clear
        dtmFile.setRowCount(0);

        //Add "To Parent" line if the path is not root path
        if (path.getParent() != null)
        {
            java.net.URL imgURL = this.getClass().getResource("/images/previous.png");
            ImageIcon icon = new ImageIcon(imgURL);
            dtmFile.addRow(new Object[]{new IconText(icon, ""), "", "", ""});
        }

        //List all files
        java.net.URL folderURL = this.getClass().getResource("/images/folder.png");
        ImageIcon folderIcon = new ImageIcon(folderURL);
        java.net.URL fileURL = this.getClass().getResource("/images/TSB_NewFile.Image.png");
        ImageIcon fileIcon = new ImageIcon(fileURL);
        File[] files = path.listFiles();
        Arrays.sort(files);
        for (File file : files){
            String name = file.getName();
            if (file.isDirectory()) {
                dtmFile.addRow(new Object[]{new IconText(folderIcon, name), "", "Folder", ""});
            } 
        }
        for (File file : files) {
            String name = file.getName();
            if (file.isFile()) {                              
                if (name.lastIndexOf(".") != -1) {
                    dtmFile.addRow(new Object[]{new IconText(fileIcon, name), sizeFormat(file.length()), name.substring(name.lastIndexOf(".") + 1), new SimpleDateFormat("yyyy/M/d hh:mm").format(new Date(file.lastModified()))});
                } else {
                    dtmFile.addRow(new Object[]{new IconText(fileIcon, name), sizeFormat(file.length()), "", new SimpleDateFormat("yyyy/M/d hh:mm").format(new Date(file.lastModified()))});
                }
            }
        }

        return true;
    }

    //Convert file size to string
    private String sizeFormat(long length) {
        long kb;
        if (length < 1024)
        {
            return String.valueOf(length);
        }
        else if ((kb = length / 1024) < 1024)
        {
            return (String.valueOf(kb) + "kb");
        }
        else
        {
            return (String.valueOf(length / 1024 / 1024) + "kb");
        }
    }

    //Test
    public static void main(String[] args) {
        JFrame jf = new JFrame("Test");
        jf.setSize(300, 400);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension di = Toolkit.getDefaultToolkit().getScreenSize();
        jf.setLocation((int)(di.getWidth() - jf.getWidth()) / 2, 
                (int)(di.getHeight() - jf.getHeight()) / 2);
        jf.add(new FileExplorer(new File(System.getProperty("user.dir"))));
        jf.setVisible(true);
    }

    //LocalTableModel class
    class LocalTableModel extends DefaultTableModel
    {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }  
    }
}
