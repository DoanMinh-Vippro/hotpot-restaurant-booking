package com.example.hotpotrestaurantbooking_backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    // =========================
    // HMAC SHA512
    // =========================
    public static String hmacSHA512(String key, String data) {
        try {

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

            mac.init(secretKey);

            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildHashData(Map<String, String> params) {

        Map<String, String> filtered = filterParams(params);

        List<String> fieldNames = new ArrayList<>(filtered.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {

            String fieldName = itr.next();
            String fieldValue = filtered.get(fieldName);

            if (fieldValue != null && fieldValue.length() > 0) {

                hashData.append(fieldName);
                hashData.append("=");

                hashData.append(
                        URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                );

                if (itr.hasNext()) {
                    hashData.append("&");
                }
            }
        }

        return hashData.toString();
    }


    //===========================================


    public static String buildQuery(Map<String, String> params) {

        Map<String, String> filtered = filterParams(params);

        List<String> fieldNames = new ArrayList<>(filtered.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {

            String fieldName = itr.next();
            String fieldValue = filtered.get(fieldName);

            if (fieldValue != null && fieldValue.length() > 0) {

                query.append(
                        URLEncoder.encode(fieldName, StandardCharsets.UTF_8)
                );

                query.append("=");

                query.append(
                        URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                );

                if (itr.hasNext()) {
                    query.append("&");
                }
            }
        }

        return query.toString();
    }

    // =========================
    // VERIFY RETURN
    // =========================
    public static boolean verifyReturnData(
            Map<String, String> fields,
            String secretKey
    ) {

        String hashData = buildHashData(fields);

        String secureHash = hmacSHA512(secretKey, hashData);

        return secureHash.equalsIgnoreCase(fields.get("vnp_SecureHash"));
    }

    // =========================
    // REMOVE SIGN PARAMS
    // =========================
    private static Map<String, String> filterParams(Map<String, String> params) {

        Map<String, String> filtered = new HashMap<>();

        for (Map.Entry<String, String> e : params.entrySet()) {

            if (e.getValue() == null || e.getValue().isEmpty())
                continue;

            if ("vnp_SecureHash".equals(e.getKey()))
                continue;

            if ("vnp_SecureHashType".equals(e.getKey()))
                continue;

            filtered.put(e.getKey(), e.getValue());
        }

        return filtered;
    }

}