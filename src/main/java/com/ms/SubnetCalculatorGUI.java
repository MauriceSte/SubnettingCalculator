package com.ms;

import com.ms.IPAdress;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SubnetCalculatorGUI extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private final JTextArea resultArea = new JTextArea();

    private JTextField equalNetworkField;
    private JTextField subnetCountField;

    private JTextField vlsmNetworkField;
    private JTextField categoryCountField;

    public SubnetCalculatorGUI() {
        setTitle("Subnetzrechner");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createSelectionPanel(), "menu");
        mainPanel.add(createEqualPanel(), "equal");
        mainPanel.add(createVLSMPanel(), "vlsm");

        add(mainPanel, BorderLayout.CENTER);

        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        cardLayout.show(mainPanel, "menu");
    }

    // ===============================
    // Auswahlmenü
    // ===============================
    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton equalBtn = new JButton("Gleich große Subnetze");
        JButton vlsmBtn = new JButton("VLSM");
        JButton exitBtn = new JButton("Beenden");

        equalBtn.addActionListener(e -> cardLayout.show(mainPanel, "equal"));
        vlsmBtn.addActionListener(e -> cardLayout.show(mainPanel, "vlsm"));
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(equalBtn);
        panel.add(vlsmBtn);
        panel.add(exitBtn);

        return panel;
    }

    // ===============================
    // Gleich große Subnetze
    // ===============================
    private JPanel createEqualPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        equalNetworkField = new JTextField("192.168.1.0");
        subnetCountField = new JTextField();

        JButton calcBtn = new JButton("Berechnen");
        JButton backBtn = new JButton("Zurück");

        calcBtn.addActionListener(e -> calculateEqualSubnets());
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        panel.add(new JLabel("Netzwerkadresse:"));
        panel.add(equalNetworkField);
        panel.add(new JLabel("Anzahl Subnetze:"));
        panel.add(subnetCountField);
        panel.add(calcBtn);
        panel.add(backBtn);

        return panel;
    }

    private void calculateEqualSubnets() {
        try {
            IPAdress network = parseIP(equalNetworkField.getText());
            int subnetCount = Integer.parseInt(subnetCountField.getText());

            int bitsNeeded = (int) Math.ceil(Math.log(subnetCount) / Math.log(2));
            int newPrefix = network.getNetworkBits() + bitsNeeded;
            int blockSize = (int) Math.pow(2, 32 - newPrefix);

            StringBuilder sb = new StringBuilder();
            sb.append("Gleich große Subnetze\n");
            sb.append("Neuer Prefix: /").append(newPrefix).append("\n");
            sb.append("Blockgröße: ").append(blockSize).append("\n\n");

            int baseInt = ipToInt(network);

            for (int i = 0; i < subnetCount; i++) {
                int subnetInt = baseInt + (i * blockSize);
                IPAdress subnet = intToIP(subnetInt);
                subnet.setNetworkBits(newPrefix);
                sb.append("Subnet ").append(i + 1).append(": ").append(subnet).append("\n");
            }

            resultArea.setText(sb.toString());

        } catch (Exception ex) {
            showError();
        }
    }

    // ===============================
    // VLSM
    // ===============================
    private JPanel createVLSMPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        vlsmNetworkField = new JTextField("192.168.1.0");
        categoryCountField = new JTextField();

        JButton calcBtn = new JButton("Berechnen");
        JButton backBtn = new JButton("Zurück");

        calcBtn.addActionListener(e -> calculateVLSM());
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        panel.add(new JLabel("Netzwerkadresse:"));
        panel.add(vlsmNetworkField);
        panel.add(new JLabel("Anzahl Kategorien:"));
        panel.add(categoryCountField);
        panel.add(calcBtn);
        panel.add(backBtn);

        return panel;
    }

    private void calculateVLSM() {
        try {
            IPAdress network = parseIP(vlsmNetworkField.getText());
            int categories = Integer.parseInt(categoryCountField.getText());

            Map<String, Integer> requests = new LinkedHashMap<>();

            for (int i = 0; i < categories; i++) {
                String name = JOptionPane.showInputDialog(this, "Name der Kategorie:");
                int hosts = Integer.parseInt(
                        JOptionPane.showInputDialog(this, "Benötigte Hosts für " + name + ":"));
                requests.put(name, hosts);
            }

            List<Map.Entry<String, Integer>> sorted =
                    new ArrayList<>(requests.entrySet());

            sorted.sort((a, b) -> b.getValue() - a.getValue());

            StringBuilder sb = new StringBuilder();
            sb.append("VLSM Berechnung\n\n");

            int currentAddress = ipToInt(network);

            for (Map.Entry<String, Integer> entry : sorted) {
                int neededHosts = entry.getValue();
                int neededBits = (int) Math.ceil(Math.log(neededHosts + 2) / Math.log(2));
                int prefix = 32 - neededBits;
                int blockSize = (int) Math.pow(2, neededBits);

                IPAdress subnet = intToIP(currentAddress);
                subnet.setNetworkBits(prefix);

                sb.append(entry.getKey()).append("\n");
                sb.append("Hosts benötigt: ").append(neededHosts).append("\n");
                sb.append("Prefix: /").append(prefix).append("\n");
                sb.append("Netz: ").append(subnet).append("\n\n");

                currentAddress += blockSize;
            }

            resultArea.setText(sb.toString());

        } catch (Exception ex) {
            showError();
        }
    }

    // ===============================
    // Hilfsmethoden
    // ===============================
    private IPAdress parseIP(String input) {
        String[] parts = input.split("/");
        String[] octetsStr = parts[0].split("\\.");

        int[] octets = new int[4];
        for (int i = 0; i < 4; i++) {
            octets[i] = Integer.parseInt(octetsStr[i]);
        }

        IPAdress ip = new IPAdress(octets);

        if (parts.length == 2) {
            ip.setNetworkBits(Integer.parseInt(parts[1]));
        } else {
            ip.setNetworkBits(24); // Standard /24 falls nichts angegeben
        }

        return ip;
    }

    private int ipToInt(IPAdress ip) {
        int[] o = ip.getOctets();
        return (o[0] << 24) | (o[1] << 16) | (o[2] << 8) | o[3];
    }

    private IPAdress intToIP(int value) {
        int[] o = new int[4];
        o[0] = (value >> 24) & 0xFF;
        o[1] = (value >> 16) & 0xFF;
        o[2] = (value >> 8) & 0xFF;
        o[3] = value & 0xFF;
        return new IPAdress(o);
    }

    private void showError() {
        JOptionPane.showMessageDialog(this,
                "Ungültige Eingabe!",
                "Fehler",
                JOptionPane.ERROR_MESSAGE);
    }

    // ===============================
    // Main
    // ===============================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SubnetCalculatorGUI().setVisible(true));
    }
}