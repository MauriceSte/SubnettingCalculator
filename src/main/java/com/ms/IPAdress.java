package com.ms;

import java.util.stream.IntStream;

public class IPAdress {

    private final int[] octets;
    private int networkBits;

    public IPAdress(int[] octets) {
        this.octets = octets;
    }

    public void setNetworkBits(int amount) {
        networkBits = amount;
    }

    public int getNetworkBits() {
        return networkBits;
    }

    public int[] getOctets() {
        return octets;
    }

    public void setOctet(int index, int value) {
        octets[index] = value;
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s.%s/%s", octets[0], octets[1], octets[2], octets[3], networkBits);
    }

    public String toBinaryString() {
        String[] binOct = (String[]) IntStream.of(octets).mapToObj(Integer::toBinaryString).toArray();
        return String.format("%s.%s.%s.%s/%s", binOct[0], binOct[1], binOct[2], binOct[3], networkBits);
    }
}
