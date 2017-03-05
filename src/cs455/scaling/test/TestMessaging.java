package cs455.scaling.test;

import cs455.scaling.hash.ComputeHash;

import java.util.Random;

public class TestMessaging {
    private Long testLong2 = new Long(34);
    private Byte testByte = new Byte(Byte.MAX_VALUE);
    private static byte[] testArray;

    private void printPrimitiveSizes() {
        System.out.println(testLong2.SIZE/8);
        System.out.println(testByte.SIZE/8);
    }

    private byte[] prepareMessage() {
        byte[] byteArray = new byte[8000];
        new Random().nextBytes(byteArray);
        return byteArray;
    }

    private void testHash(byte[] data) {
        System.out.println(ComputeHash.SHA1FromBytes(data));
        byte[] bytes = ComputeHash.SHA1FromBytes(data).getBytes();
        String backToString = new String(bytes);
        System.out.println(backToString);
    }

    public static void main(String[] args) {
        TestMessaging testMessaging = new TestMessaging();
        testArray = testMessaging.prepareMessage();
        testMessaging.testHash(testArray);
    }
}
