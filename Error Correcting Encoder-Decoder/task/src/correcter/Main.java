package correcter;

import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static final int numberOfCharsInSegment = 3;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String userChoice = scanner.next();

        switch (userChoice) {
            case "encode":
                encode();
                break;
            case "send":
                send();
                break;
            case "decode":
                decode();
                break;
            default:
                System.out.println("No valid choice made.");
        }

    }

    // Stage 5
    static void encode() throws IOException {
        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./send.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./encoded.txt"));

        int inByte = streamInput.read();
        int outByte = 0;

        while (inByte != -1) {

            // first half of inbound byte
            outByte = copyBitBetweenNums(inByte, outByte, 8, 6);
            outByte = copyBitBetweenNums(inByte, outByte, 7, 4);
            outByte = copyBitBetweenNums(inByte, outByte, 6, 3);
            outByte = copyBitBetweenNums(inByte, outByte, 5, 2);
            outByte = addParityBits(outByte);
            outputStream.write(outByte);
            outByte = 0;

            // second half of inbound byte
            outByte = copyBitBetweenNums(inByte, outByte, 4, 6);
            outByte = copyBitBetweenNums(inByte, outByte, 3, 4);
            outByte = copyBitBetweenNums(inByte, outByte, 2, 3);
            outByte = copyBitBetweenNums(inByte, outByte, 1, 2);
            outByte = addParityBits(outByte);
            outputStream.write(outByte);
            outByte = 0;

            inByte = streamInput.read();
        }

        streamInput.close();
        outputStream.close();
    }

    static void send() throws IOException {
        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./encoded.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./received.txt"));

        int inByte = streamInput.read();

        while (inByte != -1) {
            outputStream.write(changeOneBitOnInt(inByte));
            inByte = streamInput.read();
        }

        streamInput.close();
        outputStream.close();
    }

    static void decode() throws IOException {
        // https://www.youtube.com/watch?v=373FUw-2U2k

        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./received.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./decoded.txt"));

        int inByte = streamInput.read();
        int inByte2 = streamInput.read();
        int outByte = 0;

        while (inByte != -1 || inByte2 != -1) {
            // first inByte into outByte
            int errorBit = findErrorBitPosition(inByte);
            inByte = fixErrorBit(inByte, errorBit);
            outByte = copyBitBetweenNums(inByte, outByte, 6, 8);
            outByte = copyBitBetweenNums(inByte, outByte, 4, 7);
            outByte = copyBitBetweenNums(inByte, outByte, 3, 6);
            outByte = copyBitBetweenNums(inByte, outByte, 2, 5);

            // second inByte to outByte
            errorBit = findErrorBitPosition(inByte2);
            inByte2 = fixErrorBit(inByte2, errorBit);
            outByte = copyBitBetweenNums(inByte2, outByte, 6, 4);
            outByte = copyBitBetweenNums(inByte2, outByte, 4, 3);
            outByte = copyBitBetweenNums(inByte2, outByte, 3, 2);
            outByte = copyBitBetweenNums(inByte2, outByte, 2, 1);

            outputStream.write(outByte);
            outByte = 0;

            inByte = streamInput.read();
            inByte2 = streamInput.read();
        }

        streamInput.close();
        outputStream.close();
    }

    static int fixErrorBit(int num, int position) {
        int fixedNum = num;

        if (getNthBitOfNum(num, position) == 1 && position != 0) {
            fixedNum = clearNthBitOfNum(num, position);
        } else {
            fixedNum = setNthBitOfNum(num, position);
        }

        return fixedNum;
    }

    static int findErrorBitPosition(int num) {
        int calculatedBits = addParityBits(num);
        int bit1 = compareParityBit(num, calculatedBits, 8) ? 0 : 1;
        int bit2 = compareParityBit(num, calculatedBits, 7) ? 0 : 1;
        int bit4 = compareParityBit(num, calculatedBits, 5) ? 0 : 1;

        int formulaBit = bit1 + bit2 * 2 + bit4 * 4;
        int realBit = Math.abs(formulaBit - 9);

        return realBit;
    }

    static boolean compareParityBit(int num, int calculatedBits, int position) {
        boolean match = true;

        if (getNthBitOfNum(num, position) != getNthBitOfNum(calculatedBits, position)) {
            match = false;
        }

        return match;
    }

    static int addParityBits(int num) {
        // https://www.youtube.com/watch?v=373FUw-2U2k

        if (calculateParityBit1(num) == 1) {
            num = setNthBitOfNum(num, 8);
        } else {
            num = clearNthBitOfNum(num, 8);
        }

        if (calculateParityBit2(num) == 1) {
            num = setNthBitOfNum(num, 7);
        } else {
            num = clearNthBitOfNum(num, 7);
        }

        if (calculateParityBit4(num) == 1) {
            num = setNthBitOfNum(num, 5);
        } else {
            num = clearNthBitOfNum(num, 5);
        }

        return num;
    }

    static int calculateParityBit1(int num) {
        int bit3 = getNthBitOfNum(num, 6);
        int bit5 = getNthBitOfNum(num, 4);
        int bit7 = getNthBitOfNum(num, 2);

        return (bit3 + bit5 + bit7) % 2;
    }

    static int calculateParityBit2(int num) {
        int bit3 = getNthBitOfNum(num, 6);
        int bit6 = getNthBitOfNum(num, 3);
        int bit7 = getNthBitOfNum(num, 2);

        return (bit3 + bit6 + bit7) % 2;
    }

    static int calculateParityBit4(int num) {
        int bit5 = getNthBitOfNum(num, 4);
        int bit6 = getNthBitOfNum(num, 3);
        int bit7 = getNthBitOfNum(num, 2);

        return (bit5 + bit6 + bit7) % 2;
    }

    static int copyBitBetweenNums(int fromNum, int toNum,
                                  int fromBitPosition, int toBitPosition) {
        int newNum = toNum;

        if (getNthBitOfNum(fromNum, fromBitPosition) == 1) {
            newNum = setNthBitOfNum(newNum, toBitPosition);
        } else {
            newNum = clearNthBitOfNum(newNum, toBitPosition);
        }

        return newNum;
    }

    // Stage 4
    static void stage4() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String userChoice = scanner.next();

        switch (userChoice) {
            case "encode":
                encodeS4();
                break;
            case "send":
                sendS4();
                break;
            case "decode":
                decodeS4();
                break;
            default:
                System.out.println("No valid choice made.");
        }

    }

    static void encodeS4() throws IOException {
        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./send.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./encoded.txt"));

        int inByte = streamInput.read();
        int inBitTracker = 8;
        int outByte = 0;
        int outBitTracker = 8;

        while (inByte != -1) {

            while (inBitTracker > 0) {
                // checking if out bit is full and if so, writing it out
                if (outBitTracker == 2) {
                    outByte = addParityBit(outByte);
                    outputStream.write(outByte);
                    outByte = 0;
                    outBitTracker = 8;
                }

                // If there is a one in bit position of inbound byte, double out in
                // outbound byte, otherwise will leave 0s in those bits
                if (getNthBitOfNum(inByte, inBitTracker) == 1) {
                    outByte = setNthBitOfNum(outByte, outBitTracker);
                    outByte = setNthBitOfNum(outByte, outBitTracker - 1);
                }

                outBitTracker -= 2;
                inBitTracker--;
            }

            inByte = streamInput.read();
            inBitTracker = 8;
        }

        // checking last byte to flush to file
        if (outByte >= 1) {
            outByte = addParityBit(outByte);
            outputStream.write(outByte);
        }

        streamInput.close();
        outputStream.close();
    }

    static void sendS4() throws IOException {
        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./encoded.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./received.txt"));

        int inByte = streamInput.read();

        while (inByte != -1) {
            outputStream.write(changeOneBitOnInt(inByte));
            inByte = streamInput.read();
        }

        streamInput.close();
        outputStream.close();
    }

    static void decodeS4() throws IOException {
        BufferedInputStream streamInput =
                new BufferedInputStream(new FileInputStream("./received.txt"));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream("./decoded.txt"));

        int inByte = streamInput.read();
        boolean errorBitFound = false;
        int errorBitPairLocation = 0;

        int tempByte;
        int outByte = 0;
        int outByteBitTracker = 8;

        // go through each byte
        while (inByte != -1) {
            // go through byte to find error
            for (int i = 8; i > 0; i -= 2) {
                int bit = getNthBitOfNum(inByte, i);
                int bitMinus1 = getNthBitOfNum(inByte, i - 1);
                if (bit != bitMinus1) {
                    errorBitFound = true;
                    errorBitPairLocation = i;
                    break;
                }
            }

            // fixing error and storing temporarily in memory
            if (errorBitFound && errorBitPairLocation > 2) {
                tempByte = fixNum(inByte, errorBitPairLocation);
            } else {
                tempByte = inByte;
            }

            // for each other bit in temp byte (excluding parity bit)
            for (int i = 8; i > 2; i -= 2) {
                // checking if outbyte is full and if so flushing
                if (outByteBitTracker == 0) {
                    outputStream.write(outByte);
                    outByte = 0;
                    outByteBitTracker = 8;
                }

                // Updating bit if it is 1 in input byte
                if (getNthBitOfNum(tempByte, i) == 1) {
                    outByte = setNthBitOfNum(outByte, outByteBitTracker);
                }

                outByteBitTracker--;
            }

            errorBitFound = false;
            errorBitPairLocation = 0;

            inByte = streamInput.read();
        }

        if (outByte > 0) {
            outputStream.write(outByte);
        }

        streamInput.close();
        outputStream.close();
    }

    static int fixNum(int num, int errBitPairLoc) {
        int parityBit = getNthBitOfNum(num, 1);
        int otherBitsSum = 0;
        int newNum;

        for (int i = 8; i > 2; i -= 2) {
            if(i != errBitPairLoc) {
                otherBitsSum += getNthBitOfNum(num, i);
            }
        }

        if (otherBitsSum % 2 == parityBit) {
            newNum = clearNthBitOfNum(num, errBitPairLoc);
            newNum = clearNthBitOfNum(newNum, errBitPairLoc - 1);
        } else {
            newNum = setNthBitOfNum(num, errBitPairLoc);
            newNum = setNthBitOfNum(newNum, errBitPairLoc - 1);
        }

        return newNum;
    }

    static int addParityBit(int num) {
        int parityBit = calculateParityBit(num);

        if (parityBit == 1) {
            num = setNthBitOfNum(num, 2);
            num = setNthBitOfNum(num, 1);
        }

        return num;
    }

    static int calculateParityBit(int num) {
        // starting from left
        int firstPair = getNthBitOfNum(num, 8);
        int secondPair = getNthBitOfNum(num, 6);
        int thirdPair = getNthBitOfNum(num, 4);
        return (firstPair + secondPair + thirdPair) % 2;
    }

    static int getNthBitOfNum(int num, int bit) {
        int bitNum = getNumWithBitSet(bit);
        int checkNum = num & bitNum;
        return checkNum > 0 ? 1 : 0;
    }

    static int setNthBitOfNum(int num, int bit) {
        int bitNum = getNumWithBitSet(bit);
        return num | bitNum;
    }
    
    static int clearNthBitOfNum(int num, int bit) {
        int bitNum = ~getNumWithBitSet(bit);
        return num & bitNum;
    }

    static int getNumWithBitSet(int bit) {
        int newNum = 1;
        return newNum << (bit - 1);
    }

    // Stage 3
    static void stage3() throws IOException {
        File sendFile = new File("./send.txt");
        File file = new File("./received.txt");
        FileWriter writer = new FileWriter(file);

        BufferedReader reader = new BufferedReader(new FileReader(sendFile));
        int current = reader.read();

        while (current != -1) {
            int temp = changeOneBitOnInt(current);
            writer.write(temp);

            current = reader.read();
        }

        reader.close();
        writer.close();
    }

    static int getRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    static int getSingleOneByteAsInt() {
        int[] onlyOneBitIsOne = new int[]{1, 2, 4, 8, 16, 32, 64};
        int randomBit = getRandomInt(1, 7);
        return onlyOneBitIsOne[randomBit];
    }

    static int changeOneBitOnInt(int intToChange) {
        int randomInt = getSingleOneByteAsInt();
        int result;

        if ((intToChange | randomInt) != intToChange) {
            result = intToChange | randomInt;
        } else {
            result = intToChange & ~randomInt;
        }

        return result;
    }


    // Stage 2
    static void stage2(String userInput) {
        System.out.println(userInput);
        String encodedMessage = encodeMessage(userInput);
        System.out.println(encodedMessage);
        String errorString = emulateErrors(encodedMessage);
        System.out.println(errorString);
        String decodedMessage = decodeMessage(errorString);
        System.out.println(decodedMessage);
    }

    static String encodeMessage(String message) {
        StringBuilder encodedMessage = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            for (int j = 0; j < numberOfCharsInSegment; j++) {
                encodedMessage.append(message.charAt(i));
            }
        }

        return encodedMessage.toString();
    }

    static String emulateErrors(String message) {
        Random rand = new Random();
        StringBuilder newText = new StringBuilder(message);

        char[] validErrorCharacters = {' ', '1', '2', '3', '4',
                '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e',
                'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
                'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z'};

        for (int i = 0; i < message.length(); i += numberOfCharsInSegment) {
            // getting random error characters
            int randInt = rand.nextInt(validErrorCharacters.length);
            char randomChar = validErrorCharacters[randInt];
            int randomCharLocation;

            if (message.length() - i < numberOfCharsInSegment) {
                // last segment may be less than segment length
                randomCharLocation = rand.nextInt(message.length() - i);
            } else {
                randomCharLocation = rand.nextInt(numberOfCharsInSegment);

            }

            // Adding check to make sure random char isn't same as letter it's replacing
            while (newText.charAt(i + randomCharLocation) == randomChar) {
                randInt = rand.nextInt(validErrorCharacters.length);
                randomChar = validErrorCharacters[randInt];
            }

            newText.replace(i + randomCharLocation, i + randomCharLocation + 1, String.valueOf(randomChar));
        }

        return newText.toString();
    }

    static String decodeMessage(String message) {
        StringBuilder decodedMessage = new StringBuilder();

        for (int i = 0; i < message.length(); i += numberOfCharsInSegment) {
            HashMap<Character, Integer> letterCounts = new HashMap<>();

            // Getting count of each letter in a segment
            for (int j = 0; j < numberOfCharsInSegment; j++) {
                char currentChar = message.charAt(i + j);
                if(letterCounts.containsKey(currentChar)) {
                    letterCounts.put(currentChar, letterCounts.get(currentChar) + 1);
                } else {
                    letterCounts.put(currentChar, 1);
                }
            }

            // Getting letter with highest count
            char highestCountLetter = ' ';
            int highestCount = 0;

            for (char currentChar : letterCounts.keySet()) {
                if (letterCounts.get(currentChar) > highestCount) {
                    highestCountLetter = currentChar;
                    highestCount = letterCounts.get(currentChar);
                }
            }

            decodedMessage.append(highestCountLetter);
        }

        return decodedMessage.toString();
    }
}
