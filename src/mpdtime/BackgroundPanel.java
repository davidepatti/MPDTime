package mpdtime;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

// Classe interna per il pannello con immagine di sfondo
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String fileName) {
        setBackgroundImage(fileName);
    }

    public void setBackgroundImage(String fileName) {
        backgroundImage = new ImageIcon(getClass().getResource("/" + fileName)).getImage();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Disegna l'immagine di sfondo
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}