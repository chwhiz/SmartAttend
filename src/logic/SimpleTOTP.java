package logic;

import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SimpleTOTP {

    public static String getTOTPCode(String secretKey) {
        try {
            long timeIndex = System.currentTimeMillis() / 1000 / 30;
            byte[] secretBytes = decodeBase32(secretKey);
            if (secretBytes.length == 0) return "000000";

            byte[] data = new byte[8];
            for (int i = 8; i-- > 0; timeIndex >>>= 8)
                data[i] = (byte) timeIndex;

            SecretKeySpec signKey = new SecretKeySpec(secretBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset  = hash[hash.length - 1] & 0xF;
            long binary = ((hash[offset]     & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        |  (hash[offset + 3] & 0xff);
            long otp = binary % 1_000_000;

            String result = Long.toString(otp);
            while (result.length() < 6) result = "0" + result;
            return result;

        } catch (Exception e) { return "000000"; }
    }

    private static byte[] decodeBase32(String value) {
        if (value == null) return new byte[0];
        String cleaned = value.trim().replace("=", "").replace(" ", "").toUpperCase();
        if (cleaned.isEmpty()) return new byte[0];

        List<Byte> output = new ArrayList<>();
        int buffer = 0, bitsLeft = 0;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            int index;
            if      (c >= 'A' && c <= 'Z') index = c - 'A';
            else if (c >= '2' && c <= '7') index = 26 + (c - '2');
            else continue;

            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output.add((byte) ((buffer >> bitsLeft) & 0xFF));
            }
        }

        byte[] result = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) result[i] = output.get(i);
        return result;
    }
}
