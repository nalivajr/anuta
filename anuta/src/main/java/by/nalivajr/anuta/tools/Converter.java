package by.nalivajr.anuta.tools;

import by.nalivajr.anuta.exceptions.ConversionException;
import by.nalivajr.anuta.exceptions.InvalidDataTypeException;
import by.nalivajr.anuta.exceptions.ObjectDeserializationException;
import by.nalivajr.anuta.exceptions.ObjectSerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
class Converter {

    /**
     * Coverts value to byte array cheking if it compatible with strategy of storing data for the given field
     * @throws ObjectSerializationException if serialization has failed
     */
    static byte[] toByteArray(Field field, Object val) {
        if (!(val instanceof Serializable)) {
            throw new InvalidDataTypeException(field);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(val);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ObjectSerializationException(field);
        }
    }

    /**
     * Reads data from bytes and tries to covert it to the type of the given field
     * @throws ObjectDeserializationException if deserialization has failed
     */
    static Object readObject(Field field, byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (IOException e) {
            throw new ObjectDeserializationException(field);
        } catch (ClassNotFoundException e) {
            throw new ObjectDeserializationException(field);
        }
    }

    /**
     * Coverts list of numbers to byte array
     * @param numbers source numbers list
     * @return array containing byte values of source list
     */
    static byte[] numbersListToBytes(List<Number> numbers) {
        byte[] converted = new byte[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            converted[i] = (numbers.get(i)).byteValue();
        }
        return converted;
    }


    /**
     * Converts string to enum constant
     * @param val source string representing constant name
     * @param type target enum type
     */
    static Enum getStringAsEnum(String val, Class<?> type) {
        Enum[] enumConstants = (Enum[]) type.getEnumConstants();
        for (Enum e : enumConstants) {
            if (e.name().equals(val)) {
                return e;
            }
        }
        throw new ConversionException(val, type);
    }
}
