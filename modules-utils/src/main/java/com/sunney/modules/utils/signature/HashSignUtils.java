package com.sunney.modules.utils.signature;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;



/**
 * Description:
 * 
 * @author LiChunming
 * @version V1.0 @createDateTime��2013-1-11 ����02:56:09
 * @Company: MSD.
 * @Copyright: Copyright (c) 2011
 **/
public class HashSignUtils {

    private static String encodingCharset = "UTF-8";

    /**
     * �Զ������Hash����
     * 
     * @param object �����ܵĶ���
     * @param aKey ��Կ
     * @return
     */
    public static String buildObjectSign(Object object, String aKey) {
        Map<String, Object> sArray = ObjecttoHashMap(object);
        String sign = buildMapSign(sArray, aKey);
        return sign;
    }

    /**
     * ��Map����Hash����
     * 
     * @param sArray �����ܵ�Map����
     * @param aKey ��Կ
     * @return
     */
    public static String buildMapSign(Map<String, Object> sArray, String aKey) {
        // ��key��������
        sArray.put("key", aKey);
        // ����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
        String str = createLinkString(sArray);
        String sign = buildStringSign(str, aKey);
        return sign;
    }

    /**
     * ���ַ��������Hash����
     * 
     * @param args �ַ�����
     * @param key ��Կ
     * @return
     */
    public static String buildStringSign(String[] args, String key) {
        if (args == null || args.length == 0) {
            return (null);
        }
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            str.append(args[i]);
        }
        return (buildStringSign(str.toString(), key));
    }

    /**
     * ���ַ�������Hash����
     * 
     * @param aValue �����ܵ��ַ���
     * @param aKey ��Կ
     * @return
     */
    public static String buildStringSign(String aValue, String aKey) {
        byte k_ipad[] = new byte[64];
        byte k_opad[] = new byte[64];
        byte keyb[];
        byte value[];
        try {
            keyb = aKey.getBytes(encodingCharset);
            value = aValue.getBytes(encodingCharset);
        } catch (UnsupportedEncodingException e) {
            keyb = aKey.getBytes();
            value = aValue.getBytes();
        }

        Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
        Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
        for (int i = 0; i < keyb.length; i++) {
            k_ipad[i] = (byte) (keyb[i] ^ 0x36);
            k_opad[i] = (byte) (keyb[i] ^ 0x5c);
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {

            return null;
        }
        md.update(k_ipad);
        md.update(value);
        byte dg[] = md.digest();
        md.reset();
        md.update(k_opad);
        md.update(dg, 0, 16);
        dg = md.digest();
        return toHex(dg).toUpperCase();
    }

    public static String toHex(byte input[]) {
        if (input == null) return null;
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16) output.append("0");
            output.append(Integer.toString(current, 16));
        }
        return output.toString();
    }

    /**
     * ����תΪHashMap
     * 
     * @param object
     * @return
     */
    public static HashMap<String, Object> ObjecttoHashMap(Object object) {
        HashMap<String, Object> data = new HashMap<String, Object>();
       
        JSONObject jsonObject = JSONObject.fromObject(object);
        Iterator<?> it = jsonObject.keys();
        while (it.hasNext()) {
            String key = String.valueOf(it.next());
            Object value = jsonObject.get(key);
            data.put(key, value);
        }
        return data;
    }

    /**
     * ���ܣ�����������Ԫ�����򣬲�����"����=����ֵ"��ģʽ��"&"�ַ�ƴ�ӳ��ַ���
     * 
     * @param params ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
     * @return ƴ�Ӻ��ַ���
     */
    public static String createLinkString(Map<String, Object> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        // �Բ�����ĸ��������
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            String value = params.get(key).toString();
            if (i == keys.size() - 1) {// ƴ��ʱ�����������һ��&�ַ�
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String value = "UnsupportedEncodingException2222222222222222222222";
        String key = "69cl522AV6q613Ii4W6u8K6XuW8vM1N6bFgyv769220IuYe9u37N4y7rI4Pl";
        String str = buildStringSign(value, key);
        System.out.println(str);
    }

}
