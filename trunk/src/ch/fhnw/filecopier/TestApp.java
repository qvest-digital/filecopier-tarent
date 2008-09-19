/*
 * TestApp.java
 *
 * Created on 22. April 2008, 15:30
 */
package ch.fhnw.filecopier;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author  ronny
 */
public class TestApp extends javax.swing.JFrame {

    private final Logger logger = Logger.getLogger(TestApp.class.getName());

    /** Creates new form TestApp */
    public TestApp() {
        initComponents();

        // test run
        final FileCopier fileCopier = new FileCopier();
        fileCopierPanel.setFileCopier(fileCopier);
        SwingWorker copier = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                try {
                    CopyJob job1 = new CopyJob(true, "/mnt/sda8/ronny",
                            "/media/sda8/archiv/knoppix/KNOPPIX_V5.3.1DVD-2008-03-26-DE.iso");
                    fileCopier.copy(job1, job1);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void done() {
                System.out.println("fertsch!");
            }
        };
        copier.execute();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileCopierPanel = new ch.fhnw.filecopier.FileCopierPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fileCopierPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(fileCopierPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TestApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ch.fhnw.filecopier.FileCopierPanel fileCopierPanel;
    // End of variables declaration//GEN-END:variables
}