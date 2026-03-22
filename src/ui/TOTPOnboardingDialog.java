package ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Base64;

public class TOTPOnboardingDialog extends JDialog {

    private static final Color MAROON   = UIBuilder.MAROON;
    private static final Color GOLD     = UIBuilder.GOLD;
    private static final Color BG       = UIBuilder.BG;
    private static final Color CARD_BG  = UIBuilder.CARD_BG;
    private static final Color TEXT_DIM = UIBuilder.TEXT_DIM;
    private static final Color BORDER   = UIBuilder.BORDER;
    private static final Color SUCCESS  = UIBuilder.SUCCESS;

    private final String adminName;
    private final String secretKey;
    private boolean finished = false;

    public TOTPOnboardingDialog(Frame parent, String adminName) {
        super(parent, "Setup Authenticator", true);
        this.adminName = adminName;
        this.secretKey = generateSecretKey();

        setSize(580, 540);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(new LineBorder(BORDER, 1));

        // ── Title bar ──
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAROON);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel titleStack = new JPanel(new GridLayout(2, 1));
        titleStack.setBackground(MAROON);

        JLabel title = new JLabel("Authenticator Setup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Scan the QR code or enter the key manually");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(240, 200, 200));

        titleStack.add(title);
        titleStack.add(sub);
        bar.add(titleStack, BorderLayout.WEST);

        // REFACTORED: Use UIBuilder for standard gold bar
        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel barWrap = new JPanel(new BorderLayout());
        barWrap.add(bar,     BorderLayout.CENTER);
        barWrap.add(goldBar, BorderLayout.SOUTH);
        root.add(barWrap, BorderLayout.NORTH);

        // ── Body ──
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Welcome line
        JLabel lblWelcome = new JLabel("Setting up 2FA for: " + adminName);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWelcome.setForeground(MAROON);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        // QR code
        JLabel qrLabel = new JLabel();
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));

        try {
            String otpUri = buildOtpUri(adminName, secretKey);
            BufferedImage qrImage = generateQR(otpUri, 200, 200);
            qrLabel.setIcon(new ImageIcon(qrImage));
        } catch (Exception e) {
            qrLabel.setText("[QR generation failed]");
            qrLabel.setForeground(Color.RED);
        }

        // Instruction
        JLabel lblStep1 = new JLabel("Step 1: Open your Authenticator app");
        lblStep1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStep1.setForeground(new Color(40, 40, 40));
        lblStep1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStep2 = new JLabel("Step 2: Scan the QR code above");
        lblStep2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStep2.setForeground(TEXT_DIM);
        lblStep2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator divider = new JSeparator();
        divider.setForeground(BORDER);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel lblManual = new JLabel("Can't scan? Enter this key manually:");
        lblManual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblManual.setForeground(TEXT_DIM);
        lblManual.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Secret key display box
        JPanel keyBox = new JPanel(new BorderLayout(8, 0));
        keyBox.setBackground(new Color(245, 243, 240));
        keyBox.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        keyBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Format key as groups of 4 for readability: ABCD EFGH IJKL ...
        String formattedKey = formatKey(secretKey);
        JLabel lblKey = new JLabel(formattedKey);
        lblKey.setFont(new Font("Courier New", Font.BOLD, 14));
        lblKey.setForeground(MAROON);

        JButton btnCopy = new JButton("Copy");
        btnCopy.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCopy.setBackground(MAROON);
        btnCopy.setForeground(Color.WHITE);
        btnCopy.setOpaque(true);
        btnCopy.setBorderPainted(false);
        btnCopy.setFocusPainted(false);
        btnCopy.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(secretKey), null);
            btnCopy.setText("Copied!");
            btnCopy.setBackground(SUCCESS);
            new javax.swing.Timer(2000, ev -> {
                btnCopy.setText("Copy");
                btnCopy.setBackground(MAROON);
            }) {{ setRepeats(false); }}.start();
        });

        keyBox.add(lblKey,   BorderLayout.CENTER);
        keyBox.add(btnCopy,  BorderLayout.EAST);

        // Done button
        JButton btnDone = new JButton("I've added the account - Done");
        btnDone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDone.setBackground(SUCCESS);
        btnDone.setForeground(Color.WHITE);
        btnDone.setOpaque(true);
        btnDone.setBorderPainted(false);
        btnDone.setFocusPainted(false);
        btnDone.setBorder(new EmptyBorder(11, 20, 11, 20));
        btnDone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnDone.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDone.addActionListener(e -> {
            finished = true;
            dispose();
        });

        body.add(lblWelcome);
        body.add(Box.createVerticalStrut(16));
        body.add(qrLabel);
        body.add(Box.createVerticalStrut(10));
        body.add(lblStep1);
        body.add(Box.createVerticalStrut(4));
        body.add(lblStep2);
        body.add(Box.createVerticalStrut(14));
        body.add(divider);
        body.add(Box.createVerticalStrut(14));
        body.add(lblManual);
        body.add(Box.createVerticalStrut(8));
        body.add(keyBox);
        body.add(Box.createVerticalStrut(20));
        body.add(btnDone);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    // Generate a Base32 secret key (160-bit / 32 chars)
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    private static String base32Encode(byte[] data) {
        final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) sb.append(ALPHABET.charAt((buffer << (5 - bitsLeft)) & 31));
        return sb.toString();
    }

    private String buildOtpUri(String account, String secret) {
        // otpauth://totp/ISSUER:ACCOUNT?secret=SECRET&issuer=ISSUER
        String issuer  = "UPHMO%20SHS%20Attendance";
        String encoded = account.replace(" ", "%20");
        return "otpauth://totp/" + issuer + ":" + encoded
             + "?secret=" + secret
             + "&issuer=" + issuer
             + "&algorithm=SHA1&digits=6&period=30";
    }

    private BufferedImage generateQR(String content, int w, int h) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, w, h);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private String formatKey(String key) {
        // ABCDEFGHIJKLMNOP → ABCD EFGH IJKL MNOP
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(key.charAt(i));
        }
        return sb.toString();
    }

    public String  getSecretKey()  { return secretKey; }
    public boolean isFinished()    { return finished; }
}
