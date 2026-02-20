package com.ms;

import java.util.*;

public class SubnetCalculator {

    // Berechnung von gleich großen Subnetzen
    public static void calculateEqualSizedSubnets(IPAdress networkAddress, int subnetCount) {
        int bitsForSubnets = (int) (Math.ceil(Math.log(subnetCount) / Math.log(2)));
        int totalHostsPerSubnet = (int) Math.pow(2, 32 - bitsForSubnets) - 2; // -2 für Netzwerk- und Broadcast-Adresse
        int subnetMask = 32 - bitsForSubnets;

        networkAddress.setNetworkBits(subnetMask);

        System.out.println("Berechnung von " + subnetCount + " gleich großen Subnetzen");
        System.out.println("Subnetzmaske: /" + subnetMask);
        System.out.println("Anzahl Hosts pro Subnetz: " + totalHostsPerSubnet);

        // Zeige die Subnetz-Adressen
        int networkInt = ipToInt(networkAddress);
        for (int i = 0; i < subnetCount; i++) {
            int subnetAddress = networkInt + (i * totalHostsPerSubnet);
            IPAdress subnet = intToIp(subnetAddress);
            System.out.println("Subnetz " + (i + 1) + ": " + subnet);
        }
    }

    // Berechnung von VLSM Subnetzen
    public static void calculateVLSM(IPAdress networkAddress, Map<String, Integer> subnets) {
        List<Map.Entry<String, Integer>> sortedSubnets = new ArrayList<>(subnets.entrySet());
        sortedSubnets.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // Sortiere nach Hosts in absteigender Reihenfolge

        int networkInt = ipToInt(networkAddress);
        System.out.println("VLSM Subnetzberechnung");

        for (Map.Entry<String, Integer> entry : sortedSubnets) {
            String subnetName = entry.getKey();
            int hostsRequired = entry.getValue();
            int bitsRequired = (int) Math.ceil(Math.log(hostsRequired + 2) / Math.log(2)); // +2 für Netzwerk- und Broadcast-Adresse
            int subnetMask = 32 - bitsRequired;
            int totalHosts = (int) Math.pow(2, bitsRequired) - 2;

            System.out.println("Subnetz Name: " + subnetName);
            System.out.println("Mindestanzahl Hosts: " + hostsRequired);
            System.out.println("Subnetzmaske: /" + subnetMask);
            System.out.println("Anzahl Hosts im Subnetz: " + totalHosts);

            // Berechne das Subnetz
            int subnetAddress = networkInt;
            networkInt += totalHosts + 2; // Erhöhe die Netzwerkadresse um die Anzahl Hosts + 2 (Netzwerk- und Broadcastadresse)
            IPAdress subnet = intToIp(subnetAddress);
            System.out.println("Subnetz Adresse: " + subnet);
            System.out.println();
        }
    }

    // Hilfsmethode zur Umwandlung einer IP-Adresse in eine Integer
    public static int ipToInt(IPAdress ipAddress) {
        int[] octets = ipAddress.getOctets();
        return (octets[0] << 24) | (octets[1] << 16) | (octets[2] << 8) | octets[3];
    }

    // Hilfsmethode zur Umwandlung eines Integers in eine IP-Adresse
    public static IPAdress intToIp(int ip) {
        int[] octets = new int[4];
        octets[0] = (ip >> 24) & 0xFF;
        octets[1] = (ip >> 16) & 0xFF;
        octets[2] = (ip >> 8) & 0xFF;
        octets[3] = ip & 0xFF;
        return new IPAdress(octets);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Subnetzberechnung Tool");
        System.out.println("Wählen Sie eine der folgenden Optionen:");
        System.out.println("1. Gleich große Subnetze (Anzahl Subnetze oder Hosts)");
        System.out.println("2. VLSM (Variable Length Subnet Mask)");

        int option = scanner.nextInt();
        scanner.nextLine();  // Konsumiere den verbleibenden Zeilenumbruch

        if (option == 1) {
            // Option für gleich große Subnetze
            System.out.print("Geben Sie die Netzwerkadresse (z.B. 192.168.1.0) ein: ");
            String[] networkAddressString = scanner.nextLine().split("\\.");
            int[] networkOctets = Arrays.stream(networkAddressString).mapToInt(Integer::parseInt).toArray();
            IPAdress networkAddress = new IPAdress(networkOctets);

            System.out.print("Geben Sie die Anzahl der Subnetze ein: ");
            int subnetCount = scanner.nextInt();
            calculateEqualSizedSubnets(networkAddress, subnetCount);

        } else if (option == 2) {
            // Option für VLSM
            System.out.print("Geben Sie die Netzwerkadresse (z.B. 192.168.1.0) ein: ");
            String[] networkAddressString = scanner.nextLine().split("\\.");
            int[] networkOctets = Arrays.stream(networkAddressString).mapToInt(Integer::parseInt).toArray();
            IPAdress networkAddress = new IPAdress(networkOctets);

            Map<String, Integer> subnets = new LinkedHashMap<>();
            System.out.print("Geben Sie die Anzahl der Hostkategorien ein: ");
            int categoriesCount = scanner.nextInt();
            scanner.nextLine(); // Konsumiere den Zeilenumbruch

            for (int i = 0; i < categoriesCount; i++) {
                System.out.print("Geben Sie den Namen der Kategorie ein: ");
                String name = scanner.nextLine();
                System.out.print("Geben Sie die Mindestanzahl an Hosts für " + name + " ein: ");
                int hosts = scanner.nextInt();
                scanner.nextLine(); // Konsumiere den Zeilenumbruch
                subnets.put(name, hosts);
            }

            calculateVLSM(networkAddress, subnets);

        } else {
            System.out.println("Ungültige Auswahl. Bitte starten Sie das Programm erneut.");
        }

        scanner.close();
    }
}