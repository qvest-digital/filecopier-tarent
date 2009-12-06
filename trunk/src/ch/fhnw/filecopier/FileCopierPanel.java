/*
 * FileCopierPanel.java
 *
 * Created on 22. April 2008, 14:21
 *
 * This file is part of the Java File Copy Library.
 * 
 * The Java File Copy Libraryis free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * 
 * The Java File Copy Libraryis distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.fhnw.filecopier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A JPanel that can show the progress of file copy operations.
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class FileCopierPanel extends JPanel implements PropertyChangeListener {

    private static final ResourceBundle strings =
            ResourceBundle.getBundle("ch/fhnw/filecopier/Strings");
    // the sum of the size of all files to copy
    private long byteCount;
    // the number of bytes already copied
    private long bytesCopied;
    private String completeDataVolumeString;
    private FileCopier fileCopier;
    private final static NumberFormat numberFormat = NumberFormat.getInstance();
    private DateFormat timeFormat;
    private DateFormat endTimeFormat;
    private DateFormat minuteFormat;
    // some constants
    private static final int KILO = 1024;
    private static final int MEGA = KILO * 1024;
    private static final int GIGA = MEGA * 1024;
    private static final long TERA = GIGA * 1024;
    private static final int HOUR = 3600000;
    // remaining time calcuation
    private long startTime;
    private Timer remainigTimer = new Timer(1000, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            bytesCopied = fileCopier.getCopiedBytes();
            if (bytesCopied == 0) {
                return;
            }
            updateProgressBar();
            long currentTime = System.currentTimeMillis();
            long timeSpent = currentTime - startTime;
            long estimate = (timeSpent * byteCount) / bytesCopied;
            long remaining = estimate - timeSpent;
            String formattedSpentTime = getTimeString(timeSpent);
            String formattedTotalTime = getTimeString(estimate);
            String formattedRemainingTime = getTimeString(remaining);
            String formattedEndTime = endTimeFormat.format(
                    currentTime + remaining);
            String string = strings.getString("Time");
            string = MessageFormat.format(string, formattedSpentTime,
                    formattedTotalTime, formattedRemainingTime,
                    formattedEndTime);
            remainingLabel.setText(string);
        }
    });

    /** Creates new form FileCopierPanel */
    public FileCopierPanel() {
        timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        endTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        minuteFormat = new SimpleDateFormat("mm:ss");
        initComponents();
        remainingLabel.setText(null);
    }

    private String getTimeString(long time) {
        if (time < HOUR) {
            return minuteFormat.format(time);
        } else {
            return timeFormat.format(time);
        }
    }

    /**
     * returns the string representation of a given data volume
     * @param bytes the datavolume given in Byte
     * @param fractionDigits the number of fraction digits to display
     * @return the string representation of a given data volume
     */
    public static String getDataVolumeString(
            long bytes, int fractionDigits) {
        if (bytes >= KILO) {
            numberFormat.setMaximumFractionDigits(fractionDigits);
            float kbytes = (float) bytes / KILO;
            if (kbytes >= KILO) {
                float mbytes = (float) bytes / MEGA;
                if (mbytes >= KILO) {
                    float gbytes = (float) bytes / GIGA;
                    if (gbytes >= KILO) {
                        float tbytes = (float) bytes / TERA;
                        return numberFormat.format(tbytes) + " TB";
                    }
                    return numberFormat.format(gbytes) + " GB";
                }
                return numberFormat.format(mbytes) + " MB";
            }
            return numberFormat.format(kbytes) + " KB";
        }
        return numberFormat.format(bytes) + " Byte";
    }

    /**
     * Sets the fileCopier
     *
     * @param fileCopier the fileCopier to monitor
     */
    public void setFileCopier(FileCopier fileCopier) {
        if (this.fileCopier != null) {
            this.fileCopier.removePropertyChangeListener(
                    FileCopier.STATE_PROPERTY, this);
//            this.fileCopier.removePropertyChangeListener(
//                    FileCopier.BYTE_COUNTER_PROPERTY, this);
        }
        this.fileCopier = fileCopier;
        fileCopier.addPropertyChangeListener(FileCopier.STATE_PROPERTY, this);
//        fileCopier.addPropertyChangeListener(
//                FileCopier.BYTE_COUNTER_PROPERTY, this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();
        remainingLabel = new javax.swing.JLabel();

        progressBar.setStringPainted(true);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ch/fhnw/filecopier/Strings"); // NOI18N
        remainingLabel.setText(bundle.getString("Time")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(remainingLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remainingLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel remainingLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String propertyName = evt.getPropertyName();
                if (FileCopier.STATE_PROPERTY.equals(propertyName)) {
                    FileCopier.State newState =
                            (FileCopier.State) evt.getNewValue();

                    switch (newState) {
                        case START:
                            progressBar.setIndeterminate(false);
                            progressBar.setString("0%");
                            break;

                        case CHECKING_SOURCE:
                            progressBar.setIndeterminate(true);
                            progressBar.setString(strings.getString(
                                    "Checking_Source_Directory"));
                            break;

                        case COPYING:
                            startTime = System.currentTimeMillis();
                            remainigTimer.setInitialDelay(0);
                            remainigTimer.start();
                            byteCount = fileCopier.getByteCount();
                            completeDataVolumeString =
                                    getDataVolumeString(byteCount, 1);
                            progressBar.setIndeterminate(false);
                            bytesCopied = 0;
                            updateProgressBar();
                            break;

                        case END:
                            progressBar.setString(strings.getString("Done"));
                            remainigTimer.stop();
                            long currentTime = System.currentTimeMillis();
                            long timeSpent = currentTime - startTime;
                            String formattedSpentTime =
                                    getTimeString(timeSpent);
                            String string = strings.getString("Time_Summary");
                            String formattedEndTime =
                                    endTimeFormat.format(currentTime);
                            string = MessageFormat.format(string,
                                    formattedSpentTime, formattedEndTime);
                            remainingLabel.setText(string);
                    }

                } else if (FileCopier.BYTE_COUNTER_PROPERTY.equals(
                        propertyName)) {
                    bytesCopied = ((Long) evt.getNewValue()).longValue();
                    updateProgressBar();
                }
            }
        });
    }

    // must be called from the Swing Event Thread!
    private void updateProgressBar() {
        if (byteCount == 0) {
            // there are only empty files and directories...
            return;
        }
        int progress = (int) ((100 * bytesCopied) / byteCount);
        long currentTime = System.currentTimeMillis();
        long timeSpent = (currentTime - startTime) / 1000;
        int speed = 0;
        if (timeSpent != 0) {
            speed = (int) (bytesCopied / timeSpent);
        }
        String currentDataVolume = getDataVolumeString(bytesCopied, 1);
        String currentBandwidth = getDataVolumeString(speed, 1) + "/s";
        String copyMessage = MessageFormat.format(
                strings.getString("Copying_X_Of_Y_Bytes"),
                currentDataVolume, completeDataVolumeString) +
                " (" + currentBandwidth + ", " + progress + "%)";
        progressBar.setValue(progress);
        progressBar.setString(copyMessage);
    }
}
