import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

class RoundedPanel extends JPanel {
        private int cornerRadius = 15;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            setOpaque(false); // Quan trọng để nền không bị vuông
            setBackground(bgColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            super.paintComponent(g2);
            g2.dispose();
        }
    }