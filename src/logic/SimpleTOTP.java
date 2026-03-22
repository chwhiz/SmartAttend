// SIMPLETOTP LIBRARY
// This is a simple implementation of TOTP (Time-Based One-Time Password) generator in Java.
// It uses HMAC-SHA1 algorithm and a base32 secret key to generate a 6-digit code that changes every 30 seconds.
// Authors: Guillergan Gabriel Martin G., ICT 11-02

package logic;

import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SimpleTOTP {

    /**
     * Generates a 6-digit Time-Based One-Time Password (TOTP) from a given base32 secret.
     * Evaluates against current system time (divided by 30 second epochs).
     *
     * @param secretKey The base-32 secret specific to the user
     * @return 6-digit TOTP string, padded with zeroes if necessary
     */
    public static String getTOTPCode(String secretKey) {
        try {
            // hatiin ang current time sa 30 para makuha ang current epoch. (eto talaga logic ng basic TOTP ahaha)
            long timeIndex = System.currentTimeMillis() / 1000 / 30;
            
            // decode yung string to actual bytes kasi b32 yan 
            byte[] secretBytes = decodeBase32(secretKey);
            if (secretBytes.length == 0) return "000000"; // fallback if tanga ako/yung user lol

            // kailangan pasok sa 8 byte array para pang HmacSHA1 later
            byte[] data = new byte[8];
            for (int i = 8; i-- > 0; timeIndex >>>= 8)
                data[i] = (byte) timeIndex;

            // sign natin using HMAC-SHA1. wag nyo tatanungin kung bakit ganito syntax, google ko lang to hahaha
            SecretKeySpec signKey = new SecretKeySpec(secretBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            // bitwise magic para maka-kuhang index sa dulo tapos yun ang offset
            int offset  = hash[hash.length - 1] & 0xF;
            long binary = ((hash[offset]     & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        |  (hash[offset + 3] & 0xff);
            
            // then mod natin with 1m para mapwersang 6 digits lang
            long otp = binary % 1_000_000;

            String result = Long.toString(otp);
            
            // lagyan ng zero pad sa unahan pag kulang 6 digits (halimbawa, lumabas '91' lang as TOTP lol)
            while (result.length() < 6) result = "0" + result;
            return result;

        } catch (Exception e) { return "000000"; }
    }

    /**
     * Decodes Base32 string to standard bytes needed by HMAC.
     */
    private static byte[] decodeBase32(String value) {
        if (value == null) return new byte[0];
        
        // tangglin lahat ng space at equal symbols if meron, then upper case
        String cleaned = value.trim().replace("=", "").replace(" ", "").toUpperCase();
        if (cleaned.isEmpty()) return new byte[0];

        List<Byte> output = new ArrayList<>();
        int buffer = 0, bitsLeft = 0;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            int index;
            // parang manual switch-case to determine index mapping
            if      (c >= 'A' && c <= 'Z') index = c - 'A';
            else if (c >= '2' && c <= '7') index = 26 + (c - '2');
            else continue; // if invalid char, let's pretend it never happened :))

            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            
            // pag may 1 byte/8 bits na tayo, extract lang tapos reset onting bits
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output.add((byte) ((buffer >> bitsLeft) & 0xFF));
            }
        }

        // pasok sa byte array
        byte[] result = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) result[i] = output.get(i);
        return result;
    }
}
