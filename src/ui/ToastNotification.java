package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ToastNotification {

    private static final Color MAROON = UIBuilder.MAROON;
    private static final Color GOLD   = UIBuilder.GOLD;
    private static final Color BG     = new Color(255, 255, 255);
    private static final Color BORDER = UIBuilder.BORDER;
    private static final Color TEXT_MAIN = UIBuilder.TEXT_MAIN;
    private static final Color TEXT_DIM  = UIBuilder.TEXT_DIM;

    public static void show(JFrame owner, String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel glass = (JPanel) owner.getGlassPane();
            glass.setLayout(null);
            glass.setVisible(true);

            // ── Toast card ────────────────────────────────────
            JPanel toast = new JPanel(new BorderLayout(0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    // White card
                    g2.setColor(BG);
                    g2.fill(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), 12, 12));
                    // Gold left accent bar
                    g2.setColor(MAROON);
                    g2.fill(new RoundRectangle2D.Double(0, 0, 5, getHeight(), 4, 4));
                    // Subtle shadow border
                    g2.setColor(BORDER);
                    g2.draw(new RoundRectangle2D.Double(
                        0.5, 0.5, getWidth()-1, getHeight()-1, 12, 12));
                    g2.dispose();
                }
            };
            toast.setOpaque(false);

            // ── Gold top stripe ───────────────────────────────
            JPanel goldStripe = new JPanel();
            goldStripe.setBackground(GOLD);
            goldStripe.setPreferredSize(new Dimension(0, 3));

            // Rounded top for stripe — just a plain panel is fine
            JPanel topWrap = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(GOLD);
                    g2.fill(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight() * 2, 12, 12));
                    g2.dispose();
                }
            };
            topWrap.setOpaque(false);
            topWrap.setPreferredSize(new Dimension(0, 3));

            // ── Body ──────────────────────────────────────────
            JPanel body = new JPanel(new BorderLayout(10, 0));
            body.setOpaque(false);
            body.setBorder(new EmptyBorder(12, 16, 12, 16));

            // Icon — maroon circle with "!"
            JPanel icon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = 30;
                    int x = (getWidth()  - s) / 2;
                    int y = (getHeight() - s) / 2;
                    // Soft maroon bg
                    g2.setColor(new Color(138, 26, 19, 20));
                    g2.fillOval(x - 3, y - 3, s + 6, s + 6);
                    // Solid circle
                    g2.setColor(MAROON);
                    g2.fillOval(x, y, s, s);
                    // "!" text
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("!",
                        x + (s - fm.stringWidth("!")) / 2,
                        y + (s + fm.getAscent() - fm.getDescent()) / 2 - 1);
                    g2.dispose();
                }
            };
            icon.setOpaque(false);
            icon.setPreferredSize(new Dimension(38, 38));

            // Text
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
            textPanel.setOpaque(false);

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitle.setForeground(MAROON);

            JLabel lblMsg = new JLabel(message);
            lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblMsg.setForeground(TEXT_DIM);

            textPanel.add(lblTitle);
            textPanel.add(lblMsg);

            body.add(icon,       BorderLayout.WEST);
            body.add(textPanel,  BorderLayout.CENTER);

            toast.add(topWrap, BorderLayout.NORTH);
            toast.add(body,    BorderLayout.CENTER);

            // ── Size + top-right position ─────────────────────
            int tw = 310, th = 70;
            int tx = owner.getWidth() - tw - 20;
            int endY   = 20;
            int startY = -th;

            toast.setBounds(tx, startY, tw, th);
            glass.add(toast);
            glass.revalidate();
            glass.repaint();

            // ── Slide in (ease out) ───────────────────────────
            final int[] curY = {startY};
            Timer slideIn = new Timer(10, null);
            slideIn.addActionListener(e -> {
                int dist = endY - curY[0];
                curY[0] += Math.max(1, dist / 4); // ease out
                toast.setBounds(tx, curY[0], tw, th);
                glass.repaint();
                if (curY[0] >= endY) {
                    ((Timer) e.getSource()).stop();
                    // Hold 3.5s
                    Timer hold = new Timer(3500, null);
                    hold.setRepeats(false);
                    hold.addActionListener(he -> {
                        // Slide out (ease in)
                        Timer slideOut = new Timer(10, null);
                        slideOut.addActionListener(se -> {
                            curY[0] -= Math.max(2, (curY[0] + th) / 3);
                            toast.setBounds(tx, curY[0], tw, th);
                            glass.repaint();
                            if (curY[0] <= -th) {
                                ((Timer) se.getSource()).stop();
                                glass.remove(toast);
                                glass.revalidate();
                                glass.repaint();
                                if (glass.getComponentCount() == 0)
                                    glass.setVisible(false);
                            }
                        });
                        slideOut.start();
                    });
                    hold.start();
                }
            });
            slideIn.start();
        });
    }
}
