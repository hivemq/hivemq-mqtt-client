package org.mqttbee.mqtt3.codec;

public class Mqtt3DataTypes {


    /**
     * Calculates the byte count of the given value encoded as a variable byte integer.
     * <p>
     * This method does not check if the value is in range of a 4 byte variable byte integer.
     *
     * @param value the value to calculate the encoded length for.
     * @return the encoded length of the value.
     */
    public static int encodedVariableByteIntegerLength(int value) {
        int length = 1;

        while (value >= 1 << 7) {
            length++;
            value = value >> 7;
        }
        return length;
    }


}
